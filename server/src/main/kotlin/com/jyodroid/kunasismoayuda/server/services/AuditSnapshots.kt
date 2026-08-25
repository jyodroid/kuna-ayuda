package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.NewSearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.NewShelter
import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import kotlinx.serialization.Serializable

/**
 * JSON-serializable snapshots of the entities the audit log records before/after a moderator action.
 * They are the source of truth for revert (dates are dropped — revert restores editable state, not the
 * original timestamps). One file so the shapes stay together.
 */

@Serializable
data class ShelterSnapshot(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String,
    val hours: String?,
    val contactPhone: String?,
    val verified: Boolean,
    val active: Boolean,
    val country: String,
) {
    fun toNewShelter() = NewShelter(name, type, address, latitude, longitude, accepts, hours, contactPhone, country)
}

fun Shelter.toSnapshot() = ShelterSnapshot(
    id, name, type, address, latitude, longitude, accepts, hours, contactPhone, verified, active, country,
)

@Serializable
data class BoardPostSnapshot(
    val id: Int,
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactName: String?,
    val status: String,
    val source: String,
    val rawText: String?,
    val factCheck: String?,
    val country: String,
    val ownerSecret: String?,
)

fun ResourcePost.toSnapshot() = BoardPostSnapshot(
    id, kind, resourceType, region, description, contactPhone, contactEmail, contactName,
    status, source, rawText, factCheck, country, ownerSecret,
)

@Serializable
data class SosSnapshot(
    val id: Int,
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val region: String?,
    val message: String?,
    val contactPhone: String?,
    val handledBy: String?,
    val displayName: String? = null,
    val country: String = "CO",
) {
    fun toNewSos() = NewSosReport(status, latitude, longitude, region, message, contactPhone, displayName, country)
}

fun SosReport.toSnapshot() =
    SosSnapshot(id, status, latitude, longitude, region, message, contactPhone, handledBy, displayName, country)

@Serializable
data class SearchSnapshot(
    val id: Int,
    val subject: String,
    val state: String,
    val title: String,
    val description: String,
    val lastSeen: String,
    val notes: String?,
    val contactPhone: String,
    val contactName: String?,
    val photoId: Int?,
    val country: String,
    val status: String,
) {
    fun toNewSearch() = NewSearchReport(
        subject, state, title, description, lastSeen, notes, contactPhone, contactName, photoId, country, "ACTIVE",
    )
}

fun SearchReport.toSnapshot() = SearchSnapshot(
    id, subject, state, title, description, lastSeen, notes, contactPhone, contactName, photoId, country, status,
)
