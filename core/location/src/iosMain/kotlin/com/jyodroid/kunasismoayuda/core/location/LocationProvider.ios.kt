package com.jyodroid.kunasismoayuda.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS location via CoreLocation. Returns a cached fix immediately when available, otherwise asks
 * `CLLocationManager` for a one-shot `requestLocation()` and waits for the delegate callback (with a
 * timeout so an SOS is never blocked waiting for GPS — it falls back to a manual region instead).
 *
 * The Info.plist of the hosting `iosApp` must declare `NSLocationWhenInUseUsageDescription`;
 * `requestWhenInUseAuthorization()` shows the system prompt when permission has not been decided.
 *
 * Compile-verified on the iOS Kotlin/Native targets; on-device behaviour needs a simulator/device run.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosLocationProvider : LocationProvider {

    // Held strongly for the app lifetime: CLLocationManager keeps its delegate weakly, so letting
    // either be collected mid-request would silently drop the callback.
    private val manager = CLLocationManager().apply { desiredAccuracy = kCLLocationAccuracyBest }
    private var delegate: CLLocationManagerDelegateProtocol? = null

    override suspend fun current(): LocationResult = withContext(Dispatchers.Main) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways,
            -> resolveLocation()

            kCLAuthorizationStatusNotDetermined -> {
                // Prompt now; the user can retry the SOS once they grant it.
                manager.requestWhenInUseAuthorization()
                LocationResult.PermissionRequired
            }

            else -> LocationResult.PermissionRequired // denied / restricted
        }
    }

    private suspend fun resolveLocation(): LocationResult {
        // A recent cached fix avoids waiting on a fresh GPS lock.
        manager.location?.let { return it.toResult() }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val cbDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManager(
                        manager: CLLocationManager,
                        didUpdateLocations: List<*>,
                    ) {
                        val loc = didUpdateLocations.lastOrNull() as? CLLocation
                        if (cont.isActive) cont.resume(loc?.toResult() ?: LocationResult.Unavailable)
                    }

                    override fun locationManager(
                        manager: CLLocationManager,
                        didFailWithError: NSError,
                    ) {
                        if (cont.isActive) cont.resume(LocationResult.Unavailable)
                    }
                }
                delegate = cbDelegate
                manager.delegate = cbDelegate
                manager.requestLocation()
                cont.invokeOnCancellation { manager.delegate = null }
            }
        } ?: LocationResult.Unavailable
    }

    private fun CLLocation.toResult(): LocationResult =
        coordinate.useContents { LocationResult.Granted(Coordinates(latitude, longitude)) }

    private companion object {
        const val LOCATION_TIMEOUT_MS = 10_000L
    }
}

actual fun createLocationProvider(): LocationProvider = IosLocationProvider()
