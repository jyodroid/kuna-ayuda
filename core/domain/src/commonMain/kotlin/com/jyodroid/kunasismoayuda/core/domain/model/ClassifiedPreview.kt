package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * What Claude extracted from a pasted post, shown back to the poster to review BEFORE it's sent to a
 * moderator. Nothing is persisted at this point — the poster confirms to queue it.
 */
data class ClassifiedPreview(
    val kind: PostKind,
    val resourceType: ResourceType,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactName: String?,
    val factCheck: String?,
    val collectionPoints: List<CollectionPoint> = emptyList(),
    val riskFlags: List<String> = emptyList(),
    // Opaque handle to the cached classify result — lets the poster confirm an IMAGE classify without
    // re-uploading the screenshot. Null for the text path.
    val cacheRef: String? = null,
)
