package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.ai.AnthropicBillingException
import com.jyodroid.kunasismoayuda.server.ai.AnthropicCallException
import com.jyodroid.kunasismoayuda.server.ai.AnthropicMissingKeyException
import com.jyodroid.kunasismoayuda.server.ai.AnthropicRefusalException
import com.jyodroid.kunasismoayuda.server.config.BoardRateLimit
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.services.UsageLimitReachedException
import com.jyodroid.kunasismoayuda.server.routes.dto.ClassifyRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.ResolveRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostRequest
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.ResolveOutcome
import com.jyodroid.kunasismoayuda.server.services.ResourceBoardService
import com.jyodroid.kunasismoayuda.server.services.UnclassifiableTextException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Peer mutual-aid board. Reading and posting are public (that's the point — anyone can ask or
 * offer). Guardrails: server-side validation + a rate limit on posting; administrators can close
 * abusive entries. `POST /classify` turns a pasted social-media post into a **PENDING** entry
 * (Claude-classified) that an admin reviews before it goes live — see the admin routes below.
 */
fun Route.resourceBoardRoutes(service: ResourceBoardService, audit: AuditService) = route("/api/board") {
    get {
        val kind = call.request.queryParameters["kind"]?.uppercase()
        val region = call.request.queryParameters["region"]
        val type = call.request.queryParameters["type"]?.uppercase()
        val country = call.request.queryParameters["country"] ?: "CO"
        call.respond(service.list(kind, region, type, country))
    }

    rateLimit(BoardRateLimit) {
        post {
            val request = call.receive<ResourcePostRequest>()
            validate(request)
            call.respond(HttpStatusCode.Created, service.create(request))
        }

        // Step 1 — preview: classify the paste and show it BACK to the poster to review. Nothing is
        // persisted; the poster confirms below before it reaches a moderator.
        post("/classify") {
            val req = call.receive<ClassifyRequest>()
            val text = req.text.trim()
            validateClassifyText(text, service.classifyEnabled)
            val preview = classifyGuarded { service.classifyPreview(text, req.country, req.kind) }
            call.respond(preview)
        }

        // Step 2 — confirm: the poster approved the preview; queue a PENDING entry for moderation. The
        // classify work is served from cache (see the preview above), so this makes no new paid call.
        post("/classify/confirm") {
            val req = call.receive<ClassifyRequest>()
            val text = req.text.trim()
            validateClassifyText(text, service.classifyEnabled)
            val created = classifyGuarded { service.classifyAndQueue(text, req.country, req.kind) }
            call.respond(HttpStatusCode.Created, created)
        }

        // Device-gated resolve (#4): the creating device closes its own post by presenting the
        // owner_secret it received at creation. No auth — the secret IS the identity.
        post("/{id}/resolve") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Post id must be an integer", HttpStatusCode.BadRequest)
            val secret = call.receive<ResolveRequest>().secret
            if (secret.isBlank()) {
                throw appError(ErrorCode.VALIDATION, "secret is required", HttpStatusCode.BadRequest)
            }
            when (service.resolveByOwner(id, secret)) {
                ResolveOutcome.RESOLVED -> call.respond(HttpStatusCode.NoContent)
                ResolveOutcome.NOT_FOUND -> throw appError(ErrorCode.NOT_FOUND, "Post $id not found", HttpStatusCode.NotFound)
                ResolveOutcome.FORBIDDEN -> throw appError(ErrorCode.FORBIDDEN, "Not the owner of this post", HttpStatusCode.Forbidden)
            }
        }
    }

    authenticate {
        // Admin moderation queue for classified (PENDING) posts.
        get("/pending") {
            requireAdmin()
            call.respond(service.listPending())
        }

        post("/{id}/approve") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Post id must be an integer", HttpStatusCode.BadRequest)
            val before = service.find(id)
            service.approve(id)
            before?.let { audit.boardApproved(actor(), it) }
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Post id must be an integer", HttpStatusCode.BadRequest)
            val before = service.find(id)
            service.close(id)
            before?.let { audit.boardRejected(actor(), it) }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

/** Shared input checks for both classify steps (preview + confirm). */
private fun validateClassifyText(text: String, classifyEnabled: Boolean) {
    if (text.isBlank()) {
        throw appError(ErrorCode.VALIDATION, "text is required", HttpStatusCode.BadRequest)
    }
    if (text.length > 2000) {
        throw appError(ErrorCode.VALIDATION, "text is too long (max 2000)", HttpStatusCode.BadRequest)
    }
    if (!classifyEnabled) {
        throw appError(
            ErrorCode.UPSTREAM_UNAVAILABLE,
            "AI classification is not configured (set ANTHROPIC_API_KEY)",
            HttpStatusCode.ServiceUnavailable,
        )
    }
}

/** Runs a classify call and maps the service/AI failures to clean HTTP errors (shared by both steps). */
private suspend fun <T> classifyGuarded(block: suspend () -> T): T = try {
    block()
} catch (e: UnclassifiableTextException) {
    throw appError(
        ErrorCode.VALIDATION,
        "We couldn't read a post there. Paste the TEXT of the post (its words), not a link or a screenshot.",
        HttpStatusCode.UnprocessableEntity,
    )
} catch (e: UsageLimitReachedException) {
    throw appError(
        ErrorCode.RATE_LIMITED,
        "Monthly AI classification limit reached. Please try again next month.",
        HttpStatusCode.TooManyRequests,
    )
} catch (e: AnthropicMissingKeyException) {
    throw appError(ErrorCode.UPSTREAM_UNAVAILABLE, e.message ?: "AI unavailable", HttpStatusCode.ServiceUnavailable)
} catch (e: AnthropicBillingException) {
    // Expected operational gap (account out of credits) — clean 503, no leaked billing text, and a
    // one-line WARN in the log rather than an alarming ERROR stack.
    throw appError(
        ErrorCode.UPSTREAM_UNAVAILABLE,
        "AI classification is temporarily unavailable. Please try again later.",
        HttpStatusCode.ServiceUnavailable,
        expected = true,
    )
} catch (e: AnthropicRefusalException) {
    throw appError(ErrorCode.VALIDATION, "Could not classify this text", HttpStatusCode.UnprocessableEntity)
} catch (e: AnthropicCallException) {
    throw appError(ErrorCode.UPSTREAM_UNAVAILABLE, e.message ?: "AI call failed", HttpStatusCode.BadGateway)
}

private fun validate(request: ResourcePostRequest) {
    fun bad(message: String): Nothing =
        throw appError(ErrorCode.VALIDATION, message, HttpStatusCode.BadRequest)

    if (request.kind.uppercase() !in ResourceBoardService.KINDS) bad("kind must be REQUEST or OFFER")
    if (request.resourceType.uppercase() !in ResourceBoardService.TYPES) bad("Unknown resourceType")
    if (request.region.isBlank()) bad("region is required")
    if (request.description.length > 500) bad("description is too long (max 500)")
    // Contact is optional (opt-in, #3). Validate only what was provided.
    val phone = request.contactPhone?.trim().orEmpty()
    if (phone.length > 40) bad("contactPhone is too long")
    val email = request.contactEmail?.trim().orEmpty()
    if (email.isNotEmpty() && (email.length > 160 || !email.contains('@') || !email.contains('.'))) {
        bad("contactEmail is invalid")
    }
}
