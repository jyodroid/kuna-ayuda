package com.jyodroid.kunasismoayuda.server.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GuideContentTest {

    @Test
    fun `colombia default with spanish tips`() {
        val r = GuideContent.response("CO", "es")
        assertEquals("CO", r.country)
        assertEquals("es", r.lang)
        assertEquals("123", r.emergency.general)
        assertTrue(r.emergency.contacts.any { it.category == "MENTAL_HEALTH" })
        // 18 tips across before/during/after/mental/animals, extracted from strings.xml.
        assertEquals(18, r.tips.sumOf { it.tips.size })
        assertEquals("Antes", r.tips.first { it.key == "before" }.label)
        // 7 tips carry a wordless storyboard (3 steps each) for low-vision / non-reading users.
        val withSteps = r.tips.flatMap { it.tips }.filter { it.steps.isNotEmpty() }
        assertEquals(7, withSteps.size)
        assertTrue(withSteps.all { it.steps.size == 3 && it.steps.all { s -> s.img.endsWith(".png") && s.caption.isNotBlank() } })
    }

    @Test
    fun `english tips + per-country numbers`() {
        val es = GuideContent.response("ES", "en")
        assertEquals("112", es.emergency.general)
        assertEquals("Before", es.tips.first { it.key == "before" }.label)
        assertEquals("105", GuideContent.response("PE", "es").emergency.general)
        assertEquals("112", GuideContent.response("IT", "es").emergency.general)
        assertEquals("112", GuideContent.response("ID", "es").emergency.general)
    }

    @Test
    fun `unknown country falls back to colombia, unknown lang to spanish`() {
        val r = GuideContent.response("XX", "fr")
        assertEquals("123", r.emergency.general)
        assertEquals("es", r.lang)
    }
}
