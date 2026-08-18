package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.Instant

/**
 * A normalized disaster event ingested from an external feed (currently GDACS). Deduplicated by
 * [source] + [externalId]. [fetchedAt] is when ingestion last saw/updated it (staleness signal).
 */
data class Disaster(
    val id: Int = 0,
    val source: String,
    val externalId: String,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val country: String? = null,
    val iso3: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val magnitude: Double? = null,
    val alertLevel: String? = null,
    val severityText: String? = null,
    val eventDate: Instant? = null,
    val url: String? = null,
    val fetchedAt: Instant = Instant.now(),
)
