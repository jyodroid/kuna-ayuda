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
)
