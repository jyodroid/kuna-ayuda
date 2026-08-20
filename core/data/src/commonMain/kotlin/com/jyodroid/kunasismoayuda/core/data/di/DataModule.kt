package com.jyodroid.kunasismoayuda.core.data.di

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.remote.AdminApi
import com.jyodroid.kunasismoayuda.core.data.remote.AuthApi
import com.jyodroid.kunasismoayuda.core.data.remote.BoardApi
import com.jyodroid.kunasismoayuda.core.data.remote.HttpClientFactory
import com.jyodroid.kunasismoayuda.core.data.remote.FireApi
import com.jyodroid.kunasismoayuda.core.data.remote.QuakeApi
import com.jyodroid.kunasismoayuda.core.data.remote.SearchApi
import com.jyodroid.kunasismoayuda.core.data.remote.ShelterApi
import com.jyodroid.kunasismoayuda.core.data.offline.SosOutbox
import com.jyodroid.kunasismoayuda.core.data.offline.SosOutboxStore
import com.jyodroid.kunasismoayuda.core.data.remote.SosApi
import com.jyodroid.kunasismoayuda.core.data.repository.AdminRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.AuthRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.FireRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.QuakeRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.ResourceBoardRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.SearchRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.ShelterRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.repository.SosRepositoryImpl
import com.jyodroid.kunasismoayuda.core.data.settings.CountryStore
import com.jyodroid.kunasismoayuda.core.domain.repository.AdminRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.AuthRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.FireRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.QuakeRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.SearchRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.ShelterRepository
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin bindings for the data layer. Kept here so consumers (e.g. composeApp) don't need to
 * reference Ktor types directly.
 */
val dataModule = module {
    // A 401 on any token-bearing request expires the moderator session (auto-logout on JWT expiry).
    single { HttpClientFactory.create(onUnauthorized = { get<SessionManager>().expire() }) }
    // Persists the chosen country (survives restarts); null until the first-run picker resolves.
    single { CountryStore() }
    // Device-local record of board posts this device owns (post id → owner secret), for #4 resolve.
    single { com.jyodroid.kunasismoayuda.core.data.settings.PostOwnershipStore() }
    single { QuakeApi(get()) }
    single<QuakeRepository> { QuakeRepositoryImpl(get()) }
    single { FireApi(get()) }
    single<FireRepository> { FireRepositoryImpl(get()) }
    single { ShelterApi(get()) }
    single<ShelterRepository> { ShelterRepositoryImpl(get(), get()) }
    single { BoardApi(get()) }
    single { SearchApi(get()) }
    single<SearchRepository> { SearchRepositoryImpl(get(), get()) }
    // Shared in-memory moderator session (token never touches disk).
    single { SessionManager() }
    single { AuthApi(get()) }
    single { AdminApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<AdminRepository> { AdminRepositoryImpl(get(), get()) }
    single<ResourceBoardRepository> { ResourceBoardRepositoryImpl(get(), get(), get()) }
    single { SosApi(get()) }
    // App-lifetime scope for the offline outbox's background retry loop.
    single(named("outboxScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { SosOutboxStore() }
    single { SosOutbox(api = get(), store = get(), scope = get(named("outboxScope"))) }
    single<SosRepository> { SosRepositoryImpl(get(), get(), get()) }
}
