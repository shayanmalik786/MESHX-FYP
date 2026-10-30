package com.meshx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.model.MeshState
import com.meshx.app.ui.components.Chip
import com.meshx.app.ui.theme.MeshColors

/**
 * Weights of the composite link score. These are placeholders until the
 * firmware's real formula is tuned; keep them in sync with docs/protocol.md.
 */
private val scoreWeights = listOf("RSSI" to 0.35f, "SNR" to 0.25f, "Delivery" to 0.25f, "Battery" to 0.15f)

@Composable
fun NodeDetailsScreen(state: MeshState, id: Int, onBack: () -> Unit, onMessage: () -> Unit) {
    val n = state.node(id)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text(n?.let { "Node ${it.id} · ${it.name}" } ?: "Node $id", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    when {
                        n == null -> "No data yet"
                        n.online -> "Online · last heard ${n.lastSeenSec} s ago"
                        else -> "Offline · last heard ${n.lastSeenSec / 60} min ago"
                    },
                    color = MeshColors.Muted, fontSize = 12.sp,
                )
            }
        }
        if (n == null) return@Column
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val metrics = listOf(
                Triple("RSSI", "${n.rssi} dBm", if (n.rssi > -85) MeshColors.Success else if (n.rssi > -105) MeshColors.Warning else MeshColors.Danger),
                Triple("SNR", "${n.snr} dB", if (n.snr > 5) MeshColors.Success else MeshColors.Warning),
                Triple("Battery", "${n.battery}%", if (n.battery > 30) MeshColors.Success else MeshColors.Danger),
                Triple("Packet loss", "${(n.loss * 100).toInt()}%", if (n.loss < 0.1f) MeshColors.Success else MeshColors.Warning),
                Triple("Link score", "%.2f".format(n.score), MeshColors.PrimaryDark),
                Triple("Hops from you", "${n.hops}", MeshColors.PrimaryDark),
            )
            metrics.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value, color) -> Metric(label, value, color, Modifier.weight(1f)) }
                }
            }

            Card {
                Text("How the link score is built", fontWeight = FontWeight.SemiBold)
                val parts = listOf(
                    ((n.rssi + 120) / 70f).coerceIn(0f, 1f),
                    ((n.snr + 10) / 20f).coerceIn(0f, 1f),
                    1f - n.loss,
                    n.battery / 100f,
                )
                scoreWeights.zip(parts).forEach { (w, v) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(w.first, fontSize = 12.sp, color = MeshColors.Muted, modifier = Modifier.width(64.dp))
                        Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MeshColors.BubbleIn)) {
                            Box(Modifier.fillMaxWidth(v).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(MeshColors.Primary))
                        }
                        Text("w ${w.second}", fontSize = 11.sp, color = MeshColors.Muted)
                    }
                }
            }

            val neighbours = state.links.filter { it.a == id || it.b == id }
            if (neighbours.isNotEmpty()) {
                Card {
                    Text("Neighbours of Node $id", fontWeight = FontWeight.SemiBold)
                    neighbours.forEach { l ->
                        val other = if (l.a == id) l.b else l.a
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (other == state.myNodeId) "Node $other (You)" else "Node $other",
                                fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f),
                            )
                            Chip("%.2f".format(l.score), fg = MeshColors.score(l.score))
                        }
                    }
                }
            }
            Button(onClick = onMessage, enabled = n.online, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Send message")
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(MeshColors.Surface).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(label, fontSize = 12.sp, color = MeshColors.Muted)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MeshColors.Surface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}
