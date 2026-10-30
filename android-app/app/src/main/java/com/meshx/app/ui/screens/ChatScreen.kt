package com.meshx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.data.Protocol
import com.meshx.app.model.BROADCAST_ID
import com.meshx.app.model.MeshState
import com.meshx.app.ui.components.Avatar
import com.meshx.app.ui.components.MessageBubble
import com.meshx.app.ui.components.initials
import com.meshx.app.ui.components.peerName
import com.meshx.app.ui.theme.MeshColors

@Composable
fun ChatScreen(state: MeshState, peer: Int, onBack: () -> Unit, onSend: (String) -> Unit) {
    val messages = state.conversation(peer)
    val node = state.node(peer)
    val name = peerName(state, peer)
    var draft by rememberSaveable { mutableStateOf("") }
    val list = rememberLazyListState()
    // +1 because the list starts with a header item
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) list.animateScrollToItem(messages.size) }

    Column(Modifier.fillMaxSize().background(MeshColors.Bg).imePadding()) {
        Row(
            Modifier.fillMaxWidth().background(MeshColors.Surface).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            Avatar(if (peer == BROADCAST_ID) "ALL" else initials(name), size = 40)
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                val sub = when {
                    peer == BROADCAST_ID -> "Sent to every node by flooding" to MeshColors.Muted
                    node == null -> "Not seen yet" to MeshColors.Muted
                    node.online -> "Reachable · ${node.hops} hop${if (node.hops == 1) "" else "s"} · score ${"%.2f".format(node.score)}" to MeshColors.Success
                    else -> "Offline · last heard ${node.lastSeenSec / 60} min ago" to MeshColors.Danger
                }
                Text(sub.first, color = sub.second, fontSize = 12.sp)
            }
        }
        LazyColumn(
            state = list,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
                    Text(
                        "Encrypted · max ${Protocol.MAX_TEXT} characters",
                        color = MeshColors.Muted, fontSize = 11.sp,
                        modifier = Modifier.background(MeshColors.BubbleIn, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            items(messages, key = { it.id }) { MessageBubble(it) }
        }
        Row(
            Modifier.fillMaxWidth().background(MeshColors.Surface).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(Protocol.MAX_TEXT) },
                placeholder = { Text("Message") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (draft.isNotBlank()) { onSend(draft); draft = "" } }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MeshColors.Bg,
                    focusedContainerColor = MeshColors.Bg,
                    unfocusedBorderColor = MeshColors.Bg,
                ),
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(
                onClick = { onSend(draft); draft = "" },
                enabled = draft.isNotBlank(),
                modifier = Modifier.size(48.dp),
            ) { Icon(Icons.AutoMirrored.Outlined.Send, "Send") }
        }
    }
}
