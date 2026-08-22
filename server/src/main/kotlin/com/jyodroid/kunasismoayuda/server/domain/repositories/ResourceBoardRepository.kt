package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import java.time.LocalDateTime

interface ResourceBoardRepository {
    fun listActive(kind: String?, region: String?, resourceType: String?, country: String = "CO"): List<ResourcePost>
    fun listByStatus(status: String): List<ResourcePost>
    fun create(post: NewResourcePost): ResourcePost
    fun find(id: Int): ResourcePost?
    fun setStatus(id: Int, status: String): Boolean
    fun close(id: Int): Boolean

    /**
     * Restore a post's status + contact fields from a snapshot (revert of a reject/close, which had
     * scrubbed the contact). Returns true if the row existed.
     */
    fun restore(
        id: Int,
        status: String,
        contactPhone: String?,
        contactEmail: String?,
        contactName: String?,
        ownerSecret: String?,
    ): Boolean

    /** Closes every ACTIVE post created before [cutoff] (and scrubs its contact). Returns the count. */
    fun expireOlderThan(cutoff: LocalDateTime): Int

    /** Permanently deletes every post created before [cutoff], any status (60-day purge). Returns the count. */
    fun deleteOlderThan(cutoff: LocalDateTime): Int
}
