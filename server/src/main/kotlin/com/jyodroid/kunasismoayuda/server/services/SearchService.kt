package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.NewSearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.SearchReportRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.SearchReportResponse
import io.ktor.http.HttpStatusCode

/** Lost & Found / reunification reports. Public reads + direct posting (validated); admin can close. */
class SearchService(private val repository: SearchRepository) {

    companion object {
        val SUBJECTS = setOf("PET", "PERSON")
        val STATES = setOf("LOST", "FOUND")
    }

    fun list(subject: String?, state: String?, country: String = "CO"): List<SearchReportResponse> =
        repository.listActive(subject, state, country).map { it.toResponse() }

    fun create(request: SearchReportRequest): SearchReportResponse {
        validate(request)
        return repository.create(
            NewSearchReport(
                subject = request.subject.uppercase(),
                state = request.state.uppercase(),
                title = request.title.trim(),
                description = request.description.trim(),
                lastSeen = request.lastSeen.trim(),
                notes = request.notes?.trim()?.ifBlank { null },
                contactPhone = request.contactPhone.trim(),
                contactName = request.contactName?.trim()?.ifBlank { null },
                photoId = request.photoId,
                country = request.country,
            ),
        ).toResponse()
    }

    fun close(id: Int): Boolean = repository.close(id)

    private fun validate(request: SearchReportRequest) {
        fun bad(message: String): Nothing =
            throw appError(ErrorCode.VALIDATION, message, HttpStatusCode.BadRequest)

        if (request.subject.uppercase() !in SUBJECTS) bad("subject must be PET or PERSON")
        if (request.state.uppercase() !in STATES) bad("state must be LOST or FOUND")
        if (request.title.isBlank()) bad("title is required")
        if (request.title.length > 160) bad("title is too long (max 160)")
        if (request.lastSeen.isBlank()) bad("lastSeen is required")
        if (request.lastSeen.length > 160) bad("lastSeen is too long (max 160)")
        if (request.contactPhone.isBlank()) bad("contactPhone is required")
        if (request.contactPhone.length > 40) bad("contactPhone is too long")
        if (request.description.length > 800) bad("description is too long (max 800)")
    }

    private fun SearchReport.toResponse() = SearchReportResponse(
        id = id,
        subject = subject,
        state = state,
        title = title,
        description = description,
        lastSeen = lastSeen,
        notes = notes,
        contactPhone = contactPhone,
        contactName = contactName,
        photoId = photoId,
        country = country,
        status = status,
        createdAt = createdAt.toString(),
    )
}
