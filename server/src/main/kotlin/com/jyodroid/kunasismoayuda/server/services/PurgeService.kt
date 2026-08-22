package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Data minimization: **permanently deletes** user content older than [PURGE_DAYS] days. This runs
 * alongside [ExpiryService] — content is first soft-closed at `EXPIRY_DAYS` (30d) and then hard-deleted
 * here at 60d, so a post lives ACTIVE → CLOSED+scrubbed → gone. Aid-board posts, Lost & Found reports
 * (**and their photos**), and SOS reports are purged; **official help points (shelters) are kept.**
 * Called on startup and then daily (see `config/BoardExpiry.kt`). Each delete is isolated so one
 * failing table doesn't abort the rest.
 */
class PurgeService(
    private val boardRepo: ResourceBoardRepository,
    private val searchRepo: SearchRepository,
    private val sosRepo: SosRepository,
    private val photoRepo: PhotoRepository,
) {
    private val logger = LoggerFactory.getLogger(PurgeService::class.java)

    data class PurgeResult(val boardPosts: Int, val searchReports: Int, val sosReports: Int, val orphanPhotos: Int)

    fun purgeOnce(): PurgeResult {
        val cutoff = LocalDateTime.now().minusDays(PURGE_DAYS.toLong())
        val posts = runCatching { boardRepo.deleteOlderThan(cutoff) }
            .onFailure { logger.error("Board purge failed.", it) }.getOrDefault(0)
        val reports = runCatching { searchRepo.deleteOlderThan(cutoff) }
            .onFailure { logger.error("Search purge failed.", it) }.getOrDefault(0)
        val sos = runCatching { sosRepo.deleteOlderThan(cutoff) }
            .onFailure { logger.error("SOS purge failed.", it) }.getOrDefault(0)
        val orphans = runCatching { photoRepo.deleteOrphansOlderThan(cutoff) }
            .onFailure { logger.error("Orphan-photo purge failed.", it) }.getOrDefault(0)
        if (posts > 0 || reports > 0 || sos > 0 || orphans > 0) {
            logger.info(
                "Purge deleted board={} search={} sos={} orphanPhotos={} (older than {} days).",
                posts, reports, sos, orphans, PURGE_DAYS,
            )
        }
        return PurgeResult(posts, reports, sos, orphans)
    }

    companion object {
        /** User content older than this is permanently deleted. Tunable in one place. */
        const val PURGE_DAYS = 60
    }
}
