package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.mapper.toDomain
import com.jyodroid.kunasismoayuda.core.data.mapper.toDto
import com.jyodroid.kunasismoayuda.core.data.remote.BoardApi
import com.jyodroid.kunasismoayuda.core.data.remote.ConfirmClassifyRequestDto
import com.jyodroid.kunasismoayuda.core.data.settings.PostOwnershipStore
import com.jyodroid.kunasismoayuda.core.domain.model.ClassifiedPreview
import com.jyodroid.kunasismoayuda.core.domain.model.EditedClassifiedPost
import com.jyodroid.kunasismoayuda.core.domain.model.NewResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import com.jyodroid.kunasismoayuda.core.domain.repository.ResourceBoardRepository

class ResourceBoardRepositoryImpl(
    private val api: BoardApi,
    private val sessionManager: SessionManager,
    private val ownership: PostOwnershipStore,
) : ResourceBoardRepository {

    override suspend fun list(kind: PostKind?, region: String?, type: ResourceType?, country: String): List<ResourcePost> =
        api.list(kind?.name, region?.ifBlank { null }, type?.name, country).map { it.toDomain() }

    override suspend fun create(post: NewResourcePost): ResourcePost {
        val created = api.create(post.toDto()).toDomain()
        // Persist the one-time owner secret so this device can later resolve its own post.
        created.ownerSecret?.let { ownership.remember(created.id, it) }
        return created
    }

    override suspend fun ownedPostIds(): Set<Int> = ownership.ownedIds()

    override suspend fun resolve(id: Int) {
        val secret = ownership.secretFor(id) ?: return // not our post — nothing to do
        api.resolve(id, secret)
        ownership.forget(id)
    }

    override suspend fun previewClassification(text: String, country: String, kind: PostKind?): ClassifiedPreview =
        api.classifyPreview(text, country, kind?.name).toDomain()

    override suspend fun previewClassificationImage(bytes: ByteArray, mime: String, country: String, kind: PostKind?): ClassifiedPreview =
        api.classifyImage(bytes, mime, country, kind?.name).toDomain()

    override suspend fun confirmClassifiedEdited(
        edited: EditedClassifiedPost,
        cacheRef: String,
        rawText: String?,
        country: String,
    ): ResourcePost = api.confirmEdit(
        ConfirmClassifyRequestDto(
            cacheRef = cacheRef,
            kind = edited.kind.name,
            resourceType = edited.resourceType.name,
            region = edited.region,
            description = edited.description,
            contactPhone = edited.contactPhone,
            contactName = edited.contactName,
            rawText = rawText,
            country = country,
        ),
    ).toDomain()

    override suspend fun listPending(): List<ResourcePost> =
        api.listPending(sessionManager.requireToken()).map { it.toDomain() }

    override suspend fun listActive(): List<ResourcePost> =
        api.listActive(sessionManager.requireToken()).map { it.toDomain() }

    override suspend fun approve(id: Int) = api.approve(id, sessionManager.requireToken())

    override suspend fun reject(id: Int) = api.reject(id, sessionManager.requireToken())
}
