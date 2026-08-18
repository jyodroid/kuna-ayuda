package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import kotlin.math.abs

/**
 * Finds the réplicas (aftershocks / nearby events) of a main quake within the current feed.
 *
 * The upstream feed carries no "is-aftershock" flag, so this is a **spatial + temporal cluster
 * heuristic**: events other than [main] that fall within [radiusKm] of its epicentre and within
 * [windowMs] of its time, returned most-recent first. It's an approximation useful for context, not
 * a seismological classification. Thresholds are deliberately simple constants, easy to tune.
 */
class IdentifyAftershocksUseCase(
    private val radiusKm: Double = 120.0,
    private val windowMs: Long = 3L * 24 * 60 * 60 * 1000, // ±3 days
) {
    operator fun invoke(main: Quake, all: List<Quake>): List<Quake> =
        all.asSequence()
            .filter { it.id != main.id }
            .filter { abs(it.timeMillis - main.timeMillis) <= windowMs }
            .filter { Geo.distanceKm(main.latitude, main.longitude, it.latitude, it.longitude) <= radiusKm }
            .sortedByDescending { it.timeMillis }
            .toList()
}
