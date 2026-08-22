package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.ClassifiedPreview
import com.jyodroid.kunasismoayuda.core.domain.model.NewResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType

/** Reads and publishes mutual-aid posts via the backend `/api/board`. */
interface ResourceBoardRepository {
    suspend fun list(kind: PostKind?, region: String?, type: ResourceType?, country: String = "CO"): List<ResourcePost>
    suspend fun create(post: NewResourcePost): ResourcePost

    /** Ids of posts this device created (owns) — the ones it can resolve. */
    suspend fun ownedPostIds(): Set<Int>

    /** Device-gated resolve of an owned post (#4). No-op if this device doesn't own [id]. */
    suspend fun resolve(id: Int)

    /**
     * Step 1: classify a pasted post and return a **preview** for the poster to review. Nothing is
     * queued yet — the poster confirms via [confirmClassification]. [kind] is the poster's own
     * REQUEST/OFFER choice; when non-null it overrides the model's guess.
     */
    suspend fun previewClassification(text: String, country: String = "CO", kind: PostKind? = null): ClassifiedPreview

    /** Step 2: the poster confirmed the preview → queue it as a moderated (pending) post. */
    suspend fun confirmClassification(text: String, country: String = "CO", kind: PostKind? = null): ResourcePost

    /**
     * Image intake — classify a SCREENSHOT/photo of a post via vision (Instagram blocks copying text).
     * Returns a preview (with a `cacheRef` used by [confirmClassificationImage]).
     */
    suspend fun previewClassificationImage(bytes: ByteArray, mime: String, country: String = "CO", kind: PostKind? = null): ClassifiedPreview

    /** Confirm an image classify by the preview's `cacheRef` (no image re-upload) → queue PENDING. */
    suspend fun confirmClassificationImage(cacheRef: String, country: String = "CO", kind: PostKind? = null): ResourcePost

    // --- Moderator-only (require an authenticated ADMIN session) ---

    /** Posts awaiting moderation (status PENDING). */
    suspend fun listPending(): List<ResourcePost>

    /** Published (ACTIVE) posts, so a moderator can find and remove an abusive live post. */
    suspend fun listActive(): List<ResourcePost>

    /** Publish a pending post (make it ACTIVE). */
    suspend fun approve(id: Int)

    /** Reject a pending post, or delete a published one (both close it via DELETE /api/board/{id}). */
    suspend fun reject(id: Int)
}
