package com.jyodroid.kunasismoayuda.core.beacon

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Holds the application context for the beacon (torch access). Set once from the Android app
 * (MainActivity) before the beacon is used — mirrors `AndroidLocationContext`.
 */
object AndroidBeaconContext {
    @Volatile
    var appContext: Context? = null
}

private class AndroidBeaconDevice(private val context: Context) : BeaconDevice {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    /** Id of a camera that actually has a flash unit (usually the back camera), or null if none. */
    private val torchCameraId: String? = runCatching {
        cameraManager?.cameraIdList?.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.getOrNull()

    // ToneGenerator on the ALARM stream so the beacon is audible even with media/ring muted. Lazily
    // created (and recreated if the platform reclaims it) so we don't hold audio focus while idle.
    private var toneGenerator: ToneGenerator? = null

    override val hasTorch: Boolean =
        torchCameraId != null && context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    override val hasSound: Boolean = true

    override fun setTorch(on: Boolean) {
        val id = torchCameraId ?: return
        runCatching { cameraManager?.setTorchMode(id, on) }
            .onFailure { Log.w("Beacon", "Torch toggle failed", it) }
    }

    override fun setTone(on: Boolean) {
        if (on) {
            val gen = toneGenerator ?: runCatching {
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
            }.getOrNull()?.also { toneGenerator = it }
            // A continuous alert tone; stopped by setTone(false). Long duration since we control on/off.
            runCatching { gen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 60_000) }
        } else {
            runCatching { toneGenerator?.stopTone() }
        }
    }

    override fun release() {
        setTorch(false)
        runCatching { toneGenerator?.release() }
        toneGenerator = null
    }
}

/** No-op device when the Android context hasn't been set (should not happen in the running app). */
private object NoopBeaconDevice : BeaconDevice {
    override val hasTorch = false
    override val hasSound = false
    override fun setTorch(on: Boolean) {}
    override fun setTone(on: Boolean) {}
    override fun release() {}
}

actual fun createBeaconDevice(): BeaconDevice {
    val ctx = AndroidBeaconContext.appContext ?: return NoopBeaconDevice
    return AndroidBeaconDevice(ctx.applicationContext)
}
