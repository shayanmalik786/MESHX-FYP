package com.meshx.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.model.MeshNode
import com.meshx.app.model.MeshState
import com.meshx.app.model.RoutingMode
import com.meshx.app.ui.theme.MeshColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(state: MeshState, onMode: (RoutingMode) -> Unit, onOpenNode: (Int) -> Unit) {
    val online = state.nodes.count { it.online } + 1 // + our own node
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text("Network", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("$online of ${state.nodes.size + 1} nodes online · tap a node for details", color = MeshColors.Muted, fontSize = 12.sp)
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            RoutingMode.entries.forEachIndexed { i, mode ->
                SegmentedButton(
                    selected = state.routingMode == mode,
                    onClick = { onMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(i, RoutingMode.entries.size),
                ) { Text(mode.label, fontSize = 12.sp) }
            }
        }
        Topology(state, onOpenNode, Modifier.fillMaxWidth().weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(MeshColors.Success, "Strong")
            Legend(MeshColors.Warning, "Fair")
            Legend(MeshColors.Danger, "Weak")
            Legend(MeshColors.Outline, "Offline")
        }
        val live = state.nodes.filter { it.online }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("${live.size}", "Neighbours online", Modifier.weight(1f))
            Stat(if (live.isEmpty()) "–" else "%.1f".format(live.map { it.hops }.average()), "Avg hops", Modifier.weight(1f))
            Stat(if (live.isEmpty()) "–" else "%.2f".format(live.map { it.score }.average()), "Avg link score", Modifier.weight(1f))
        }
    }
}

/** Draws our node at the bottom and the others on an arc above it, joined by links coloured by score. */
@Composable
private fun Topology(state: MeshState, onOpenNode: (Int) -> Unit, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    val me = state.myNodeId ?: 0
    val others = state.nodes.filter { it.id != me }.sortedBy { it.id }

    fun positions(w: Float, h: Float): Map<Int, Offset> {
        val center = Offset(w / 2, h * 0.55f)
        val r = minOf(w, h) * 0.40f
        val map = mutableMapOf(me to Offset(w / 2, h * 0.88f))
        others.forEachIndexed { i, n ->
            val t = if (others.size == 1) 0.5 else i / (others.size - 1).toDouble()
            val angle = PI + t * PI // left -> over the top -> right
            map[n.id] = Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat() * 1.1f)
        }
        return map
    }

    Box(modifier.clip(RoundedCornerShape(20.dp)).background(MeshColors.Surface)) {
        Canvas(
            Modifier.fillMaxSize().pointerInput(state.nodes) {
                detectTapGestures { tap ->
                    val pos = positions(size.width.toFloat(), size.height.toFloat())
                    pos.entries.filter { it.key != me }
                        .minByOrNull { (it.value - tap).getDistance() }
                        ?.takeIf { (it.value - tap).getDistance() < 40.dp.toPx() }
                        ?.let { onOpenNode(it.key) }
                }
            },
        ) {
            val pos = positions(size.width, size.height)
            val radius = 22.dp.toPx()
            state.links.forEach { l ->
                val a = pos[l.a] ?: return@forEach
                val b = pos[l.b] ?: return@forEach
                val offline = listOf(l.a, l.b).any { id -> id != me && state.node(id)?.online != true }
                drawLine(
                    color = if (offline) MeshColors.Outline else MeshColors.score(l.score),
                    start = a, end = b,
                    strokeWidth = (2 + 4 * l.score).dp.toPx() / 2,
                    pathEffect = if (offline) PathEffect.dashPathEffect(floatArrayOf(12f, 12f)) else null,
                )
            }
            pos.forEach { (id, p) ->
                val node: MeshNode? = state.node(id)
                val isMe = id == me
                val off = !isMe && node?.online != true
                drawCircle(if (isMe) MeshColors.Primary else if (off) MeshColors.BubbleIn else MeshColors.PrimarySoft, radius, p)
                drawCircle(if (off) MeshColors.Outline else MeshColors.Primary, radius, p, style = Stroke(2.dp.toPx()))
                val label = measurer.measure("N$id", TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (isMe) Color.White else if (off) MeshColors.Muted else MeshColors.PrimaryDark))
                drawText(label, topLeft = p - Offset(label.size.width / 2f, label.size.height / 2f))
                val sub = measurer.measure(if (isMe) "You" else if (off) "Offline" else node?.name ?: "", TextStyle(fontSize = 11.sp, color = MeshColors.Muted))
                drawText(sub, topLeft = p + Offset(-sub.size.width / 2f, radius + 2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(14.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(text, fontSize = 11.sp, color = MeshColors.Muted)
    }
}

@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(MeshColors.Surface).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MeshColors.PrimaryDark)
        Text(label, fontSize = 11.sp, color = MeshColors.Muted)
    }
}
