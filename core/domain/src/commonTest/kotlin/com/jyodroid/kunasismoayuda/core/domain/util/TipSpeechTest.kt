package com.jyodroid.kunasismoayuda.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TipSpeechTest {

    @Test
    fun joins_title_and_body_with_a_period() {
        assertEquals(
            "Prepara un kit. Agua, comida y linterna",
            tipSpeechText("Prepara un kit", "Agua, comida y linterna"),
        )
    }

    @Test
    fun keeps_existing_sentence_punctuation_without_doubling() {
        assertEquals(
            "¿Tienes un plan? Acuerda un punto de encuentro",
            tipSpeechText("¿Tienes un plan?", "Acuerda un punto de encuentro"),
        )
    }

    @Test
    fun trims_and_tolerates_empty_parts() {
        assertEquals("Solo titulo", tipSpeechText("  Solo titulo  ", "   "))
        assertEquals("Solo cuerpo", tipSpeechText("", "Solo cuerpo"))
        assertEquals("", tipSpeechText("  ", ""))
    }
}
