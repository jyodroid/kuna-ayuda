package com.jyodroid.kunasismoayuda.core.beacon

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmergencyBeaconTest {

    private class FakeDevice(
        override val hasTorch: Boolean = true,
        override val hasSound: Boolean = true,
    ) : BeaconDevice {
        var torchOn = false
        var toneOn = false
        var torchOnCount = 0
        var released = false
        override fun setTorch(on: Boolean) { if (on && !torchOn) torchOnCount++; torchOn = on }
        override fun setTone(on: Boolean) { toneOn = on }
        override fun release() { released = true }
    }

    @Test
    fun auto_stops_after_the_bounded_burst_and_leaves_hardware_off() = runTest {
        val device = FakeDevice()
        val beacon = EmergencyBeacon(device, unitMillis = 100, maxDurationMillis = 3_000)

        beacon.run(useLight = true, useSound = true) // suspends for the whole burst under virtual time

        assertFalse(device.torchOn, "torch must be off after the burst")
        assertFalse(device.toneOn, "tone must be off after the burst")
        assertFalse(beacon.state.value.running)
        assertEquals(0, beacon.state.value.secondsRemaining)
        assertTrue(device.torchOnCount > 0, "the torch should have flashed at least once")
    }

    @Test
    fun cancelling_the_job_stops_early_and_turns_the_torch_off() = runTest {
        val device = FakeDevice()
        val beacon = EmergencyBeacon(device, unitMillis = 100, maxDurationMillis = 30_000)

        val job = launch { beacon.run() }
        testScheduler.advanceTimeBy(250) // into the first lit pulses
        testScheduler.runCurrent()
        assertTrue(beacon.state.value.running)

        job.cancel()
        testScheduler.advanceUntilIdle()

        assertFalse(device.torchOn, "cancellation must turn the torch off (battery safety)")
        assertFalse(device.toneOn)
        assertFalse(beacon.state.value.running)
    }

    @Test
    fun does_nothing_when_the_device_can_neither_flash_nor_sound() = runTest {
        val device = FakeDevice(hasTorch = false, hasSound = false)
        val beacon = EmergencyBeacon(device, unitMillis = 100, maxDurationMillis = 3_000)

        beacon.run()

        assertFalse(beacon.state.value.running)
        assertEquals(0, device.torchOnCount)
    }

    @Test
    fun sound_toggle_off_keeps_the_light_flashing_without_tone() = runTest {
        val device = FakeDevice()
        val beacon = EmergencyBeacon(device, unitMillis = 100, maxDurationMillis = 3_000)

        beacon.run(useLight = true, useSound = false)

        assertTrue(device.torchOnCount > 0, "light channel still flashes")
        assertFalse(device.toneOn, "tone stayed off the whole time")
    }
}
