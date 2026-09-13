package com.jyodroid.kunasismoayuda.server.content

import com.jyodroid.kunasismoayuda.server.routes.dto.EmergencyBlock
import com.jyodroid.kunasismoayuda.server.routes.dto.GuideContact
import com.jyodroid.kunasismoayuda.server.routes.dto.GuideResponse
import com.jyodroid.kunasismoayuda.server.routes.dto.TipPhase
import com.jyodroid.kunasismoayuda.server.routes.dto.TipsFile
import kotlinx.serialization.json.Json

/**
 * The central emergency-guide content: verified per-country emergency numbers (ported from the app's
 * `core/domain EmergencyDirectory.kt`) + localized safety tips (bundled `guide/tips_{lang}.json`,
 * extracted from the app's `strings.xml`). Static content — no DB. Tips are cached after first load.
 *
 * Supported tip languages: **es, en** (the web app). Adding **id/it** here is what a future
 * apps-consume-this migration needs; unknown languages fall back to Spanish.
 */
object GuideContent {

    private val json = Json { ignoreUnknownKeys = true }
    private val tipsCache = HashMap<String, List<TipPhase>>()

    fun response(countryCode: String, langRaw: String): GuideResponse {
        val country = countryCode.uppercase().take(2)
        val lang = langRaw.lowercase().take(2).let { if (it == "en") "en" else "es" }
        return GuideResponse(country = country, lang = lang, emergency = emergency(country), tips = tips(lang))
    }

    private fun tips(lang: String): List<TipPhase> = tipsCache.getOrPut(lang) {
        val path = "guide/tips_$lang.json"
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: javaClass.classLoader.getResourceAsStream("guide/tips_es.json")
            ?: return@getOrPut emptyList()
        stream.use { json.decodeFromString<TipsFile>(it.readBytes().decodeToString()).phases }
    }

    private fun c(category: String, name: String, phone: String) = GuideContact(category, name, phone)

    private fun emergency(country: String): EmergencyBlock = when (country) {
        "ID" -> EmergencyBlock(
            general = "112",
            contacts = listOf(
                c("GENERAL", "Panggilan darurat nasional", "112"),
                c("POLICE", "Polri", "110"),
                c("FIRE", "Pemadam Kebakaran (Damkar)", "113"),
                c("MEDICAL", "Ambulans", "119"),
                c("SAR", "Basarnas", "115"),
                c("DISASTER", "BNPB", "129"),
                c("MENTAL_HEALTH", "SEJIWA (119 ext. 8)", "119"),
            ),
        )
        "ES" -> EmergencyBlock(
            general = "112",
            contacts = listOf(
                c("GENERAL", "Emergencias (UE)", "112"),
                c("MEDICAL", "Emergencias sanitarias", "061"),
                c("POLICE", "Policía Nacional", "091"),
                c("POLICE", "Guardia Civil", "062"),
                c("FIRE", "Bomberos", "080"),
                c("RED_CROSS", "Cruz Roja Española", "900221122"),
                c("MENTAL_HEALTH", "Línea 024", "024"),
            ),
        )
        "IT" -> EmergencyBlock(
            general = "112",
            contacts = listOf(
                c("GENERAL", "Numero unico di emergenza (NUE)", "112"),
                c("MEDICAL", "Emergenza sanitaria", "118"),
                c("FIRE", "Vigili del Fuoco", "115"),
                c("POLICE", "Polizia di Stato", "113"),
                c("SAR", "Guardia Costiera", "1530"),
                c("MENTAL_HEALTH", "Telefono Amico Italia", "0223272327"),
            ),
        )
        "PE" -> EmergencyBlock(
            general = "105",
            contacts = listOf(
                c("GENERAL", "Policía Nacional del Perú", "105"),
                c("FIRE", "Bomberos", "116"),
                c("MEDICAL", "SAMU (emergencias médicas)", "106"),
                c("CIVIL_DEFENSE", "INDECI — Defensa Civil", "115"),
                c("MENTAL_HEALTH", "Línea 113 (opción 5) — MINSA", "113"),
            ),
        )
        else -> EmergencyBlock( // CO (default)
            general = "123",
            contacts = listOf(
                c("GENERAL", "Línea única de emergencias", "123"),
                c("FIRE", "Bomberos", "119"),
                c("RED_CROSS", "Cruz Roja Colombiana", "132"),
                c("CIVIL_DEFENSE", "Defensa Civil Colombiana", "144"),
                c("MENTAL_HEALTH", "Línea 192 (opción 4)", "192"),
            ),
        )
    }
}
