package com.jyodroid.kunasismoayuda.core.beacon

/**
 * Low-level, platform-specific access to the two things an emergency beacon needs: the camera **torch**
 * (flashlight) and a loud **tone**. The shared [EmergencyBeacon] drives these on/off to play the SOS
 * Morse pattern, so the timing/loop/auto-stop logic stays in commonMain (and unit-testable) while each
 * platform only implements the raw primitives.
 *
 * Implementations must be safe to call [setTorch]/[setTone] repeatedly and must leave the hardware OFF
 * after [release] — a beacon that fails to turn the torch back off would silently drain the battery.
 */
interface BeaconDevice {
    /** Whether this device can light a camera torch (false on most desktops / the iOS Simulator). */
    val hasTorch: Boolean

    /** Whether this device can emit a tone. */
    val hasSound: Boolean

    /** Turn the torch on/off. No-op when [hasTorch] is false. */
    fun setTorch(on: Boolean)

    /** Start/stop the alarm tone. No-op when [hasSound] is false. */
    fun setTone(on: Boolean)

    /** Release any held resources (tone player, camera lock) and ensure the torch is OFF. */
    fun release()
}

/** Creates the platform's [BeaconDevice]. */
expect fun createBeaconDevice(): BeaconDevice
