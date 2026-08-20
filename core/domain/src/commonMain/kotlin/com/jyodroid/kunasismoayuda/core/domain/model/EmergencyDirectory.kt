package com.jyodroid.kunasismoayuda.core.domain.model

/** A kind of emergency service; the UI maps it to a localized label. */
enum class EmergencyCategory {
    GENERAL, POLICE, FIRE, MEDICAL, RED_CROSS, CIVIL_DEFENSE, SAR, DISASTER, MENTAL_HEALTH
}

/**
 * One official emergency contact. [name] is the agency proper noun (not localized — e.g. "Basarnas",
 * "Guardia Civil"); [phone] is what `tel:` dials (any extension is spelled out in [name]).
 */
data class EmergencyContact(
    val category: EmergencyCategory,
    val name: String,
    val phone: String,
)

/**
 * Official emergency numbers per [Country], verified via public sources (Aug 2026):
 * - **Colombia:** 123 (línea única), 119 (bomberos), 132 (Cruz Roja), 144 (Defensa Civil),
 *   192 opción 4 (salud mental).
 * - **Indonesia:** 112 (darurat), 110 (Polri), 113 (Damkar), 119 (ambulans), 115 (Basarnas),
 *   129 (BNPB), 119 ext. 8 (SEJIWA, kesehatan jiwa).
 * - **Spain:** 112 (emergencias UE), 061 (sanitarias), 091 (Policía), 062 (Guardia Civil),
 *   080 (bomberos), 024 (conducta suicida), 900 22 11 22 (Cruz Roja Española).
 * - **Italy:** 112 (NUE, numero unico di emergenza), 118 (emergenza sanitaria), 115 (Vigili del
 *   Fuoco), 113 (Polizia di Stato), 1530 (Guardia Costiera), 1515 (emergenze ambientali / incendi
 *   boschivi), Telefono Amico Italia 02 2327 2327 (supporto emotivo).
 * - **Peru:** 105 (Policía Nacional del Perú, emergencias), 116 (Bomberos — Cuerpo General de
 *   Bomberos Voluntarios del Perú), 106 (SAMU, emergencias médicas), 115 (INDECI, Defensa Civil /
 *   desastres), Línea 113 opción 5 (MINSA, salud mental).
 */
object CountryEmergency {

    /** The single number to dial for any life-threatening emergency in [country]. */
    fun generalNumber(country: Country): String = when (country) {
        Country.COLOMBIA -> "123"
        Country.INDONESIA -> "112"
        Country.SPAIN -> "112"
        Country.ITALY -> "112"
        Country.PERU -> "105"
    }

    /** The national mental-health / crisis line for [country]. */
    fun mentalHealth(country: Country): EmergencyContact = when (country) {
        Country.COLOMBIA -> EmergencyContact(EmergencyCategory.MENTAL_HEALTH, "Línea 192 (opción 4)", "192")
        Country.INDONESIA -> EmergencyContact(EmergencyCategory.MENTAL_HEALTH, "SEJIWA (119 ext. 8)", "119")
        Country.SPAIN -> EmergencyContact(EmergencyCategory.MENTAL_HEALTH, "Línea 024", "024")
        Country.ITALY -> EmergencyContact(EmergencyCategory.MENTAL_HEALTH, "Telefono Amico Italia", "0223272327")
        Country.PERU -> EmergencyContact(EmergencyCategory.MENTAL_HEALTH, "Línea 113 (opción 5) — MINSA", "113")
    }

    /** The full official directory for [country] (general line first, mental-health last). */
    fun contacts(country: Country): List<EmergencyContact> = when (country) {
        Country.COLOMBIA -> listOf(
            EmergencyContact(EmergencyCategory.GENERAL, "Línea única de emergencias", "123"),
            EmergencyContact(EmergencyCategory.FIRE, "Bomberos", "119"),
            EmergencyContact(EmergencyCategory.RED_CROSS, "Cruz Roja Colombiana", "132"),
            EmergencyContact(EmergencyCategory.CIVIL_DEFENSE, "Defensa Civil Colombiana", "144"),
            mentalHealth(country),
        )
        Country.INDONESIA -> listOf(
            EmergencyContact(EmergencyCategory.GENERAL, "Panggilan darurat nasional", "112"),
            EmergencyContact(EmergencyCategory.POLICE, "Polri", "110"),
            EmergencyContact(EmergencyCategory.FIRE, "Pemadam Kebakaran (Damkar)", "113"),
            EmergencyContact(EmergencyCategory.MEDICAL, "Ambulans", "119"),
            EmergencyContact(EmergencyCategory.SAR, "Basarnas", "115"),
            EmergencyContact(EmergencyCategory.DISASTER, "BNPB", "129"),
            mentalHealth(country),
        )
        Country.SPAIN -> listOf(
            EmergencyContact(EmergencyCategory.GENERAL, "Emergencias (UE)", "112"),
            EmergencyContact(EmergencyCategory.MEDICAL, "Emergencias sanitarias", "061"),
            EmergencyContact(EmergencyCategory.POLICE, "Policía Nacional", "091"),
            EmergencyContact(EmergencyCategory.POLICE, "Guardia Civil", "062"),
            EmergencyContact(EmergencyCategory.FIRE, "Bomberos", "080"),
            EmergencyContact(EmergencyCategory.RED_CROSS, "Cruz Roja Española", "900221122"),
            mentalHealth(country),
        )
        Country.ITALY -> listOf(
            EmergencyContact(EmergencyCategory.GENERAL, "Numero unico di emergenza (NUE)", "112"),
            EmergencyContact(EmergencyCategory.MEDICAL, "Emergenza sanitaria", "118"),
            EmergencyContact(EmergencyCategory.FIRE, "Vigili del Fuoco", "115"),
            EmergencyContact(EmergencyCategory.POLICE, "Polizia di Stato", "113"),
            EmergencyContact(EmergencyCategory.SAR, "Guardia Costiera", "1530"),
            mentalHealth(country),
        )
        Country.PERU -> listOf(
            EmergencyContact(EmergencyCategory.GENERAL, "Policía Nacional del Perú", "105"),
            EmergencyContact(EmergencyCategory.FIRE, "Bomberos", "116"),
            EmergencyContact(EmergencyCategory.MEDICAL, "SAMU (emergencias médicas)", "106"),
            EmergencyContact(EmergencyCategory.CIVIL_DEFENSE, "INDECI — Defensa Civil", "115"),
            mentalHealth(country),
        )
    }
}
