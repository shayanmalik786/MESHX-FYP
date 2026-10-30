package com.meshx.app.model

/** Node id 255 means "every node" (broadcast / flooding). */
const val BROADCAST_ID = 255

enum class DeliveryStatus { SENDING, SENT, DELIVERED, FAILED }

enum class RoutingMode(val label: String) {
    ADAPTIVE("Adaptive"),
    SHORTEST_HOP("Shortest hop"),
    FLOODING("Flooding"),
}

data class Message(
    val id: Long,
    /** The other node in the conversation (or BROADCAST_ID). */
    val peer: Int,
    val outgoing: Boolean,
    val text: String,
    val timeMillis: Long,
    val hops: Int? = null,
    val rssi: Int? = null,
    val status: DeliveryStatus = DeliveryStatus.DELIVERED,
)

/** A node in the mesh as reported by the connected node. */
data class MeshNode(
    val id: Int,
    val name: String,
    val rssi: Int,
    val snr: Float,
    val battery: Int,
    /** Packet loss 0..1 */
    val loss: Float,
    /** Composite link-quality score 0..1 (RSSI, SNR, loss, battery). */
    val score: Float,
    val hops: Int,
    val lastSeenSec: Int,
) {
    val online: Boolean get() = lastSeenSec < 120
}

data class Link(val a: Int, val b: Int, val score: Float)

/** A node found during the Bluetooth scan. */
data class ScanItem(val address: String, val name: String, val rssi: Int)

enum class ConnectionState { DISCONNECTED, SCANNING, CONNECTING, CONNECTED }

/** Everything the UI needs, produced by a MeshRepository. */
data class MeshState(
    val connection: ConnectionState = ConnectionState.DISCONNECTED,
    val connectedName: String? = null,
    val myNodeId: Int? = null,
    val myBattery: Int? = null,
    val scanResults: List<ScanItem> = emptyList(),
    val nodes: List<MeshNode> = emptyList(),
    val links: List<Link> = emptyList(),
    val messages: List<Message> = emptyList(),
    val routingMode: RoutingMode = RoutingMode.ADAPTIVE,
    val error: String? = null,
) {
    fun node(id: Int): MeshNode? = nodes.firstOrNull { it.id == id }
    fun conversation(peer: Int): List<Message> = messages.filter { it.peer == peer }.sortedBy { it.timeMillis }
}
