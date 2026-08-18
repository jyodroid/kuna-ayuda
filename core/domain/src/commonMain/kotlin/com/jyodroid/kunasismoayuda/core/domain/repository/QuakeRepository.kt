package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.Quake

/**
 * Reads recent earthquakes for Colombia from the backend (`GET /api/quakes`).
 */
interface QuakeRepository {
    suspend fun getQuakes(minMagnitude: Double = 2.5, country: String = "CO"): List<Quake>
}
