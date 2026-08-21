package com.jyodroid.kunasismoayuda.server.domain.models

import kotlinx.serialization.Serializable

/**
 * A drop-off / collection / distribution point named in an aid post — where resources can be brought or
 * picked up. Extracted by the classify flow as structured data (kept as post content, not an official map
 * help point). Fields are empty strings when the post doesn't specify them.
 */
@Serializable
data class CollectionPoint(
    val name: String = "",
    val address: String = "",
    val hours: String = "",
)
