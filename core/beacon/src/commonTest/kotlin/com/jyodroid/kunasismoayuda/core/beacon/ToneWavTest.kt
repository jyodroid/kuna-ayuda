package com.jyodroid.kunasismoayuda.core.beacon

import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneWavTest {

    private fun ascii(bytes: ByteArray, from: Int, len: Int) =
        bytes.copyOfRange(from, from + len).map { (it.toInt() and 0xFF).toChar() }.joinToString("")

    private fun le32(bytes: ByteArray, at: Int): Int =
        (bytes[at].toInt() and 0xFF) or ((bytes[at + 1].toInt() and 0xFF) shl 8) or
            ((bytes[at + 2].toInt() and 0xFF) shl 16) or ((bytes[at + 3].toInt() and 0xFF) shl 24)

    @Test
    fun pcm_length_is_whole_cycles_for_a_seamless_loop() {
        val sampleRate = 44_100
        val freq = 1000
        val cycles = 50
        val pcm = ToneWav.pcm16Mono(freq, sampleRate, cycles)
        val expected = ((sampleRate.toDouble() / freq) * cycles).roundToInt()
        assertEquals(expected, pcm.size)
    }

    @Test
    fun wav_has_a_valid_riff_wave_header_and_matching_sizes() {
        val cycles = 20
        val wav = ToneWav.sineWav(cycles = cycles)
        assertEquals("RIFF", ascii(wav, 0, 4))
        assertEquals("WAVE", ascii(wav, 8, 4))
        assertEquals("fmt ", ascii(wav, 12, 4))
        assertEquals("data", ascii(wav, 36, 4))

        val dataSize = le32(wav, 40)
        assertEquals(wav.size - 44, dataSize, "data chunk size must match the trailing PCM bytes")
        assertEquals(36 + dataSize, le32(wav, 4), "RIFF chunk size = 36 + data size")
        assertEquals(1, (wav[22].toInt() and 0xFF), "mono = 1 channel")
        assertEquals(16, (wav[34].toInt() and 0xFF), "16-bit samples")
    }

    @Test
    fun samples_are_non_trivial() {
        val pcm = ToneWav.pcm16Mono(cycles = 10)
        assertTrue(pcm.any { it.toInt() != 0 }, "a sine tone should have non-zero samples")
    }
}
