package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.NewSearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject

/** Reads and publishes lost/found reunification reports via the backend `/api/search` + `/api/photos`. */
interface SearchRepository {
    suspend fun list(
        subject: SearchSubject?,
        status: SearchStatus?,
        country: String = "CO",
    ): List<SearchReport>

    /**
     * Publish a report. If [photo] is provided it's uploaded first (`POST /api/photos`) and the
     * returned id is attached to the report. [mime] is the image content type (e.g. `image/jpeg`).
     */
    suspend fun create(report: NewSearchReport, photo: ByteArray?, mime: String?): SearchReport

    /** Remove a report (moderator-only; anti-abuse). */
    suspend fun delete(id: Int)
}
