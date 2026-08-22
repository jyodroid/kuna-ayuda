package com.jyodroid.kunasismoayuda.server.di

import com.jyodroid.kunasismoayuda.server.ai.AnthropicClient
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ApiUsageRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.AuditRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.DisasterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ReportRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ShelterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.AdminUserRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.ApiUsageRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.AuditRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.ClassifyCacheRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.DisasterRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.PhotoRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.ReportRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.ResourceBoardRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.SearchReportRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.ShelterRepositoryImpl
import com.jyodroid.kunasismoayuda.server.infrastructure.repositories.SosRepositoryImpl
import com.jyodroid.kunasismoayuda.server.services.AdminService
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.AuthService
import com.jyodroid.kunasismoayuda.server.services.DisasterIngestionService
import com.jyodroid.kunasismoayuda.server.services.ExpiryService
import com.jyodroid.kunasismoayuda.server.services.PurgeService
import com.jyodroid.kunasismoayuda.server.services.FireService
import com.jyodroid.kunasismoayuda.server.services.UsageLimiter
import com.jyodroid.kunasismoayuda.server.services.QuakeService
import com.jyodroid.kunasismoayuda.server.services.ResourceBoardService
import com.jyodroid.kunasismoayuda.server.services.SearchService
import com.jyodroid.kunasismoayuda.server.services.ShelterService
import com.jyodroid.kunasismoayuda.server.services.SosService
import com.jyodroid.kunasismoayuda.server.upstream.FactCheckClient
import com.jyodroid.kunasismoayuda.server.upstream.GdacsSource
import com.jyodroid.kunasismoayuda.server.upstream.HttpClientFactory
import com.jyodroid.kunasismoayuda.server.upstream.FireSource
import com.jyodroid.kunasismoayuda.server.upstream.FirmsSource
import com.jyodroid.kunasismoayuda.server.upstream.GdacsFireSource
import com.jyodroid.kunasismoayuda.server.upstream.QuakeSource
import com.jyodroid.kunasismoayuda.server.upstream.ReliefWebSource
import com.jyodroid.kunasismoayuda.server.upstream.SgcSource
import com.jyodroid.kunasismoayuda.server.upstream.UsgsSource
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { HttpClientFactory.create() }
    single {
        AnthropicClient(
            http = get(),
            apiKey = System.getenv("ANTHROPIC_API_KEY"),
            // Cheap default for short structured extraction; override with ANTHROPIC_MODEL (e.g. an Opus
            // model) if you want higher-quality classification at higher cost.
            model = System.getenv("ANTHROPIC_MODEL")?.takeIf { it.isNotBlank() } ?: "claude-haiku-4-5-20251001",
        )
    }
    single { FactCheckClient(http = get(), apiKey = System.getenv("FACT_CHECK_API_KEY")) }
}

val upstreamModule = module {
    single<QuakeSource>(named("primary")) { SgcSource(get()) }
    single<QuakeSource>(named("fallback")) { UsgsSource(get()) }
    single<FireSource>(named("firePrimary")) { FirmsSource(get(), System.getenv("FIRMS_MAP_KEY")) }
    single<FireSource>(named("fireFallback")) { GdacsFireSource(get()) }
    single { GdacsSource(get()) }
    single { ReliefWebSource(get()) }
}

val repositoryModule = module {
    single<ShelterRepository> { ShelterRepositoryImpl() }
    single<ResourceBoardRepository> { ResourceBoardRepositoryImpl() }
    single<SearchRepository> { SearchReportRepositoryImpl() }
    single<PhotoRepository> { PhotoRepositoryImpl() }
    single<SosRepository> { SosRepositoryImpl() }
    single<AdminUserRepository> { AdminUserRepositoryImpl() }
    single<DisasterRepository> { DisasterRepositoryImpl() }
    single<ReportRepository> { ReportRepositoryImpl() }
    single<ApiUsageRepository> { ApiUsageRepositoryImpl() }
    single<ClassifyCacheRepository> { ClassifyCacheRepositoryImpl() }
    single<AuditRepository> { AuditRepositoryImpl() }
}

val serviceModule = module {
    single {
        QuakeService(
            primary = get(named("primary")),
            fallback = get(named("fallback")),
        )
    }
    single {
        FireService(
            primary = get(named("firePrimary")),
            fallback = get(named("fireFallback")),
        )
    }
    single { ShelterService(get()) }
    single { UsageLimiter(get(), UsageLimiter.limitsFromEnv()) }
    single { ResourceBoardService(get(), get(), get(), get(), get()) }
    single { SearchService(get()) }
    single { SosService(get()) }
    single { AuthService(get(), get()) }
    single { AdminService(get()) }
    // AuditService(audit, adminUsers, shelters, board, sos, search) — resolved by type.
    single { AuditService(get(), get(), get(), get(), get(), get()) }
    single { DisasterIngestionService(get(), get(), get(), get()) }
    single { ExpiryService(get(), get()) }
    single { PurgeService(get(), get(), get(), get()) }
}

val appModules = listOf(networkModule, upstreamModule, repositoryModule, serviceModule)
