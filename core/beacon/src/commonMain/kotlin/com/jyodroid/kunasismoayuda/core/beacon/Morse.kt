package com.jyodroid.kunasismoayuda.core.beacon

/** One step of the beacon signal: the emitter is [on] (torch lit / tone sounding) for [durationMillis]. */
data class Signal(val on: Boolean, val durationMillis: Long)

/**
 * The international **SOS** distress signal in Morse — `··· ——— ···` — as a repeatable list of on/off
 * [Signal]s. We use SOS (rather than a continuous strobe) on purpose:
 *  - it is the signal search-and-rescue teams are trained to recognize, and
 *  - the emitter is **off** for most of the cycle (~60%), so it draws far less battery than a steady
 *    strobe/siren — which matters when the whole point is to survive on a dying phone.
 *
 * Standard Morse timing, in [unitMillis] units: dot = 1, dash = 3, intra-letter gap = 1, inter-letter
 * gap = 3, and a 7-unit gap after the word before it repeats.
 */
object Morse {

    /** Builds one full `··· ——— ···` cycle (including the trailing word gap) at the given unit length. */
    fun sosCycle(unitMillis: Long = 200): List<Signal> {
        val dot = Signal(on = true, durationMillis = unitMillis)
        val dash = Signal(on = true, durationMillis = unitMillis * 3)
        val symbolGap = Signal(on = false, durationMillis = unitMillis)      // between dots/dashes in a letter
        val letterGap = Signal(on = false, durationMillis = unitMillis * 3)  // between letters
        val wordGap = Signal(on = false, durationMillis = unitMillis * 7)    // before the cycle repeats

        val s = listOf(dot, symbolGap, dot, symbolGap, dot)
        val o = listOf(dash, symbolGap, dash, symbolGap, dash)

        return buildList {
            addAll(s); add(letterGap)
            addAll(o); add(letterGap)
            addAll(s); add(wordGap)
        }
    }
}
