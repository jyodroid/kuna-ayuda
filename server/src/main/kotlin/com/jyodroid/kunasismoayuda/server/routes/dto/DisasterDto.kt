package com.jyodroid.kunasismoayuda.server.routes.dto

import com.jyodroid.kunasismoayuda.server.domain.models.Disaster
import com.jyodroid.kunasismoayuda.server.domain.models.DisasterReport
import kotlinx.serialization.Serializable

/** Timestamps are exposed as epoch millis (like QuakeResponse.time), so the client stays platform-agnostic. */
@Serializable
data class DisasterDto(
    val id: Int,
    val source: String,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val magnitude: Double? = null,
    val alertLevel: String? = null,
    val severityText: String? = null,
    val eventDate: Long? = null,
    val url: String? = null,
    val fetchedAt: Long,
)

@Serializable
data class DisasterReportDto(
    val id: Int,
    val source: String,
    val title: String,
    val body: String? = null,
    val orgSource: String? = null,
    val country: String? = null,
    val disasterType: String? = null,
    val url: String? = null,
    val publishedAt: Long? = null,
    val fetchedAt: Long,
)

fun Disaster.toDto() = DisasterDto(
    id = id,
    source = source,
    eventType = eventType,
    title = title,
    description = description,
    country = country,
    latitude = latitude,
    longitude = longitude,
    magnitude = magnitude,
    alertLevel = alertLevel,
    severityText = severityText,
    eventDate = eventDate?.toEpochMilli(),
    url = url,
    fetchedAt = fetchedAt.toEpochMilli(),
)

fun DisasterReport.toDto() = DisasterReportDto(
    id = id,
    source = source,
    title = title,
    body = body,
    orgSource = orgSource,
    country = country,
    disasterType = disasterType,
    url = url,
    publishedAt = publishedAt?.toEpochMilli(),
    fetchedAt = fetchedAt.toEpochMilli(),
)
