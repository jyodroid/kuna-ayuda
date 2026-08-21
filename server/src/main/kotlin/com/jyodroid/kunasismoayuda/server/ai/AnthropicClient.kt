package com.jyodroid.kunasismoayuda.server.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** A board post extracted from free text by the model (schema-constrained, so fields are present). */
@Serializable
data class ClassifiedPost(
    val kind: String,          // REQUEST | OFFER
    val resourceType: String,  // WATER | FOOD | MEDICINE | SHELTER | HYGIENE | OTHER
    val region: String,
    val description: String,
    val contactPhone: String,
    val contactName: String,
    val collectionPoints: List<CollectionPoint> = emptyList(),
)

class AnthropicMissingKeyException : RuntimeException("ANTHROPIC_API_KEY is not configured")
class AnthropicRefusalException : RuntimeException("The model declined to classify this text")
class AnthropicCallException(message: String) : RuntimeException(message)

/** The Anthropic account has no available credits (400 billing error) — not a bug, an operational gap. */
class AnthropicBillingException : RuntimeException("Anthropic account has no available credits")

/**
 * Minimal Anthropic Messages API client using the project's Ktor HTTP client (the server already
 * standardizes on Ktor + kotlinx.serialization for outbound calls, so we don't pull in a second
 * HTTP/JSON stack). Uses **structured outputs** (`output_config.format` json_schema) so the model
 * returns a JSON object matching [ClassifiedPost] exactly. Model: claude-opus-4-8.
 */
class AnthropicClient(
    private val http: HttpClient,
    private val apiKey: String?,
    private val model: String = "claude-opus-4-8",
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** True when an API key is present; the route uses this to return a clean 503 instead of failing. */
    val isConfigured: Boolean get() = !apiKey.isNullOrBlank()

    suspend fun classify(text: String, kinds: Set<String>, types: Set<String>): ClassifiedPost {
        if (apiKey.isNullOrBlank()) throw AnthropicMissingKeyException()

        val requestBody = buildRequest(text, kinds, types)
        val response: HttpResponse = http.post(MESSAGES_URL) {
            header("x-api-key", apiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            // Out of credits: Anthropic returns 400 invalid_request_error with a billing message. Our
            // request is well-formed, so treat this as an operational "AI unavailable", not a bad request —
            // and never echo Anthropic's raw billing text to callers.
            val lower = body.lowercase()
            if (response.status.value == 400 && ("credit balance" in lower || "plans & billing" in lower)) {
                throw AnthropicBillingException()
            }
            throw AnthropicCallException("Anthropic API ${response.status}: ${body.take(300)}")
        }

        val payload = response.body<JsonObject>()
        if (payload["stop_reason"]?.jsonPrimitive?.content == "refusal") throw AnthropicRefusalException()

        val jsonText = payload["content"]?.jsonArray
            ?.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.content == "text" }
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: throw AnthropicCallException("No text block in Anthropic response")

        return json.decodeFromString<ClassifiedPost>(jsonText)
    }

    private fun buildRequest(text: String, kinds: Set<String>, types: Set<String>): JsonObject = buildJsonObject {
        put("model", model)
        put("max_tokens", 900) // room to keep lists (orgs, places, phones) instead of over-summarizing
        putJsonObject("output_config") {
            putJsonObject("format") {
                put("type", "json_schema")
                put("schema", schema(kinds, types))
            }
        }
        putJsonArray("messages") {
            addJsonObject {
                put("role", "user")
                put("content", prompt(text))
            }
        }
    }

    private fun schema(kinds: Set<String>, types: Set<String>): JsonObject = buildJsonObject {
        put("type", "object")
        put("additionalProperties", false)
        putJsonObject("properties") {
            putJsonObject("kind") {
                put("type", "string")
                putJsonArray("enum") { kinds.forEach { add(it) } }
            }
            putJsonObject("resourceType") {
                put("type", "string")
                putJsonArray("enum") { types.forEach { add(it) } }
            }
            putJsonObject("region") { put("type", "string") }
            putJsonObject("description") { put("type", "string") }
            putJsonObject("contactPhone") { put("type", "string") }
            putJsonObject("contactName") { put("type", "string") }
            putJsonObject("collectionPoints") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "object")
                    put("additionalProperties", false)
                    putJsonObject("properties") {
                        putJsonObject("name") { put("type", "string") }
                        putJsonObject("address") { put("type", "string") }
                        putJsonObject("hours") { put("type", "string") }
                    }
                    putJsonArray("required") { add("name"); add("address"); add("hours") }
                }
            }
        }
        putJsonArray("required") {
            add("kind"); add("resourceType"); add("region")
            add("description"); add("contactPhone"); add("contactName"); add("collectionPoints")
        }
    }

    private fun prompt(text: String): String = """
        You extract disaster-relief resource requests and offers from social-media posts
        (Instagram, Facebook, WhatsApp, X) about earthquakes. Classify the post below.

        - kind: REQUEST if the post asks for or solicits help/resources/donations for affected people —
          this INCLUDES "please donate/contribute/share", calls to support victims, and lists of
          channels/organizations to donate to. OFFER only when the POSTER THEMSELVES is providing or
          distributing a resource (e.g. "our shelter is open", "we are handing out food/water", "free
          medical care at X"). When unsure, prefer REQUEST.
        - resourceType: the closest single category. WATER, FOOD, MEDICINE, SHELTER, HYGIENE, or OTHER.
        - region: the main city/municipality/department mentioned, else "".
        - description: a faithful, neutral rewrite of the post in the SAME language, that PRESERVES
          every concrete, actionable detail — organization names, specific places/addresses, collection
          points, phone numbers, links/@handles, dates/hours, and ANY list of items or locations. Keep
          the whole list; never condense specifics into a generic phrase like "a list of organizations".
          Drop only greetings, hashtags spam, and emojis. It may be several sentences or a short list.
        - contactPhone: a phone number if present, else "".
        - contactName: a person/organization name if present, else "".
        - collectionPoints: a list of the specific drop-off / collection / distribution points named in the
          post — the places where people bring or pick up the resources. Each item is
          {name, address, hours}: name = the place ("Parroquia San José", "Colegio Central"); address = its
          street/area if given, else ""; hours = opening hours if given, else "". Return [] when the post
          names none. Do NOT invent points, and do NOT treat a general city/region as a collection point.

        Do not invent details. Only extract what is actually in the text.

        Post:
        ${text.trim()}
    """.trimIndent()

    private companion object {
        const val MESSAGES_URL = "https://api.anthropic.com/v1/messages"
        const val ANTHROPIC_VERSION = "2023-06-01"
    }
}
