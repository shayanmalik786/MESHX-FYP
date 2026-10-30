package com.meshx.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meshx.app.MeshViewModel
import com.meshx.app.ui.screens.ChatScreen
import com.meshx.app.ui.screens.ChatsScreen
import com.meshx.app.ui.screens.ConnectScreen
import com.meshx.app.ui.screens.NetworkScreen
import com.meshx.app.ui.screens.NodeDetailsScreen
import com.meshx.app.ui.screens.SettingsScreen
import com.meshx.app.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val CONNECT = "connect"
    const val CHATS = "chats"
    const val CHAT = "chat/{peer}"
    const val NETWORK = "network"
    const val NODE = "node/{id}"
    const val SETTINGS = "settings"
    fun chat(peer: Int) = "chat/$peer"
    fun node(id: Int) = "node/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.CHATS, "Chats", Icons.AutoMirrored.Outlined.Chat),
    Tab(Routes.NETWORK, "Network", Icons.Outlined.Hub),
    Tab(Routes.SETTINGS, "Settings", Icons.Outlined.Tune),
)

@Composable
fun MeshXApp(vm: MeshViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()
    val demo by vm.demoMode.collectAsStateWithLifecycle()
    val name by vm.displayName.collectAsStateWithLifecycle()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        bottomBar = { if (tabs.any { it.route == route }) BottomBar(nav, route) },
    ) { padding ->
        NavHost(nav, startDestination = Routes.WELCOME, modifier = Modifier.padding(padding)) {
            composable(Routes.WELCOME) {
                WelcomeScreen(onStart = { nav.navigate(Routes.CONNECT) })
            }
            composable(Routes.CONNECT) {
                ConnectScreen(
                    state = state,
                    demoMode = demo,
                    onScan = vm::startScan,
                    onStopScan = vm::stopScan,
                    onConnect = vm::connect,
                    onConnected = {
                        nav.navigate(Routes.CHATS) { popUpTo(Routes.WELCOME) { inclusive = true } }
                    },
                )
            }
            composable(Routes.CHATS) {
                ChatsScreen(state, onOpenChat = { nav.navigate(Routes.chat(it)) })
            }
            composable(Routes.CHAT, arguments = listOf(navArgument("peer") { type = NavType.IntType })) {
                val peer = it.arguments?.getInt("peer") ?: 0
                ChatScreen(state, peer, onBack = { nav.popBackStack() }, onSend = { text -> vm.send(peer, text) })
            }
            composable(Routes.NETWORK) {
                NetworkScreen(state, onMode = vm::setRoutingMode, onOpenNode = { nav.navigate(Routes.node(it)) })
            }
            composable(Routes.NODE, arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                val id = it.arguments?.getInt("id") ?: 0
                NodeDetailsScreen(state, id, onBack = { nav.popBackStack() }, onMessage = { nav.navigate(Routes.chat(id)) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    state = state,
                    displayName = name,
                    demoMode = demo,
                    onName = { vm.displayName.value = it },
                    onDemoMode = vm::setDemoMode,
                    onDisconnect = {
                        vm.disconnect()
                        nav.navigate(Routes.CONNECT) { popUpTo(0) }
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, route: String?) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = route == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(Routes.CHATS) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
