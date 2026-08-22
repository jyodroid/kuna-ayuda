package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.services.ExpiryService
import com.jyodroid.kunasismoayuda.server.services.PurgeService
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

/**
 * Runs the retention sweep once on startup, then every [intervalMillis] (default: daily): stale ACTIVE
 * content is auto-**closed** at [ExpiryService.EXPIRY_DAYS] (30d), then permanently **deleted** at
 * [PurgeService.PURGE_DAYS] (60d). The loop is cancelled when the app stops. Only call this when a
 * database is configured (both persist).
 */
fun Application.startExpirySweep(intervalMillis: Long = 24 * 60 * 60 * 1000L) {
    val expiry by inject<ExpiryService>()
    val purge by inject<PurgeService>()
    launch {
        while (isActive) {
            runCatching { expiry.sweepOnce() }
                .onFailure { environment.log.error("Expiry sweep tick failed.", it) }
            runCatching { purge.purgeOnce() }
                .onFailure { environment.log.error("Purge tick failed.", it) }
            delay(intervalMillis)
        }
    }
    environment.log.info(
        "Retention scheduled every ${intervalMillis / 3_600_000} h " +
            "(close >${ExpiryService.EXPIRY_DAYS}d, hard-delete >${PurgeService.PURGE_DAYS}d).",
    )
}
