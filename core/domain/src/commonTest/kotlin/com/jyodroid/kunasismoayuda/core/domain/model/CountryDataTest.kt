package com.jyodroid.kunasismoayuda.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CountryDataTest {

    @Test
    fun country_from_code_is_case_insensitive_with_fallback() {
        assertEquals(Country.SPAIN, Country.fromCode("ES"))
        assertEquals(Country.SPAIN, Country.fromCode("es"))
        assertEquals(Country.DEFAULT, Country.fromCode("zz"))
    }

    @Test
    fun general_emergency_numbers_are_correct_per_country() {
        assertEquals("123", CountryEmergency.generalNumber(Country.COLOMBIA))
        assertEquals("112", CountryEmergency.generalNumber(Country.INDONESIA))
        assertEquals("112", CountryEmergency.generalNumber(Country.SPAIN))
        assertEquals("112", CountryEmergency.generalNumber(Country.ITALY))
    }

    @Test
    fun every_country_has_a_non_empty_directory_with_a_mental_health_line() {
        Country.entries.forEach { country ->
            val contacts = CountryEmergency.contacts(country)
            assertTrue(contacts.isNotEmpty(), "${country.code} directory is empty")
            assertTrue(
                contacts.any { it.category == EmergencyCategory.MENTAL_HEALTH },
                "${country.code} has no mental-health line",
            )
            contacts.forEach { assertTrue(it.phone.isNotBlank(), "${country.code} has a blank phone") }
        }
    }

    @Test
    fun region_lists_are_non_empty_for_every_country_and_colombia_is_full() {
        Country.entries.forEach { country ->
            assertTrue(CountryRegions.of(country).isNotEmpty(), "${country.code} has no regions")
        }
        // Colombia was expanded to all department capitals for the wildfire place labels.
        assertTrue(
            ColombiaRegions.all.size >= 32,
            "expected all Colombian dept capitals, was ${ColombiaRegions.all.size}",
        )
        val names = ColombiaRegions.all.map { it.name }
        assertEquals(names.size, names.toSet().size, "duplicate Colombian region names")
    }

    @Test
    fun enum_fromRaw_is_case_insensitive_with_fallback() {
        assertEquals(PostKind.OFFER, PostKind.fromRaw("offer"))
        assertEquals(PostKind.OFFER, PostKind.fromRaw("OFFER"))
        assertEquals(PostKind.REQUEST, PostKind.fromRaw("garbage"))
        assertEquals(ResourceType.WATER, ResourceType.fromRaw("water"))
        assertEquals(ResourceType.OTHER, ResourceType.fromRaw("nope"))
    }
}
