package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.repositories.DisasterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ReportRepository
import com.jyodroid.kunasismoayuda.server.services.AdminService
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.AuthService
import com.jyodroid.kunasismoayuda.server.services.FireService
import com.jyodroid.kunasismoayuda.server.services.QuakeService
import com.jyodroid.kunasismoayuda.server.services.ResourceBoardService
import com.jyodroid.kunasismoayuda.server.services.SearchService
import com.jyodroid.kunasismoayuda.server.services.ShelterService
import com.jyodroid.kunasismoayuda.server.services.SosService
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val quakeService by inject<QuakeService>()
    val fireService by inject<FireService>()
    val shelterService by inject<ShelterService>()
    val boardService by inject<ResourceBoardService>()
    val sosService by inject<SosService>()
    val authService by inject<AuthService>()
    val adminService by inject<AdminService>()
    val auditService by inject<AuditService>()
    val searchService by inject<SearchService>()
    val photoRepo by inject<PhotoRepository>()
    val disasterRepo by inject<DisasterRepository>()
    val reportRepo by inject<ReportRepository>()

    routing {
        versionRoutes()
        authRoutes(authService, auditService)
        adminRoutes(adminService, auditService)
        auditRoutes(auditService)
        quakeRoutes(quakeService)
        fireRoutes(fireService)
        shelterRoutes(shelterService, auditService)
        resourceBoardRoutes(boardService, auditService)
        searchRoutes(searchService, auditService)
        photoRoutes(photoRepo)
        sosRoutes(sosService, auditService)
        disasterRoutes(disasterRepo, reportRepo)

        // Super-admin oversight console (React build copied into resources/console by :server:buildConsole).
        // Registered before the "/" catch-all; non-/api so it's never gated. default() serves the SPA shell.
        // `/console` (no trailing slash) must redirect to `/console/`, or it falls through to the landing
        // ("/" static) and flashes the landing logo before the user realizes the console didn't load.
        get("/console") { call.respondRedirect("/console/", permanent = false) }
        staticResources("/console", "console") { default("index.html") }

        // Marketing + legal landing page (Vite build copied into resources/web by :server:buildLanding).
        // Non-/api paths are never touched by the app-gate (AppGate.kt) or app JWT. Static only matches
        // real files, so it never shadows the /api routes above; "/" serves index.html.
        staticResources("/", "web") { default("index.html") }
        get("/privacy") { call.respondRedirect("/privacy.html", permanent = false) }
        get("/terms") { call.respondRedirect("/terms.html", permanent = false) }
    }
}
