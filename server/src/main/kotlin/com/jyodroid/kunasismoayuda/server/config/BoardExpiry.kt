package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.services.ExpiryService
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

/**
 * Runs the content-expiry sweep once on startup, then every [intervalMillis] (default: daily). Stale
 * ACTIVE aid-board posts / Lost & Found reports get auto-closed so the feed stays fresh. The loop is
 * cancelled when the app stops. Only call this when a database is configured (the sweep persists).
 */
fun Application.startExpirySweep(intervalMillis: Long = 24 * 60 * 60 * 1000L) {
    val service by inject<ExpiryService>()
    launch {
        while (isActive) {
            runCatching { service.sweepOnce() }
                .onFailure { environment.log.error("Expiry sweep tick failed.", it) }
            delay(intervalMillis)
        }
    }
    environment.log.info(
        "Expiry sweep scheduled every ${intervalMillis / 3_600_000} h (content older than ${ExpiryService.EXPIRY_DAYS} days is auto-closed).",
    )
}
