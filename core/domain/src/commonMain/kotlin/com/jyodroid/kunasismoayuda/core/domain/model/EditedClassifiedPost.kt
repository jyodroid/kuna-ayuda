package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * The core fields of a classify preview after the poster reviewed and (optionally) corrected them —
 * sent on confirm to queue a moderated post. The moderation signals (fact-check, risk flags) and the
 * read-only collection points are NOT here: the server keeps them from the cached classify, so a poster
 * can't strip them.
 */
data class EditedClassifiedPost(
    val kind: PostKind,
    val resourceType: ResourceType,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactName: String?,
)
