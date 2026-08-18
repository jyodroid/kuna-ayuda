package com.jyodroid.kunasismoayuda.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.beacon.BeaconState
import com.jyodroid.kunasismoayuda.core.beacon.EmergencyBeacon
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import com.jyodroid.kunasismoayuda.core.location.LocationProvider
import com.jyodroid.kunasismoayuda.core.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SosPhase { IDLE, LOCATING, SENDING, SENT_SOS, QUEUED_SOS, SENT_SAFE, QUEUED_SAFE, ERROR }

data class SosUiState(
    val phase: SosPhase = SosPhase.IDLE,
    val preciseLocation: Boolean = false,
    val pending: Int = 0,
)

class SosViewModel(
    private val repository: SosRepository,
    private val locationProvider: LocationProvider,
    private val beacon: EmergencyBeacon,
) : ViewModel() {

    // --- Emergency light+sound beacon (offline: local torch/speaker only) --------------------------
    /** Observable beacon state (running, seconds left, light/sound toggles). */
    val beaconState: StateFlow<BeaconState> = beacon.state
    /** What this device can actually emit — drives which controls the UI shows. */
    val canFlash: Boolean get() = beacon.hasTorch
    val canSound: Boolean get() = beacon.hasSound

    private var beaconJob: Job? = null

    /** Starts one bounded SOS-morse burst; it auto-stops (battery guard). Re-tapping restarts it. */
    fun startBeacon(light: Boolean = true, sound: Boolean = true) {
        beaconJob?.cancel()
        beaconJob = viewModelScope.launch { beacon.run(useLight = light, useSound = sound) }
    }

    /** Stops the burst early — cancellation turns the torch/tone back off in the beacon's finally. */
    fun stopBeacon() {
        beaconJob?.cancel()
        beaconJob = null
    }

    fun setBeaconLight(on: Boolean) = beacon.setLight(on)
    fun setBeaconSound(on: Boolean) = beacon.setSound(on)

    override fun onCleared() {
        stopBeacon()
        beacon.release()
        super.onCleared()
    }

    private val _phase = MutableStateFlow(SosPhase.IDLE)
    private val _precise = MutableStateFlow(false)

    val state: StateFlow<SosUiState> =
        combine(_phase, _precise, repository.pending) { phase, precise, pending ->
            SosUiState(phase = phase, preciseLocation = precise, pending = pending)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SosUiState())

    fun sendSos(region: String, message: String, phone: String) {
        _phase.value = SosPhase.LOCATING
        viewModelScope.launch {
            val coords = when (val result = locationProvider.current()) {
                is LocationResult.Granted -> result.coordinates
                else -> null
            }
            _precise.value = coords != null
            _phase.value = SosPhase.SENDING
            runCatching {
                repository.send(
                    NewSos(
                        status = SosStatus.SOS,
                        latitude = coords?.latitude,
                        longitude = coords?.longitude,
                        region = region.trim().ifBlank { null },
                        message = message.trim().ifBlank { null },
                        contactPhone = phone.trim().ifBlank { null },
                    ),
                )
            }
                .onSuccess { result ->
                    _phase.value = when (result) {
                        SosSendResult.SENT -> SosPhase.SENT_SOS
                        SosSendResult.QUEUED -> SosPhase.QUEUED_SOS
                    }
                }
                .onFailure { _phase.value = SosPhase.ERROR }
        }
    }

    fun sendSafe(region: String, phone: String) {
        _phase.value = SosPhase.SENDING
        viewModelScope.launch {
            runCatching {
                repository.send(
                    NewSos(
                        status = SosStatus.SAFE,
                        latitude = null,
                        longitude = null,
                        region = region.trim().ifBlank { null },
                        message = null,
                        contactPhone = phone.trim().ifBlank { null },
                    ),
                )
            }
                .onSuccess { result ->
                    _phase.value = when (result) {
                        SosSendResult.SENT -> SosPhase.SENT_SAFE
                        SosSendResult.QUEUED -> SosPhase.QUEUED_SAFE
                    }
                }
                .onFailure { _phase.value = SosPhase.ERROR }
        }
    }

    fun reset() {
        _phase.value = SosPhase.IDLE
        _precise.value = false
    }
}
