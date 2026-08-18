package com.jyodroid.kunasismoayuda.server.upstream

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/**
 * Google **Fact Check Tools API** (`claims:search`) — a read-only search over claims that publishers
 * have marked up with ClaimReview. We use it as a **moderation signal**: when a pasted board post is
 * classified, we search its text and, if any fact-checks match, attach a short note to the PENDING post
 * so the moderator can see "this claim has been fact-checked (rated X by Y)" before approving. It never
 * auto-rejects — misinformation during a disaster is often mixed with genuine aid, so a human decides.
 *
 * Needs a free Google API key (`FACT_CHECK_API_KEY`) with the Fact Check Tools API enabled; when unset
 * the client is disabled and returns null (classification still works). Free but quota-limited, so calls
 * go through the [com.jyodroid.kunasismoayuda.server.services.UsageLimiter] at the call site.
 * Docs: https://developers.google.com/fact-check/tools/api/
 */
class FactCheckClient(
    private val http: HttpClient,
    private val apiKey: String?,
) {
    private val logger = LoggerFactory.getLogger(FactCheckClient::class.java)

    val isConfigured: Boolean get() = !apiKey.isNullOrBlank()

    /**
     * Search fact-checks matching [query]; returns a compact moderator note (top matches, rating +
     * publisher + link) or null when disabled, on error, or when nothing matches.
     */
    suspend fun searchSummary(query: String, languageCode: String? = null): String? {
        val key = apiKey?.takeIf { it.isNotBlank() } ?: return null
        val q = query.trim().take(MAX_QUERY_CHARS).ifBlank { return null }

        val response: HttpResponse = try {
            http.get(SEARCH_URL) {
                parameter("query", q)
                parameter("pageSize", MAX_RESULTS)
                if (!languageCode.isNullOrBlank()) parameter("languageCode", languageCode)
                parameter("key", key)
            }
        } catch (e: Exception) {
            logger.warn("Fact Check request failed", e)
            return null
        }
        if (!response.status.isSuccess()) {
            logger.warn("Fact Check API ${response.status}")
            return null
        }

        val body = runCatching { response.body<FactCheckResponse>() }.getOrElse {
            logger.warn("Fact Check parse failed", it)
            return null
        }
        val claims = body.claims.orEmpty()
        if (claims.isEmpty()) return null

        val lines = claims.take(MAX_RESULTS).mapNotNull { claim ->
            val review = claim.claimReview?.firstOrNull() ?: return@mapNotNull null
            val rating = review.textualRating?.takeIf { it.isNotBlank() } ?: "sin calificación"
            val publisher = review.publisher?.name?.takeIf { it.isNotBlank() } ?: review.publisher?.site ?: "fuente"
            val url = review.url?.let { " — $it" } ?: ""
            "• [$rating] $publisher$url"
        }
        if (lines.isEmpty()) return null
        return "Verificaciones relacionadas (Google Fact Check):\n" + lines.joinToString("\n")
    }

    private companion object {
        const val SEARCH_URL = "https://factchecktools.googleapis.com/v1alpha1/claims:search"
        const val MAX_QUERY_CHARS = 200
        const val MAX_RESULTS = 3
    }
}

@Serializable
private data class FactCheckResponse(val claims: List<FactCheckClaim>? = null)

@Serializable
private data class FactCheckClaim(
    val text: String? = null,
    val claimant: String? = null,
    val claimReview: List<FactCheckReview>? = null,
)

@Serializable
private data class FactCheckReview(
    val publisher: FactCheckPublisher? = null,
    val url: String? = null,
    val title: String? = null,
    val textualRating: String? = null,
    val languageCode: String? = null,
)

@Serializable
private data class FactCheckPublisher(
    val name: String? = null,
    val site: String? = null,
)
