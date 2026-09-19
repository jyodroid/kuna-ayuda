package com.jyodroid.kunasismoayuda.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.beacon.BeaconState
import com.jyodroid.kunasismoayuda.core.beacon.EmergencyBeacon
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import com.jyodroid.kunasismoayuda.core.location.Coordinates
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
    // Pre-send location readiness for the chip: null = unknown/locating, true = have a fix, false = none.
    val locationReady: Boolean? = null,
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
    private val _locationReady = MutableStateFlow<Boolean?>(null)
    // A fix acquired when the screen opens, reused on send so the SOS goes out immediately.
    private var preparedCoords: Coordinates? = null

    /** The selected country, so a public "I'm safe" check-in lands on the right country's list. */
    var country: String = "CO"
        private set

    fun setCountry(code: String) { country = code }

    val state: StateFlow<SosUiState> =
        combine(_phase, _precise, repository.pending, _locationReady) { phase, precise, pending, ready ->
            SosUiState(phase = phase, preciseLocation = precise, pending = pending, locationReady = ready)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SosUiState())

    /** Acquire a fix when the SOS screen opens, so the user sees whether coordinates will be attached
     *  BEFORE sending — and so the send itself is instant. Never blocks the UI. */
    fun prepareLocation() {
        _locationReady.value = null // "locating…"
        viewModelScope.launch {
            val coords = when (val result = locationProvider.current()) {
                is LocationResult.Granted -> result.coordinates
                else -> null
            }
            preparedCoords = coords
            _locationReady.value = coords != null
        }
    }

    fun sendSos(region: String, message: String, phone: String, name: String = "") {
        _phase.value = SosPhase.LOCATING
        viewModelScope.launch {
            // Reuse the fix acquired on open; only read again if we don't have one yet.
            val coords = preparedCoords ?: when (val result = locationProvider.current()) {
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
                        displayName = name.trim().ifBlank { null },
                        country = country,
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

    fun reset() {
        _phase.value = SosPhase.IDLE
        _precise.value = false
        _locationReady.value = null
        preparedCoords = null
    }
}
