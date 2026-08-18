package com.jyodroid.kunasismoayuda.core.beacon

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * Desktop (JVM) beacon: **no torch** (laptops have no camera flash we can drive portably), but it can
 * still sound the alarm tone via `javax.sound` — useful as a whistle substitute. The tone is the shared
 * [ToneWav] buffer, looped continuously while a pulse is "on".
 */
private class JvmBeaconDevice : BeaconDevice {

    private val clip: Clip? = runCatching {
        val wav = ToneWav.sineWav(cycles = 400) // a longer buffer → fewer loop restarts
        val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(wav))
        AudioSystem.getClip().apply { open(stream) }
    }.getOrNull()

    override val hasTorch: Boolean = false
    override val hasSound: Boolean = clip != null

    override fun setTorch(on: Boolean) { /* no flashlight on desktop */ }

    override fun setTone(on: Boolean) {
        val c = clip ?: return
        if (on) {
            if (!c.isRunning) { c.framePosition = 0; c.loop(Clip.LOOP_CONTINUOUSLY) }
        } else {
            c.stop()
        }
    }

    override fun release() {
        runCatching { clip?.stop(); clip?.close() }
    }
}

actual fun createBeaconDevice(): BeaconDevice = JvmBeaconDevice()
