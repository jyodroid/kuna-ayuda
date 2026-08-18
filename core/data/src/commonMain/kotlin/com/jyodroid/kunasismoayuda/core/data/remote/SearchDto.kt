package com.jyodroid.kunasismoayuda.core.data.remote

import com.jyodroid.kunasismoayuda.core.domain.model.NewSearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject
import kotlinx.serialization.Serializable

@Serializable
data class SearchReportDto(
    val id: Int,
    val subject: String,
    val state: String,
    val title: String,
    val description: String = "",
    val lastSeen: String,
    val notes: String? = null,
    val contactPhone: String,
    val contactName: String? = null,
    val photoId: Int? = null,
    val country: String,
    val status: String,
    val createdAt: String,
)

@Serializable
data class NewSearchReportDto(
    val subject: String,
    val state: String,
    val title: String,
    val description: String = "",
    val lastSeen: String,
    val notes: String? = null,
    val contactPhone: String,
    val contactName: String? = null,
    val photoId: Int? = null,
    val country: String = "CO",
)

@Serializable
data class PhotoUploadDto(val id: Int)

fun SearchReportDto.toDomain(): SearchReport = SearchReport(
    id = id,
    subject = SearchSubject.fromRaw(subject),
    status = SearchStatus.fromRaw(state),
    title = title,
    description = description,
    lastSeen = lastSeen,
    notes = notes,
    contactPhone = contactPhone,
    contactName = contactName,
    photoId = photoId,
    country = country,
    createdAt = createdAt,
)

fun NewSearchReport.toDto(photoId: Int?): NewSearchReportDto = NewSearchReportDto(
    subject = subject.name,
    state = status.name,
    title = title,
    description = description,
    lastSeen = lastSeen,
    notes = notes,
    contactPhone = contactPhone,
    contactName = contactName,
    photoId = photoId,
    country = country,
)
