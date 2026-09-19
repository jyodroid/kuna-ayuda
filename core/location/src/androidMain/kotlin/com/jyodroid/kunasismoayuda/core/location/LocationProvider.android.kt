package com.jyodroid.kunasismoayuda.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import kotlin.coroutines.resume

/**
 * Holds the application context for the location provider. Set once from the Android app
 * (e.g. in MainActivity) before requesting location.
 */
object AndroidLocationContext {
    @Volatile
    var appContext: Context? = null
}

/** Bounds how long we wait for a fresh one-shot fix before falling back to last-known — an SOS must
 *  never hang waiting for GPS. */
private const val FRESH_FIX_TIMEOUT_MS = 8_000L

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

        // Prefer a FRESH one-shot fix (a rescuer needs a current position, not a stale one), bounded by
        // a timeout; fall back to the best last-known fix so an SOS is never blocked.
        val fresh = withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) {
            providers
                .firstOrNull { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
                ?.let { provider -> singleFix(lm, provider) }
        }
        val location = fresh
            ?: providers.firstNotNullOfOrNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            ?: return LocationResult.Unavailable

        return LocationResult.Granted(Coordinates(location.latitude, location.longitude))
    }

    /** One-shot current fix. Uses `getCurrentLocation` on API 30+, else the pre-30 single-update path. */
    @Suppress("MissingPermission") // permissions are checked in current() before we get here
    private suspend fun singleFix(lm: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val executor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                runCatching {
                    lm.getCurrentLocation(provider, signal, executor) { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                }.onFailure { if (cont.isActive) cont.resume(null) }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lm.removeUpdates(this)
                        if (cont.isActive) cont.resume(location)
                    }

                    override fun onProviderDisabled(provider: String) {}
                    override fun onProviderEnabled(provider: String) {}

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
                runCatching {
                    @Suppress("DEPRECATION")
                    lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }.onFailure { if (cont.isActive) cont.resume(null) }
            }
        }
}

actual fun createLocationProvider(): LocationProvider = AndroidLocationProvider()
