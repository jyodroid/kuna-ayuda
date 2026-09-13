package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

/**
 * The emergency guide + safety tips for a country/language — the **central source of truth** so every
 * platform (web now; apps later) shares one copy instead of duplicating it in `strings.xml` /
 * `EmergencyDirectory.kt`. Emergency numbers are country-specific; tips are localized (es/en for now).
 */
@Serializable
data class GuideResponse(
    val country: String,
    val lang: String,
    val emergency: EmergencyBlock,
    val tips: List<TipPhase>,
)

@Serializable
data class EmergencyBlock(
    val general: String, // the single number for any life-threatening emergency
    val contacts: List<GuideContact>,
)

@Serializable
data class GuideContact(
    val category: String, // GENERAL | POLICE | FIRE | MEDICAL | RED_CROSS | CIVIL_DEFENSE | SAR | DISASTER | MENTAL_HEALTH
    val name: String,     // agency proper noun (already in the country's language)
    val phone: String,
)

@Serializable
data class TipPhase(
    val key: String,   // before | during | after | mental | animals
    val label: String, // localized heading
    val tips: List<GuideTip>,
)

@Serializable
data class GuideTip(
    val id: String,
    val title: String,
    val body: String,
    val steps: List<TipStep> = emptyList(), // wordless storyboard (accessibility): image + short caption
)

/** One step of a tip's illustrated storyboard. [img] is a filename served by the web app at /app/story/. */
@Serializable
data class TipStep(val img: String, val caption: String)

/** Shape of the bundled tips_{lang}.json resource. */
@Serializable
data class TipsFile(val phases: List<TipPhase>)
