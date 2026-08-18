package com.jyodroid.kunasismoayuda.core.beacon

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Generates a short, seamlessly-loopable **sine-wave tone** as a 16-bit PCM mono WAV byte array. Used by
 * the iOS and Desktop beacon actuals (which loop this buffer while a tone segment is "on"); Android uses
 * its native `ToneGenerator` instead. The default ~1 kHz carries well outdoors and through walls.
 *
 * The buffer is an **integer number of whole cycles**, so looping it produces no click at the seam.
 */
object ToneWav {

    /** ~1 kHz is a good balance of loudness/penetration for a rescue tone without being shrill. */
    const val DEFAULT_FREQUENCY_HZ = 1000
    const val DEFAULT_SAMPLE_RATE = 44_100

    /** 16-bit mono PCM samples for [cycles] whole periods of a sine at [frequencyHz]. */
    fun pcm16Mono(
        frequencyHz: Int = DEFAULT_FREQUENCY_HZ,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        cycles: Int = 200,
        amplitude: Double = 0.9, // near full-scale so the alarm is loud
    ): ShortArray {
        val samplesPerCycle = sampleRate.toDouble() / frequencyHz
        val total = (samplesPerCycle * cycles).roundToInt()
        val peak = (Short.MAX_VALUE * amplitude)
        return ShortArray(total) { i ->
            (sin(2.0 * PI * frequencyHz * i / sampleRate) * peak).roundToInt().toShort()
        }
    }

    /** Wraps [pcm16Mono] in a canonical 44-byte RIFF/WAVE header → a self-contained playable WAV. */
    fun sineWav(
        frequencyHz: Int = DEFAULT_FREQUENCY_HZ,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        cycles: Int = 200,
        amplitude: Double = 0.9,
    ): ByteArray {
        val samples = pcm16Mono(frequencyHz, sampleRate, cycles, amplitude)
        val dataSize = samples.size * 2
        val bytesPerSample = 2
        val channels = 1
        val byteRate = sampleRate * channels * bytesPerSample

        val out = ArrayList<Byte>(44 + dataSize)
        fun ascii(s: String) = s.forEach { out.add(it.code.toByte()) }
        fun le16(v: Int) { out.add((v and 0xFF).toByte()); out.add((v ushr 8 and 0xFF).toByte()) }
        fun le32(v: Int) {
            out.add((v and 0xFF).toByte()); out.add((v ushr 8 and 0xFF).toByte())
            out.add((v ushr 16 and 0xFF).toByte()); out.add((v ushr 24 and 0xFF).toByte())
        }

        ascii("RIFF"); le32(36 + dataSize); ascii("WAVE")
        ascii("fmt "); le32(16); le16(1) /* PCM */; le16(channels)
        le32(sampleRate); le32(byteRate); le16(channels * bytesPerSample) /* block align */; le16(16) /* bits */
        ascii("data"); le32(dataSize)
        for (s in samples) { le16(s.toInt() and 0xFFFF) }

        return out.toByteArray()
    }
}
