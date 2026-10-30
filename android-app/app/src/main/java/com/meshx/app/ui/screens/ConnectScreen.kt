package com.meshx.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.meshx.app.model.ConnectionState
import com.meshx.app.model.MeshState
import com.meshx.app.ui.theme.MeshColors

private val blePermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

@Composable
fun ConnectScreen(
    state: MeshState,
    demoMode: Boolean,
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (String) -> Unit,
    onConnected: () -> Unit,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.all { it }) onScan()
    }
    fun scan() {
        val granted = demoMode || blePermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) onScan() else permissionLauncher.launch(blePermissions)
    }

    LaunchedEffect(Unit) { scan() }
    LaunchedEffect(state.connection) {
        if (state.connection == ConnectionState.CONNECTED) onConnected()
    }

    val scanning = state.connection == ConnectionState.SCANNING
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column {
            Text("Connect your node", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(if (demoMode) "Demo mode · no hardware needed" else "Step 1 of 2 · Bluetooth", color = MeshColors.Muted, fontSize = 12.sp)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MeshColors.Surface), shape = RoundedCornerShape(20.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.size(80.dp).clip(CircleShape).background(MeshColors.PrimarySoft), contentAlignment = Alignment.Center) {
                    if (scanning || state.connection == ConnectionState.CONNECTING) {
                        CircularProgressIndicator(Modifier.size(76.dp), strokeWidth = 4.dp)
                    }
                    Icon(Icons.Outlined.Bluetooth, null, tint = MeshColors.PrimaryDark, modifier = Modifier.size(32.dp))
                }
                Text(
                    when (state.connection) {
                        ConnectionState.SCANNING -> "Scanning for MeshX nodes…"
                        ConnectionState.CONNECTING -> "Connecting…"
                        else -> "Ready to scan"
                    },
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                )
                Text(
                    "Power on your node and keep the phone within 5 m.",
                    color = MeshColors.Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
                )
                state.error?.let { Text(it, color = MeshColors.Danger, fontSize = 13.sp) }
                TextButton(onClick = { if (scanning) onStopScan() else scan() }) {
                    Text(if (scanning) "Stop scan" else "Scan again")
                }
            }
        }
        Text("NODES NEARBY", color = MeshColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(state.scanResults, key = { it.address }) { item ->
                val best = item == state.scanResults.maxByOrNull { it.rssi }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MeshColors.Surface),
                    shape = RoundedCornerShape(16.dp),
                    border = if (best) BorderStroke(1.5.dp, MeshColors.Primary) else null,
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MeshColors.PrimarySoft),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Outlined.CellTower, null, tint = MeshColors.PrimaryDark, modifier = Modifier.size(20.dp)) }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("${item.rssi} dBm", color = MeshColors.Muted, fontSize = 12.sp)
                        }
                        if (best) Button(onClick = { onConnect(item.address) }) { Text("Connect") }
                        else FilledTonalButton(onClick = { onConnect(item.address) }) { Text("Connect") }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Can't find your node? Check that its blue LED is blinking, then tap Scan again.",
            color = MeshColors.Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}
