package com.jyodroid.kunasismoayuda.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class FireIntensityTest {

    private fun fire(frp: Double?, confidence: String? = null) = Fire(
        id = "f", timeMillis = 0, latitude = 0.0, longitude = 0.0,
        brightnessK = null, frpMw = frp, confidence = confidence, daynight = null,
        source = "TEST", place = null,
    )

    @Test
    fun frp_drives_intensity_when_present() {
        assertEquals(FireIntensity.HIGH, fire(frp = 150.0).intensity)
        assertEquals(FireIntensity.HIGH, fire(frp = 100.0).intensity)
        assertEquals(FireIntensity.MODERATE, fire(frp = 25.0).intensity)
        assertEquals(FireIntensity.MODERATE, fire(frp = 20.0).intensity)
        assertEquals(FireIntensity.LOW, fire(frp = 5.0).intensity)
    }

    @Test
    fun gdacs_alert_level_used_when_no_frp() {
        assertEquals(FireIntensity.HIGH, fire(frp = null, confidence = "red").intensity)
        assertEquals(FireIntensity.HIGH, fire(frp = null, confidence = "RED").intensity)
        assertEquals(FireIntensity.MODERATE, fire(frp = null, confidence = "orange").intensity)
        assertEquals(FireIntensity.LOW, fire(frp = null, confidence = "green").intensity)
        assertEquals(FireIntensity.LOW, fire(frp = null, confidence = null).intensity)
    }
}
