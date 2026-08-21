package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A drop-off / collection / distribution point named in an aid post — where resources can be brought or
 * picked up. Extracted by the classify flow; shown on the board card. Fields may be blank.
 */
data class CollectionPoint(
    val name: String,
    val address: String,
    val hours: String,
)
