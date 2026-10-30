package com.meshx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.model.BROADCAST_ID
import com.meshx.app.model.MeshState
import com.meshx.app.ui.components.Avatar
import com.meshx.app.ui.components.Chip
import com.meshx.app.ui.components.NodeStatusBar
import com.meshx.app.ui.components.formatTime
import com.meshx.app.ui.components.initials
import com.meshx.app.ui.components.peerName
import com.meshx.app.ui.theme.MeshColors

@Composable
fun ChatsScreen(state: MeshState, onOpenChat: (Int) -> Unit) {
    var picking by remember { mutableStateOf(false) }

    // One row per peer we have messages with, newest first.
    val conversations = state.messages.groupBy { it.peer }
        .map { (peer, msgs) -> peer to msgs.maxBy { it.timeMillis } }
        .sortedByDescending { it.second.timeMillis }

    Scaffold(
        containerColor = MeshColors.Bg,
        contentWindowInsets = WindowInsets(0), // the app-level Scaffold already handles system bars
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { picking = true },
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("New message") },
                containerColor = MeshColors.Primary,
                contentColor = MeshColors.Surface,
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            Text("MeshX", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            NodeStatusBar(state, Modifier.padding(horizontal = 16.dp))
            if (conversations.isEmpty()) {
                Text(
                    "No messages yet. Tap New message to say hello to another node.",
                    color = MeshColors.Muted, modifier = Modifier.padding(24.dp),
                )
            }
            LazyColumn(Modifier.padding(top = 8.dp)) {
                items(conversations, key = { it.first }) { (peer, last) ->
                    val name = peerName(state, peer)
                    val hops = if (peer == BROADCAST_ID) "flood" else state.node(peer)?.hops?.let { "$it hop${if (it == 1) "" else "s"}" }
                    Row(
                        Modifier.fillMaxWidth().background(MeshColors.Surface).clickable { onOpenChat(peer) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Avatar(if (peer == BROADCAST_ID) "ALL" else initials(name))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                hops?.let { Chip(it) }
                            }
                            Text(
                                (if (last.outgoing) "You: " else "") + last.text,
                                color = MeshColors.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(formatTime(last.timeMillis), color = MeshColors.Muted, fontSize = 12.sp)
                    }
                    HorizontalDivider(color = MeshColors.Outline)
                }
            }
        }
    }

    if (picking) {
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text("Send to") },
            text = {
                Column {
                    val targets = listOf(BROADCAST_ID) + state.nodes.filter { it.online && it.id != state.myNodeId }.map { it.id }
                    targets.forEach { id ->
                        Text(
                            peerName(state, id),
                            modifier = Modifier.fillMaxWidth().clickable { picking = false; onOpenChat(id) }.padding(vertical = 12.dp),
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
        )
    }
}
