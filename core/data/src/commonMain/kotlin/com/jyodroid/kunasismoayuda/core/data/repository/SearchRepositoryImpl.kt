package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.remote.SearchApi
import com.jyodroid.kunasismoayuda.core.data.remote.toDomain
import com.jyodroid.kunasismoayuda.core.data.remote.toDto
import com.jyodroid.kunasismoayuda.core.domain.model.NewSearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject
import com.jyodroid.kunasismoayuda.core.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val api: SearchApi,
    private val sessionManager: SessionManager,
) : SearchRepository {

    override suspend fun list(
        subject: SearchSubject?,
        status: SearchStatus?,
        country: String,
    ): List<SearchReport> =
        api.list(subject?.name, status?.name, country).map { it.toDomain() }

    override suspend fun create(report: NewSearchReport, photo: ByteArray?, mime: String?): SearchReport {
        val photoId = if (photo != null) api.uploadPhoto(photo, mime ?: "image/jpeg").id else null
        return api.create(report.toDto(photoId)).toDomain()
    }

    override suspend fun delete(id: Int) = api.delete(id, sessionManager.requireToken())
}
