package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/**
 * The pasted text had nothing to classify — it was just a link, or the AI extracted nothing usable
 * (server returns 422). Surfaced so the paste screen can show a specific "paste the text, not a link"
 * message instead of a generic error.
 */
class UnreadablePasteException : RuntimeException("The pasted text could not be classified")

class BoardApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun list(kind: String?, region: String?, type: String?, country: String = "CO"): List<ResourcePostDto> =
        client.get("$baseUrl/api/board") {
            kind?.let { parameter("kind", it) }
            region?.let { parameter("region", it) }
            type?.let { parameter("type", it) }
            parameter("country", country)
        }.body()

    suspend fun create(post: NewResourcePostDto): ResourcePostDto =
        client.post("$baseUrl/api/board") {
            contentType(ContentType.Application.Json)
            setBody(post)
        }.body()

    /** Step 1: classify a paste → preview for the poster to review (nothing is queued yet). */
    suspend fun classifyPreview(text: String, country: String = "CO", kind: String? = null): ClassifyPreviewDto {
        val response = client.post("$baseUrl/api/board/classify") {
            contentType(ContentType.Application.Json)
            setBody(ClassifyRequestDto(text, country, kind))
        }
        if (response.status == HttpStatusCode.UnprocessableEntity) throw UnreadablePasteException()
        return response.body()
    }

    /** Step 2: the poster confirmed the preview → queue it as a moderated (pending) post. */
    suspend fun confirmClassify(text: String, country: String = "CO", kind: String? = null): ResourcePostDto {
        val response = client.post("$baseUrl/api/board/classify/confirm") {
            contentType(ContentType.Application.Json)
            setBody(ClassifyRequestDto(text, country, kind))
        }
        if (response.status == HttpStatusCode.UnprocessableEntity) throw UnreadablePasteException()
        return response.body()
    }

    /** Image intake step 1: classify a SCREENSHOT/photo (multipart) → preview (with a cacheRef). */
    suspend fun classifyImage(bytes: ByteArray, mime: String, country: String = "CO", kind: String? = null): ClassifyPreviewDto {
        val response = client.post("$baseUrl/api/board/classify/image") {
            parameter("country", country)
            kind?.let { parameter("kind", it) }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mime)
                                append(HttpHeaders.ContentDisposition, "filename=\"post\"")
                            },
                        )
                    },
                ),
            )
        }
        if (response.status == HttpStatusCode.UnprocessableEntity) throw UnreadablePasteException()
        return response.body()
    }

    /** Image intake step 2: confirm the previewed classify by its cacheRef (no image re-upload). */
    suspend fun confirmRef(cacheRef: String, country: String = "CO", kind: String? = null): ResourcePostDto {
        val response = client.post("$baseUrl/api/board/classify/confirm-ref") {
            contentType(ContentType.Application.Json)
            setBody(ConfirmRefRequestDto(cacheRef, country, kind))
        }
        if (response.status == HttpStatusCode.UnprocessableEntity) throw UnreadablePasteException()
        return response.body()
    }

    /** Step 2 (edited): confirm the poster-corrected preview (both text & image paths use this). */
    suspend fun confirmEdit(req: ConfirmClassifyRequestDto): ResourcePostDto {
        val response = client.post("$baseUrl/api/board/classify/confirm-edit") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (response.status == HttpStatusCode.UnprocessableEntity) throw UnreadablePasteException()
        return response.body()
    }

    /** Device-gated resolve: close a post by presenting its owner secret (#4). */
    suspend fun resolve(id: Int, secret: String) {
        client.post("$baseUrl/api/board/$id/resolve") {
            contentType(ContentType.Application.Json)
            setBody(ResolveRequestDto(secret))
        }
    }

    // --- Moderator-only: the server enforces role=ADMIN on these; we attach the bearer token. ---

    suspend fun listPending(token: String): List<ResourcePostDto> =
        client.get("$baseUrl/api/board/pending") {
            bearerAuth(token)
        }.body()

    suspend fun listActive(token: String): List<ResourcePostDto> =
        client.get("$baseUrl/api/board/active") {
            bearerAuth(token)
        }.body()

    suspend fun approve(id: Int, token: String) {
        client.post("$baseUrl/api/board/$id/approve") {
            bearerAuth(token)
        }
    }

    suspend fun reject(id: Int, token: String) {
        client.delete("$baseUrl/api/board/$id") {
            bearerAuth(token)
        }
    }
}
