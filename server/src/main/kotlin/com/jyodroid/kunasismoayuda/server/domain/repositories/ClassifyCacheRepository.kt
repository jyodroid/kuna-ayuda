package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint

/** A memoized classify + fact-check result for a given pasted-text hash. */
data class ClassifyCacheEntry(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactName: String?,
    val factCheck: String?,
    val checked: Boolean,
    val collectionPoints: List<CollectionPoint> = emptyList(),
)

/** Stores classify/fact-check results keyed by content hash so a repeat paste skips the paid calls. */
interface ClassifyCacheRepository {
    fun get(contentHash: String): ClassifyCacheEntry?
    fun put(contentHash: String, entry: ClassifyCacheEntry)
}
