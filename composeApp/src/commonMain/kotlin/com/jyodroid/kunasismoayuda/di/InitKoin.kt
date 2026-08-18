package com.jyodroid.kunasismoayuda.di

import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

private var koinStarted = false

/**
 * Starts Koin once per process. Safe to call from multiple entry points — it no-ops if already
 * started (e.g. Android Activity recreation or repeated iOS controller creation). A process-level
 * flag is used rather than `GlobalContext`, which is not exposed in Koin's Kotlin/Native API.
 *
 * Also resumes the offline SOS outbox so any report left unsent from a previous session starts
 * retrying immediately at launch, not only when the user reopens the SOS screen.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    if (koinStarted) return
    koinStarted = true
    val koin = startKoin {
        config?.invoke(this)
        modules(appModule)
    }.koin
    koin.get<SosRepository>().start()
}
