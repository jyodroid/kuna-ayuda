package com.jyodroid.kunasismoayuda.core.beacon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MorseTest {

    @Test
    fun sos_cycle_has_nine_pulses_three_dots_three_dashes_three_dots() {
        val unit = 200L
        val cycle = Morse.sosCycle(unit)
        val onPulses = cycle.filter { it.on }
        assertEquals(9, onPulses.size, "SOS = S(3) O(3) S(3) = 9 lit pulses")

        val durations = onPulses.map { it.durationMillis }
        assertEquals(listOf(unit, unit, unit), durations.subList(0, 3), "S = three dots")
        assertEquals(listOf(unit * 3, unit * 3, unit * 3), durations.subList(3, 6), "O = three dashes")
        assertEquals(listOf(unit, unit, unit), durations.subList(6, 9), "S = three dots")
    }

    @Test
    fun the_emitter_is_off_for_most_of_the_cycle_to_save_battery() {
        val cycle = Morse.sosCycle(200)
        val onTime = cycle.filter { it.on }.sumOf { it.durationMillis }
        val offTime = cycle.filterNot { it.on }.sumOf { it.durationMillis }
        assertTrue(offTime > onTime, "off ($offTime ms) should exceed on ($onTime ms) — that's the battery win")
    }

    @Test
    fun unit_length_scales_the_whole_pattern() {
        val slow = Morse.sosCycle(300)
        val fast = Morse.sosCycle(100)
        assertEquals(slow.sumOf { it.durationMillis }, fast.sumOf { it.durationMillis } * 3)
    }
}
