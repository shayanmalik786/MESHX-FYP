package com.meshx.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.model.BROADCAST_ID
import com.meshx.app.model.DeliveryStatus
import com.meshx.app.model.MeshState
import com.meshx.app.model.Message
import com.meshx.app.ui.theme.MeshColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(millis: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

fun peerName(state: MeshState, peer: Int): String =
    if (peer == BROADCAST_ID) "Broadcast · All nodes"
    else state.node(peer)?.let { "${it.name} · Node ${it.id}" } ?: "Node $peer"

fun initials(name: String): String =
    name.split(' ', '·').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Composable
fun Avatar(text: String, size: Int = 48) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(MeshColors.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MeshColors.PrimaryDark, fontWeight = FontWeight.SemiBold, fontSize = (size / 3).sp)
    }
}

@Composable
fun Chip(text: String, bg: Color = MeshColors.BubbleIn, fg: Color = MeshColors.Muted) {
    Text(
        text,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** "Connected to MeshX-03 · RSSI · battery" strip at the top of Chats. */
@Composable
fun NodeStatusBar(state: MeshState, modifier: Modifier = Modifier) {
    val me = state.myNodeId
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MeshColors.PrimarySoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(MeshColors.Success))
        Text(
            "Connected to ${state.connectedName ?: "node"}" + (me?.let { " · Node $it" } ?: ""),
            color = MeshColors.PrimaryDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Outlined.SignalCellularAlt, null, tint = MeshColors.PrimaryDark, modifier = Modifier.size(16.dp))
        Text("${state.nodes.count { it.online }} online", color = MeshColors.PrimaryDark, fontSize = 12.sp)
        Icon(Icons.Outlined.BatteryStd, null, tint = MeshColors.PrimaryDark, modifier = Modifier.size(16.dp))
        Text("${state.myBattery ?: "--"}%", color = MeshColors.PrimaryDark, fontSize = 12.sp)
    }
}

@Composable
fun MessageBubble(m: Message) {
    val out = m.outgoing
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (out) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 260.dp)
                // Corner order: topStart, topEnd, bottomEnd, bottomStart
                .clip(RoundedCornerShape(16.dp, 16.dp, if (out) 4.dp else 16.dp, if (out) 16.dp else 4.dp))
                .background(if (out) MeshColors.Primary else MeshColors.BubbleIn)
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(m.text, color = if (out) Color.White else MeshColors.Ink, fontSize = 14.sp)
            Spacer(Modifier.size(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val info = buildString {
                    append(formatTime(m.timeMillis))
                    m.hops?.let { append(" · $it hop${if (it == 1) "" else "s"}") }
                    if (!out) m.rssi?.let { append(" · RSSI $it") }
                }
                Text(info, fontSize = 11.sp, color = if (out) Color.White.copy(alpha = 0.85f) else MeshColors.Muted)
                if (out) {
                    val icon = when (m.status) {
                        DeliveryStatus.SENDING -> Icons.Outlined.Schedule
                        DeliveryStatus.SENT -> Icons.Outlined.Done
                        DeliveryStatus.DELIVERED -> Icons.Outlined.DoneAll
                        DeliveryStatus.FAILED -> Icons.Outlined.ErrorOutline
                    }
                    Icon(icon, m.status.name, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
