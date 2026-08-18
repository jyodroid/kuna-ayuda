package com.jyodroid.kunasismoayuda.core.data.mapper

import com.jyodroid.kunasismoayuda.core.data.remote.ClassifyPreviewDto
import com.jyodroid.kunasismoayuda.core.data.remote.NewResourcePostDto
import com.jyodroid.kunasismoayuda.core.data.remote.ResourcePostDto
import com.jyodroid.kunasismoayuda.core.domain.model.ClassifiedPreview
import com.jyodroid.kunasismoayuda.core.domain.model.NewResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType

fun ResourcePostDto.toDomain(): ResourcePost = ResourcePost(
    id = id,
    kind = PostKind.fromRaw(kind),
    resourceType = ResourceType.fromRaw(resourceType),
    region = region,
    description = description,
    contactPhone = contactPhone,
    contactEmail = contactEmail,
    contactName = contactName,
    createdAt = createdAt,
    source = source,
    rawText = rawText,
    factCheck = factCheck,
    ownerSecret = ownerSecret,
)

fun ClassifyPreviewDto.toDomain(): ClassifiedPreview = ClassifiedPreview(
    kind = PostKind.fromRaw(kind),
    resourceType = ResourceType.fromRaw(resourceType),
    region = region,
    description = description,
    contactPhone = contactPhone,
    contactName = contactName,
    factCheck = factCheck,
)

fun NewResourcePost.toDto(): NewResourcePostDto = NewResourcePostDto(
    kind = kind.name,
    resourceType = resourceType.name,
    region = region,
    description = description,
    contactPhone = contactPhone,
    contactEmail = contactEmail,
    contactName = contactName,
    country = country,
)
