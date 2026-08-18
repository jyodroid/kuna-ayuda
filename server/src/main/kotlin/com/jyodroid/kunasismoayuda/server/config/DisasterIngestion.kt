package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.services.DisasterIngestionService
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

/**
 * Starts the periodic disaster/report ingestion loop on the application's coroutine scope: runs once
 * immediately, then every [intervalMillis]. The loop is cancelled automatically when the app stops.
 * Only call this when a database is configured (ingestion persists).
 */
fun Application.startDisasterIngestion(intervalMillis: Long = 30 * 60 * 1000L) {
    val service by inject<DisasterIngestionService>()
    launch {
        while (isActive) {
            runCatching { service.ingestOnce() }
                .onFailure { environment.log.error("Disaster ingestion tick failed.", it) }
            delay(intervalMillis)
        }
    }
    environment.log.info("Disaster ingestion scheduled every ${intervalMillis / 60000} min.")
}
