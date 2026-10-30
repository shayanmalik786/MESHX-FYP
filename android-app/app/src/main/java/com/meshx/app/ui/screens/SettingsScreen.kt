package com.meshx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshx.app.model.MeshState
import com.meshx.app.ui.theme.MeshColors

@Composable
fun SettingsScreen(
    state: MeshState,
    displayName: String,
    demoMode: Boolean,
    onName: (String) -> Unit,
    onDemoMode: (Boolean) -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Section("Profile") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Person, null, tint = MeshColors.PrimaryDark)
                OutlinedTextField(displayName, onName, label = { Text("Display name") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Item(Icons.Outlined.CellTower, "My node ID", state.myNodeId?.let { "Node $it" } ?: "Not connected")
        }
        Section("Radio") {
            // Matches the Ra-02 prototype firmware: RadioLib begin(433.0, 125, 9, 7, 0x12, 10, 8, 0)
            Item(Icons.Outlined.CellTower, "Frequency & preset", "433 MHz · SF9 · BW 125 kHz · CR 4/7")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Item(Icons.Outlined.Bluetooth, "Connected node", state.connectedName ?: "None", Modifier.weight(1f))
                TextButton(onClick = onDisconnect) { Text("Disconnect") }
            }
        }
        Section("Mesh & security") {
            Item(Icons.Outlined.Route, "Routing mode", state.routingMode.label + " · change it on the Network tab")
            Item(Icons.Outlined.Lock, "Network key", "AES-256 · key sharing by QR comes in a later version")
        }
        Section("Developer") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Item(Icons.Outlined.Science, "Demo mode", "Fake 6-node mesh, no hardware needed", Modifier.weight(1f))
                Switch(checked = demoMode, onCheckedChange = onDemoMode)
            }
        }
        Section("About") {
            Item(Icons.Outlined.Info, "MeshX v0.1", "CISD Final Year Design Project · Team MeshX")
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title.uppercase(), color = MeshColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MeshColors.Surface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) { content() }
}

@Composable
private fun Item(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MeshColors.PrimaryDark, modifier = Modifier.size(22.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 12.sp, color = MeshColors.Muted)
        }
    }
}
