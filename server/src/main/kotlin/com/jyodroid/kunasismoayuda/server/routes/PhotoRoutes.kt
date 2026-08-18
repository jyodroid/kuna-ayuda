package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.config.BoardRateLimit
import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.PhotoUploadResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

private const val MAX_BYTES = 3 * 1024 * 1024 // 3 MB
private val ALLOWED = setOf("image/jpeg", "image/png", "image/webp")

/**
 * Image storage for Lost & Found photos. `POST` accepts a single multipart image (rate-limited,
 * type-allowlisted, ≤3 MB) and stores it as bytea, returning its id; `GET /{id}` serves the bytes.
 */
fun Route.photoRoutes(repository: PhotoRepository) = route("/api/photos") {
    rateLimit(BoardRateLimit) {
        post {
            var bytes: ByteArray? = null
            var contentType: String? = null
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem && bytes == null) {
                    contentType = part.contentType?.let { "${it.contentType}/${it.contentSubtype}" }
                    bytes = part.provider().readRemaining().readByteArray()
                }
                part.dispose()
            }

            val data = bytes ?: throw appError(ErrorCode.VALIDATION, "No image file provided", HttpStatusCode.BadRequest)
            val type = contentType?.lowercase()
            if (type == null || type !in ALLOWED) {
                throw appError(ErrorCode.VALIDATION, "Unsupported image type (use JPEG/PNG/WebP)", HttpStatusCode.BadRequest)
            }
            if (data.size > MAX_BYTES) {
                throw appError(ErrorCode.VALIDATION, "Image too large (max 3 MB)", HttpStatusCode.PayloadTooLarge)
            }
            val id = repository.save(type, data)
            call.respond(HttpStatusCode.Created, PhotoUploadResponse(id))
        }
    }

    get("/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: throw appError(ErrorCode.VALIDATION, "Photo id must be an integer", HttpStatusCode.BadRequest)
        val photo = repository.find(id)
            ?: throw appError(ErrorCode.NOT_FOUND, "Photo not found", HttpStatusCode.NotFound)
        call.respondBytes(photo.data, ContentType.parse(photo.contentType))
    }
}
