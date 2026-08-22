package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.Photo

interface PhotoRepository {
    /** Store image bytes, returns the new photo id. */
    fun save(contentType: String, data: ByteArray): Int
    fun find(id: Int): Photo?

    /**
     * Deletes photos created before [cutoff] that no search report references anymore (e.g. an upload
     * whose report creation failed). Returns the count. The 60-day purge already removes photos tied to
     * purged reports; this cleans stragglers.
     */
    fun deleteOrphansOlderThan(cutoff: java.time.LocalDateTime): Int
}
