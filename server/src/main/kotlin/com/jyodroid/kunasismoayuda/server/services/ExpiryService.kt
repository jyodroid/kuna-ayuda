package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Auto-archives stale user content so the feed stays fresh and trustworthy: aid-board posts and
 * Lost & Found reports still ACTIVE after [EXPIRY_DAYS] days are closed. Closing reuses each repo's
 * close() semantics — board posts also get their (public) contact scrubbed. Called on startup and
 * then daily (see `config/BoardExpiry.kt`). Each sweep is isolated: one failing doesn't abort the other.
 */
class ExpiryService(
    private val boardRepo: ResourceBoardRepository,
    private val searchRepo: SearchRepository,
) {
    private val logger = LoggerFactory.getLogger(ExpiryService::class.java)

    data class ExpiryResult(val boardPosts: Int, val searchReports: Int)

    fun sweepOnce(): ExpiryResult {
        val cutoff = LocalDateTime.now().minusDays(EXPIRY_DAYS.toLong())
        val posts = runCatching { boardRepo.expireOlderThan(cutoff) }
            .onFailure { logger.error("Board expiry sweep failed.", it) }
            .getOrDefault(0)
        val reports = runCatching { searchRepo.expireOlderThan(cutoff) }
            .onFailure { logger.error("Search expiry sweep failed.", it) }
            .getOrDefault(0)
        if (posts > 0 || reports > 0) {
            logger.info("Expiry sweep closed board={} search={} (older than {} days).", posts, reports, EXPIRY_DAYS)
        }
        return ExpiryResult(posts, reports)
    }

    companion object {
        /** Content ACTIVE longer than this is auto-closed. Tunable in one place. */
        const val EXPIRY_DAYS = 30
    }
}
