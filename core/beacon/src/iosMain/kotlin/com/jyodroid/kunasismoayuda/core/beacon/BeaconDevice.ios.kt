package com.jyodroid.kunasismoayuda.core.beacon

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.torchMode
import platform.AVFoundation.setTorchMode
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

/**
 * iOS beacon: camera **torch** via AVCaptureDevice and a looping **tone** via AVAudioPlayer. Torch does
 * not require a camera-permission prompt. The audio session uses the Playback category + is left active
 * so the alarm sounds even with the ring/silent switch on — the point is to be heard.
 *
 * On the Simulator there's no torch device, so [hasTorch] is false and it degrades to sound only.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosBeaconDevice : BeaconDevice {

    private val torchDevice: AVCaptureDevice? =
        AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)?.takeIf { it.hasTorch }

    private val player: AVAudioPlayer? = runCatching {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)
        AVAudioPlayer(data = ToneWav.sineWav(cycles = 400).toNSData(), error = null).apply {
            numberOfLoops = -1 // loop until paused
            prepareToPlay()
        }
    }.getOrNull()

    override val hasTorch: Boolean get() = torchDevice?.isTorchAvailable() == true
    override val hasSound: Boolean = player != null

    override fun setTorch(on: Boolean) {
        val device = torchDevice ?: return
        memScoped {
            val errorVar = alloc<ObjCObjectVar<NSError?>>()
            if (device.lockForConfiguration(errorVar.ptr)) {
                device.torchMode = if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                device.unlockForConfiguration()
            }
        }
    }

    override fun setTone(on: Boolean) {
        val p = player ?: return
        if (on) { if (!p.playing) p.play() } else p.pause()
    }

    override fun release() {
        setTorch(false)
        runCatching { player?.stop() }
    }
}

actual fun createBeaconDevice(): BeaconDevice = IosBeaconDevice()
