package com.meshx.app.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import com.meshx.app.model.ConnectionState
import com.meshx.app.model.DeliveryStatus
import com.meshx.app.model.Link
import com.meshx.app.model.MeshNode
import com.meshx.app.model.MeshState
import com.meshx.app.model.Message
import com.meshx.app.model.RoutingMode
import com.meshx.app.model.ScanItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Talks to a MeshX node over BLE using the Nordic UART Service (NUS):
 * the phone writes frames to RX and receives frames as notifications on TX.
 * Frames are the text lines described in [Protocol].
 *
 * Permissions (BLUETOOTH_SCAN / BLUETOOTH_CONNECT, or location on Android 11 and older)
 * are requested by the Connect screen before any of these calls.
 */
@SuppressLint("MissingPermission")
class BleMeshRepository(context: Context) : MeshRepository {

    companion object {
        val SERVICE: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val RX_CHAR: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e") // phone -> node
        val TX_CHAR: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e") // node -> phone
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        const val NAME_PREFIX = "MeshX"
    }

    private val appContext = context.applicationContext
    private val adapter = appContext.getSystemService(BluetoothManager::class.java)?.adapter
    private val _state = MutableStateFlow(MeshState())
    override val state: StateFlow<MeshState> = _state

    private var gatt: BluetoothGatt? = null
    private var rx: BluetoothGattCharacteristic? = null
    private var mtu = 23
    private val lines = LineBuffer()
    private var nextId = System.currentTimeMillis() % 1_000_000

    // Writes must go one at a time; the next chunk is sent from onCharacteristicWrite.
    private val writeQueue = ArrayDeque<ByteArray>()
    private var writing = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.scanRecord?.deviceName ?: result.device.name ?: return
            if (!name.startsWith(NAME_PREFIX)) return
            val item = ScanItem(result.device.address, name, result.rssi)
            _state.update { s -> s.copy(scanResults = (s.scanResults.filter { it.address != item.address } + item).sortedByDescending { it.rssi }) }
        }

        override fun onScanFailed(errorCode: Int) {
            _state.update { it.copy(connection = ConnectionState.DISCONNECTED, error = "Scan failed ($errorCode)") }
        }
    }

    override fun startScan() {
        val scanner = adapter?.bluetoothLeScanner
        if (adapter == null || !adapter.isEnabled || scanner == null) {
            _state.update { it.copy(error = "Turn on Bluetooth first") }
            return
        }
        _state.update { it.copy(connection = ConnectionState.SCANNING, scanResults = emptyList(), error = null) }
        scanner.startScan(scanCallback)
    }

    override fun stopScan() {
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _state.update { if (it.connection == ConnectionState.SCANNING) it.copy(connection = ConnectionState.DISCONNECTED) else it }
    }

    override fun connect(address: String) {
        stopScan()
        val device: BluetoothDevice = adapter?.getRemoteDevice(address) ?: return
        _state.update { it.copy(connection = ConnectionState.CONNECTING, connectedName = device.name ?: address) }
        gatt = device.connectGatt(appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        rx = null
        writeQueue.clear()
        writing = false
        _state.value = MeshState()
    }

    override fun send(to: Int, text: String) {
        val id = nextId++
        _state.update {
            it.copy(messages = it.messages + Message(id, to, true, text, System.currentTimeMillis(), status = DeliveryStatus.SENDING))
        }
        write(Protocol.encodeMessage(id, to, text))
    }

    override fun setRoutingMode(mode: RoutingMode) {
        _state.update { it.copy(routingMode = mode) }
        write(Protocol.encodeMode(mode))
    }

    private fun write(frame: String) {
        val bytes = frame.toByteArray(Charsets.UTF_8)
        val chunk = (mtu - 3).coerceAtLeast(20)
        synchronized(writeQueue) {
            bytes.toList().chunked(chunk).forEach { writeQueue.addLast(it.toByteArray()) }
        }
        pump()
    }

    @Suppress("DEPRECATION")
    private fun pump() {
        val g = gatt ?: return
        val c = rx ?: return
        val next = synchronized(writeQueue) {
            if (writing || writeQueue.isEmpty()) return
            writing = true
            writeQueue.removeFirst()
        }
        if (Build.VERSION.SDK_INT >= 33) {
            g.writeCharacteristic(c, next, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            c.value = next
            g.writeCharacteristic(c)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.requestMtu(247)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                g.close()
                gatt = null
                rx = null
                _state.update { it.copy(connection = ConnectionState.DISCONNECTED, error = "Node disconnected") }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, newMtu: Int, status: Int) {
            mtu = newMtu
            g.discoverServices()
        }

        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(SERVICE)
            val tx = service?.getCharacteristic(TX_CHAR)
            rx = service?.getCharacteristic(RX_CHAR)
            if (tx == null || rx == null) {
                _state.update { it.copy(error = "This device is not a MeshX node") }
                g.disconnect()
                return
            }
            g.setCharacteristicNotification(tx, true)
            val cccd = tx.getDescriptor(CCCD)
            if (Build.VERSION.SDK_INT >= 33) {
                g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(cccd)
            }
        }

        override fun onDescriptorWrite(g: BluetoothGatt, d: BluetoothGattDescriptor, status: Int) {
            _state.update { it.copy(connection = ConnectionState.CONNECTED, error = null) }
            write(Protocol.encodeHello())
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) {
            synchronized(writeQueue) { writing = false }
            pump()
        }

        // Android 13+
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray) {
            onBytes(value)
        }

        // Android 12 and older
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < 33) onBytes(c.value ?: return)
        }
    }

    private fun onBytes(bytes: ByteArray) {
        lines.append(bytes.toString(Charsets.UTF_8)).forEach { line ->
            Protocol.decode(line)?.let(::apply)
        }
    }

    private fun apply(frame: Frame) {
        _state.update { s ->
            when (frame) {
                is Frame.Me -> s.copy(myNodeId = frame.nodeId, myBattery = frame.battery)
                is Frame.Rx -> s.copy(
                    messages = s.messages + Message(frame.msgId, frame.from, false, frame.text, System.currentTimeMillis(), frame.hops, frame.rssi)
                )
                is Frame.Sent -> s.withStatus(frame.msgId, DeliveryStatus.SENT, null)
                is Frame.Ack -> s.withStatus(frame.msgId, DeliveryStatus.DELIVERED, frame.hops)
                is Frame.Fail -> s.withStatus(frame.msgId, DeliveryStatus.FAILED, null)
                is Frame.Node -> {
                    val n = MeshNode(frame.id, frame.name, frame.rssi, frame.snr, frame.battery, frame.loss, frame.score, frame.hops, frame.lastSeenSec)
                    s.copy(nodes = (s.nodes.filter { it.id != n.id } + n).sortedBy { it.id })
                }
                is Frame.Link -> s.copy(
                    links = s.links.filterNot { (it.a == frame.a && it.b == frame.b) || (it.a == frame.b && it.b == frame.a) } +
                        Link(frame.a, frame.b, frame.score)
                )
            }
        }
    }

    private fun MeshState.withStatus(id: Long, status: DeliveryStatus, hops: Int?) =
        copy(messages = messages.map { if (it.id == id) it.copy(status = status, hops = hops ?: it.hops) else it })
}
