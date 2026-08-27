package com.jyodroid.kunasismoayuda.core.domain.util

import com.jyodroid.kunasismoayuda.core.domain.model.Quake

/** 24 hours in milliseconds — the "Hoy" / today threshold. */
private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L

/**
 * The single most recent quake in [quakes] (by [Quake.timeMillis]), returned **only** when it is fresh
 * — within [windowMs] of [nowMs] — and is not [excludeId].
 *
 * The exclusion keeps the Overview "Actividad reciente" bubble from duplicating the headline: the
 * headline is the strongest quake, and when the freshest quake IS that strongest one there is nothing
 * new to surface, so this returns null. Returns null for an empty feed or when the freshest event is
 * older than the window (i.e. no recent activity to highlight).
 */
fun mostRecentQuake(
    quakes: List<Quake>,
    nowMs: Long,
    windowMs: Long,
    excludeId: String?,
): Quake? {
    val recent = quakes.maxByOrNull { it.timeMillis } ?: return null
    if (recent.id == excludeId) return null
    return recent.takeIf { nowMs - it.timeMillis <= windowMs }
}

/**
 * Whether [thenMs] is within the last 24 hours of [nowMs] — a timezone-free "today" used for the "Hoy"
 * badge. A future/clock-skewed timestamp (negative delta) counts as today.
 */
fun isToday(nowMs: Long, thenMs: Long): Boolean = nowMs - thenMs <= ONE_DAY_MS
