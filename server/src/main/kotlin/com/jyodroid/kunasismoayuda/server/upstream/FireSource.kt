package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse

/**
 * A source of recent active wildfires within a bounding box — the wildfire analog of [QuakeSource].
 * Implementations reuse the shared [BBox] (and thus [CountryBBoxes]) so fires are scoped per country
 * exactly like quakes.
 */
interface FireSource {
    val name: String
    suspend fun recentFires(bbox: BBox): List<FireResponse>
}
