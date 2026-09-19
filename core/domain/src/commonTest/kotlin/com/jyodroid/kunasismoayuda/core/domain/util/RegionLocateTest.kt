package com.jyodroid.kunasismoayuda.core.domain.util

import com.jyodroid.kunasismoayuda.core.domain.model.Country
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegionLocateTest {

    @Test
    fun exactMatchIsCaseAndSpaceInsensitive() {
        assertEquals("Cali", RegionLocate.match("  cali ", Country.COLOMBIA)?.name)
        assertEquals("Medellín", RegionLocate.match("Medellín", Country.COLOMBIA)?.name)
    }

    @Test
    fun substringMatchResolvesADecoratedRegionString() {
        // "Cali, Valle del Cauca" should resolve to Cali (post regions are free text).
        assertEquals("Cali", RegionLocate.match("Cali, Valle del Cauca", Country.COLOMBIA)?.name)
    }

    @Test
    fun unknownOrBlankReturnsNull() {
        assertNull(RegionLocate.match("Ciudad Inventada", Country.COLOMBIA))
        assertNull(RegionLocate.match("   ", Country.COLOMBIA))
        assertNull(RegionLocate.match(null, Country.COLOMBIA))
    }
}
