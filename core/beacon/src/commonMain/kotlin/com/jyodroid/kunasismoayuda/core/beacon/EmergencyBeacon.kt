package com.jyodroid.kunasismoayuda.core.beacon

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Observable state of the [EmergencyBeacon]. */
data class BeaconState(
    val running: Boolean = false,
    /** Whole seconds left before the burst auto-stops (0 when idle). */
    val secondsRemaining: Int = 0,
    /** Whether the light channel is currently enabled (user-toggleable). */
    val light: Boolean = true,
    /** Whether the sound channel is currently enabled (user-toggleable). */
    val sound: Boolean = true,
)

/**
 * Plays the international **SOS** distress signal (`··· ——— ···`, see [Morse]) on the device torch and
 * tone, for a bounded burst, then **auto-stops** — a deliberate battery safeguard so the beacon can
 * never be left draining a phone someone's survival may depend on. Fully offline: it drives local
 * hardware only, no network involved.
 *
 * The whole loop lives here in commonMain so it's shared and testable; the platform [BeaconDevice] only
 * flips the raw torch/tone. Call [run] from a coroutine scope (e.g. `viewModelScope.launch`); **cancel
 * that job to stop early** — the `finally` block guarantees the torch and tone are turned back off even
 * on cancellation.
 */
class EmergencyBeacon(
    private val device: BeaconDevice,
    private val unitMillis: Long = 200,
    private val maxDurationMillis: Long = 30_000,
) {
    val hasTorch: Boolean get() = device.hasTorch
    val hasSound: Boolean get() = device.hasSound

    private val _state = MutableStateFlow(BeaconState())
    val state: StateFlow<BeaconState> = _state.asStateFlow()

    /** Live toggles — safe to call before or during [run]; the loop reads them at each pulse. */
    fun setLight(on: Boolean) = _state.update { it.copy(light = on) }
    fun setSound(on: Boolean) = _state.update { it.copy(sound = on) }

    /**
     * Runs one bounded SOS burst, driving the torch/tone, until [maxDurationMillis] elapses or the
     * caller cancels. Suspends for the burst's duration. If neither channel can actually emit anything
     * (no torch and no sound, or both toggled off), it returns immediately without changing state.
     */
    suspend fun run(useLight: Boolean = true, useSound: Boolean = true) {
        _state.update { it.copy(light = useLight, sound = useSound) }
        val canEmit = (useLight && device.hasTorch) || (useSound && device.hasSound)
        if (!canEmit) return

        val cycle = Morse.sosCycle(unitMillis)
        var elapsed = 0L
        _state.update { it.copy(running = true, secondsRemaining = secondsLeft(0)) }
        try {
            while (elapsed < maxDurationMillis) {
                for (signal in cycle) {
                    val step = minOf(signal.durationMillis, maxDurationMillis - elapsed)
                    if (step <= 0) break
                    val s = _state.value
                    if (signal.on) {
                        device.setTorch(s.light && device.hasTorch)
                        device.setTone(s.sound && device.hasSound)
                    } else {
                        device.setTorch(false)
                        device.setTone(false)
                    }
                    delay(step)
                    elapsed += step
                    _state.update { it.copy(secondsRemaining = secondsLeft(elapsed)) }
                    if (elapsed >= maxDurationMillis) break
                }
            }
        } finally {
            // Runs on normal completion AND on cancellation (no suspension here) → hardware always off.
            device.setTorch(false)
            device.setTone(false)
            _state.update { it.copy(running = false, secondsRemaining = 0) }
        }
    }

    /** Releases the underlying device resources (call when the owner is disposed). */
    fun release() = device.release()

    private fun secondsLeft(elapsed: Long): Int {
        val remaining = maxDurationMillis - elapsed
        if (remaining <= 0) return 0
        return ((remaining + 999) / 1000).toInt() // round up
    }
}
