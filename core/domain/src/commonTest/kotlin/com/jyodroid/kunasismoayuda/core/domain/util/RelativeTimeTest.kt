package com.jyodroid.kunasismoayuda.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeTest {

    private val now = 1_000_000_000_000L

    @Test
    fun under_a_minute_is_just_now() {
        assertEquals(TimeAgo(TimeAgoUnit.JUST_NOW, 0), relativeAgo(now, now - 30_000))
    }

    @Test
    fun minutes_bucket() {
        assertEquals(TimeAgo(TimeAgoUnit.MINUTES, 10), relativeAgo(now, now - 10 * 60_000))
        assertEquals(TimeAgo(TimeAgoUnit.MINUTES, 59), relativeAgo(now, now - 59 * 60_000))
    }

    @Test
    fun hours_bucket() {
        assertEquals(TimeAgo(TimeAgoUnit.HOURS, 1), relativeAgo(now, now - 60 * 60_000))
        assertEquals(TimeAgo(TimeAgoUnit.HOURS, 23), relativeAgo(now, now - 23 * 3_600_000))
    }

    @Test
    fun days_bucket() {
        assertEquals(TimeAgo(TimeAgoUnit.DAYS, 1), relativeAgo(now, now - 24 * 3_600_000))
        assertEquals(TimeAgo(TimeAgoUnit.DAYS, 3), relativeAgo(now, now - 3L * 86_400_000))
    }

    @Test
    fun future_or_skewed_timestamp_is_just_now() {
        assertEquals(TimeAgo(TimeAgoUnit.JUST_NOW, 0), relativeAgo(now, now + 5_000))
    }
}
