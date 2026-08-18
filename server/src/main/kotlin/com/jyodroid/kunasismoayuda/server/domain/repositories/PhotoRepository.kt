package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.Photo

interface PhotoRepository {
    /** Store image bytes, returns the new photo id. */
    fun save(contentType: String, data: ByteArray): Int
    fun find(id: Int): Photo?
}
