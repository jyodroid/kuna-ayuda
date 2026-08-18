package com.jyodroid.kunasismoayuda.core.location

/** A geographic coordinate. */
data class Coordinates(val latitude: Double, val longitude: Double)

/** Outcome of a location request. */
sealed interface LocationResult {
    data class Granted(val coordinates: Coordinates) : LocationResult
    /** Permission not granted yet — the UI should still allow sending without precise coordinates. */
    data object PermissionRequired : LocationResult
    /** No location available on this platform/device (e.g. desktop, or no fix yet). */
    data object Unavailable : LocationResult
}

/** Reads the device's current location. Implemented per platform. */
interface LocationProvider {
    suspend fun current(): LocationResult
}

/** Factory for the platform's [LocationProvider]. */
expect fun createLocationProvider(): LocationProvider
