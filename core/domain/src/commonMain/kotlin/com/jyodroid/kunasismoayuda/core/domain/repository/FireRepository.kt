package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.Fire

/**
 * Reads recent active wildfires for a country from the backend (`GET /api/fires?country=`) —
 * the wildfire analog of [QuakeRepository].
 */
interface FireRepository {
    suspend fun getFires(country: String = "CO"): List<Fire>
}
