package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.repositories.DisasterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ReportRepository
import com.jyodroid.kunasismoayuda.server.upstream.GdacsSource
import com.jyodroid.kunasismoayuda.server.upstream.ReliefWebSource
import org.slf4j.LoggerFactory

/**
 * Pulls the external humanitarian feeds and UPSERTs them into Postgres. Each source is isolated: a
 * failure in one (network, schema drift) is logged and does not abort the other. Called on startup
 * and then on a schedule (see `config/DisasterIngestion.kt`).
 */
class DisasterIngestionService(
    private val gdacs: GdacsSource,
    private val reliefWeb: ReliefWebSource,
    private val disasterRepo: DisasterRepository,
    private val reportRepo: ReportRepository,
) {
    private val logger = LoggerFactory.getLogger(DisasterIngestionService::class.java)

    data class IngestResult(val disasters: Int, val reports: Int)

    suspend fun ingestOnce(): IngestResult {
        val disasters = runCatching { gdacs.recentEarthquakes() }
            .onFailure { logger.warn("GDACS fetch failed; skipping this tick.", it) }
            .getOrDefault(emptyList())
        val savedDisasters = runCatching { disasterRepo.upsert(disasters) }
            .onFailure { logger.error("Persisting GDACS disasters failed.", it) }
            .getOrDefault(0)

        val reports = runCatching { reliefWeb.recentReports() }
            .onFailure { logger.warn("ReliefWeb fetch failed; skipping this tick.", it) }
            .getOrDefault(emptyList())
        val savedReports = runCatching { reportRepo.upsert(reports) }
            .onFailure { logger.error("Persisting ReliefWeb reports failed.", it) }
            .getOrDefault(0)

        logger.info("Ingestion complete: disasters={} reports={}", savedDisasters, savedReports)
        return IngestResult(savedDisasters, savedReports)
    }
}
