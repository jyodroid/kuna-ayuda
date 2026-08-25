package com.jyodroid.kunasismoayuda.core.domain.util

/** Coarse "time ago" bucket; the UI turns it into localized text via string resources. */
enum class TimeAgoUnit { JUST_NOW, MINUTES, HOURS, DAYS }

data class TimeAgo(val unit: TimeAgoUnit, val count: Int)

/**
 * Pure relative-time bucketing for "hace N min/horas/días" labels. Buckets by the largest whole unit;
 * anything under a minute (or a future/clock-skewed timestamp) is [TimeAgoUnit.JUST_NOW].
 */
fun relativeAgo(nowMs: Long, thenMs: Long): TimeAgo {
    val diff = (nowMs - thenMs).coerceAtLeast(0)
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1L -> TimeAgo(TimeAgoUnit.JUST_NOW, 0)
        minutes < 60L -> TimeAgo(TimeAgoUnit.MINUTES, minutes.toInt())
        hours < 24L -> TimeAgo(TimeAgoUnit.HOURS, hours.toInt())
        else -> TimeAgo(TimeAgoUnit.DAYS, days.toInt())
    }
}
