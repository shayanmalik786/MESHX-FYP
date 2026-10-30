package com.meshx.app.data

import com.meshx.app.model.MeshState
import com.meshx.app.model.RoutingMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Source of mesh data for the UI. Two implementations:
 *  - DemoMeshRepository: fake 6-node mesh, so the app can be shown without hardware.
 *  - BleMeshRepository: talks to a real MeshX node over Bluetooth Low Energy.
 */
interface MeshRepository {
    val state: StateFlow<MeshState>
    fun startScan()
    fun stopScan()
    fun connect(address: String)
    fun disconnect()
    fun send(to: Int, text: String)
    fun setRoutingMode(mode: RoutingMode)
}
