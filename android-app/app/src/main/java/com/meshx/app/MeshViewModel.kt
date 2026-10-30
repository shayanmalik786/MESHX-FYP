package com.meshx.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meshx.app.data.BleMeshRepository
import com.meshx.app.data.DemoMeshRepository
import com.meshx.app.data.MeshRepository
import com.meshx.app.model.MeshState
import com.meshx.app.model.RoutingMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MeshViewModel(app: Application) : AndroidViewModel(app) {

    private val demo by lazy { DemoMeshRepository(viewModelScope) }
    private val ble by lazy { BleMeshRepository(app) }

    /** Demo mode shows a fake mesh so the app works without hardware. Turn it off in Settings. */
    val demoMode = MutableStateFlow(true)
    val displayName = MutableStateFlow("Shayan")

    private val repo: MeshRepository get() = if (demoMode.value) demo else ble

    val state: StateFlow<MeshState> = demoMode
        .flatMapLatest { if (it) demo.state else ble.state }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MeshState())

    fun setDemoMode(on: Boolean) {
        if (on == demoMode.value) return
        repo.disconnect()
        demoMode.value = on
    }

    fun startScan() = repo.startScan()
    fun stopScan() = repo.stopScan()
    fun connect(address: String) = repo.connect(address)
    fun disconnect() = repo.disconnect()
    fun send(to: Int, text: String) {
        if (text.isNotBlank()) repo.send(to, text.trim())
    }
    fun setRoutingMode(mode: RoutingMode) = repo.setRoutingMode(mode)
}
