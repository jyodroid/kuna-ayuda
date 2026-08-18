package com.jyodroid.kunasismoayuda.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager

/**
 * Holds the application context for the location provider. Set once from the Android app
 * (e.g. in MainActivity) before requesting location.
 */
object AndroidLocationContext {
    @Volatile
    var appContext: Context? = null
}

private class AndroidLocationProvider : LocationProvider {
    override suspend fun current(): LocationResult {
        val ctx = AndroidLocationContext.appContext ?: return LocationResult.Unavailable

        val fine = ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return LocationResult.PermissionRequired

        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return LocationResult.Unavailable

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        val location = providers.firstNotNullOfOrNull { provider ->
            runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
        } ?: return LocationResult.Unavailable

        return LocationResult.Granted(Coordinates(location.latitude, location.longitude))
    }
}

actual fun createLocationProvider(): LocationProvider = AndroidLocationProvider()
