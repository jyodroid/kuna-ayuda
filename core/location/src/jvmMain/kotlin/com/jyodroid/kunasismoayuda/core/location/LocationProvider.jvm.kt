package com.jyodroid.kunasismoayuda.core.location

/** Desktop has no GPS; the SOS flow falls back to a manually entered region. */
private class JvmLocationProvider : LocationProvider {
    override suspend fun current(): LocationResult = LocationResult.Unavailable
}

actual fun createLocationProvider(): LocationProvider = JvmLocationProvider()
