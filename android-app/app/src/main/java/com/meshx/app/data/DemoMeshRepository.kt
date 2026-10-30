package com.meshx.app.data

import com.meshx.app.model.ConnectionState
import com.meshx.app.model.DeliveryStatus
import com.meshx.app.model.Link
import com.meshx.app.model.MeshNode
import com.meshx.app.model.MeshState
import com.meshx.app.model.Message
import com.meshx.app.model.RoutingMode
import com.meshx.app.model.ScanItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A fake mesh matching the Figma design, used for demos and UI work before hardware is ready. */
class DemoMeshRepository(private val scope: CoroutineScope) : MeshRepository {

    private val me = 3
    private var nextId = 1000L

    private val nodes = listOf(
        MeshNode(1, "Shayan", -71, 9.0f, 64, 0.02f, 0.82f, 1, 3),
        MeshNode(2, "Fatima", -92, 7.5f, 64, 0.03f, 0.86f, 2, 5),
        MeshNode(4, "Umaiza", -101, 3.0f, 40, 0.12f, 0.55f, 3, 9),
        MeshNode(5, "Hamna", -79, 8.0f, 71, 0.04f, 0.91f, 3, 2),
        MeshNode(6, "Node 6", -120, 0f, 0, 1f, 0f, 0, 600),
    )
    private val links = listOf(
        Link(3, 1, 0.82f), Link(3, 2, 0.86f), Link(1, 4, 0.55f),
        Link(2, 5, 0.91f), Link(2, 4, 0.41f), Link(4, 5, 0.6f),
    )

    private val _state = MutableStateFlow(MeshState())
    override val state: StateFlow<MeshState> = _state

    override fun startScan() {
        _state.update { it.copy(connection = ConnectionState.SCANNING, scanResults = emptyList()) }
        scope.launch {
            val found = listOf(
                ScanItem("DE:MO:00:00:00:03", "MeshX-03", -48),
                ScanItem("DE:MO:00:00:00:01", "MeshX-01", -71),
                ScanItem("DE:MO:00:00:00:05", "MeshX-05", -90),
            )
            for (item in found) {
                delay(600)
                _state.update { it.copy(scanResults = it.scanResults + item) }
            }
        }
    }

    override fun stopScan() {
        _state.update { if (it.connection == ConnectionState.SCANNING) it.copy(connection = ConnectionState.DISCONNECTED) else it }
    }

    override fun connect(address: String) {
        val name = _state.value.scanResults.firstOrNull { it.address == address }?.name ?: "MeshX-03"
        _state.update { it.copy(connection = ConnectionState.CONNECTING) }
        scope.launch {
            delay(800)
            val now = System.currentTimeMillis()
            fun at(minAgo: Int) = now - minAgo * 60_000L
            _state.update {
                it.copy(
                    connection = ConnectionState.CONNECTED,
                    connectedName = name,
                    myNodeId = me,
                    myBattery = 82,
                    nodes = nodes,
                    links = links,
                    messages = listOf(
                        Message(1, 2, false, "Testing from the library, can you read me?", at(4), 2, -92),
                        Message(2, 2, true, "Loud and clear! Coming via Node 3", at(3), 2),
                        Message(3, 2, false, "Reached the library, signal is good", at(1), 2, -92),
                        Message(4, 4, false, "Battery at 40%, moving closer", at(12), 3, -101),
                        Message(5, 5, false, "Ok, testing route recovery now", at(44), 3, -79),
                        Message(6, 255, false, "Meeting at block C in 15 min", at(87), 1, -71),
                    ),
                )
            }
        }
    }

    override fun disconnect() {
        _state.value = MeshState()
    }

    override fun send(to: Int, text: String) {
        val id = nextId++
        val hops = nodes.firstOrNull { it.id == to }?.hops ?: 1
        _state.update {
            it.copy(messages = it.messages + Message(id, to, true, text, System.currentTimeMillis(), hops, status = DeliveryStatus.SENDING))
        }
        scope.launch {
            delay(500)
            setStatus(id, DeliveryStatus.SENT)
            delay(400L + 300L * hops)
            val online = to == 255 || nodes.firstOrNull { it.id == to }?.online == true
            setStatus(id, if (online) DeliveryStatus.DELIVERED else DeliveryStatus.FAILED)
            if (online && to != 255) {
                delay(1500)
                val from = nodes.first { it.id == to }
                _state.update {
                    it.copy(messages = it.messages + Message(nextId++, to, false, "Got it: \"${text.take(30)}\"", System.currentTimeMillis(), from.hops, from.rssi))
                }
            }
        }
    }

    override fun setRoutingMode(mode: RoutingMode) {
        _state.update { it.copy(routingMode = mode) }
    }

    private fun setStatus(id: Long, status: DeliveryStatus) {
        _state.update { s -> s.copy(messages = s.messages.map { if (it.id == id) it.copy(status = status) else it }) }
    }
}
