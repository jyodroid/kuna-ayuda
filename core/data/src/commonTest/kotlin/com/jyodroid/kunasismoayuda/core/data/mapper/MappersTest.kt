package com.jyodroid.kunasismoayuda.core.data.mapper

import com.jyodroid.kunasismoayuda.core.data.remote.ClassifyPreviewDto
import com.jyodroid.kunasismoayuda.core.data.remote.FireDto
import com.jyodroid.kunasismoayuda.core.data.remote.ResourcePostDto
import com.jyodroid.kunasismoayuda.core.data.remote.ShelterDto
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import com.jyodroid.kunasismoayuda.core.domain.model.ShelterType
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun resource_post_dto_maps_enums_and_fields() {
        val dto = ResourcePostDto(
            id = 7, kind = "offer", resourceType = "water", region = "Cali",
            description = "agua", contactPhone = "123", contactEmail = "a@b.co",
            contactName = "Ana", status = "ACTIVE", source = "classified",
            rawText = "raw", factCheck = "note", createdAt = "2026-08-18", ownerSecret = null,
        )
        val post = dto.toDomain()
        assertEquals(7, post.id)
        assertEquals(PostKind.OFFER, post.kind)
        assertEquals(ResourceType.WATER, post.resourceType)
        assertEquals("Cali", post.region)
        assertEquals("a@b.co", post.contactEmail)
        assertEquals("note", post.factCheck)
    }

    @Test
    fun unknown_enum_values_fall_back_safely() {
        val dto = ResourcePostDto(
            id = 1, kind = "???", resourceType = "???", region = "", description = "",
            createdAt = "2026-01-01",
        )
        val post = dto.toDomain()
        assertEquals(PostKind.REQUEST, post.kind)
        assertEquals(ResourceType.OTHER, post.resourceType)
    }

    @Test
    fun classify_preview_dto_maps_to_domain() {
        val preview = ClassifyPreviewDto(
            kind = "REQUEST", resourceType = "MEDICINE", region = "Ibagué",
            description = "necesito", contactPhone = null, contactName = "Cruz Roja", factCheck = null,
        ).toDomain()
        assertEquals(PostKind.REQUEST, preview.kind)
        assertEquals(ResourceType.MEDICINE, preview.resourceType)
        assertEquals("Cruz Roja", preview.contactName)
    }

    @Test
    fun fire_dto_maps_all_fields() {
        val fire = FireDto(
            id = "firms:1", time = 42L, latitude = -3.9, longitude = -67.5,
            brightnessK = 320.0, frpMw = 28.3, confidence = "n", daynight = "D",
            source = "FIRMS", place = null,
        ).toDomain()
        assertEquals("firms:1", fire.id)
        assertEquals(42L, fire.timeMillis)
        assertEquals(28.3, fire.frpMw)
    }

    @Test
    fun shelter_dto_maps_type_via_fromRaw() {
        val shelter = ShelterDto(
            id = 3, name = "Cruz Roja", type = "salud", address = "Calle 1",
            latitude = 4.0, longitude = -74.0, accepts = "", hours = null,
            contactPhone = null, verified = true, lastVerified = null,
        ).toDomain()
        assertEquals(ShelterType.SALUD, shelter.type)
        assertEquals("Cruz Roja", shelter.name)
    }
}
