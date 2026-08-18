package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.Instant

/**
 * A humanitarian situation report ingested from an external feed (currently ReliefWeb). Deduplicated
 * by [source] + [externalId]. [fetchedAt] records when ingestion last saw/updated it.
 */
data class DisasterReport(
    val id: Int = 0,
    val source: String,
    val externalId: String,
    val title: String,
    val body: String? = null,
    val orgSource: String? = null,
    val country: String? = null,
    val disasterType: String? = null,
    val url: String? = null,
    val publishedAt: Instant? = null,
    val fetchedAt: Instant = Instant.now(),
)
