package com.jyodroid.kunasismoayuda.core.data.mapper

import com.jyodroid.kunasismoayuda.core.data.remote.ShelterDto
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.core.domain.model.ShelterType

fun ShelterDto.toDomain(): Shelter = Shelter(
    id = id,
    name = name,
    type = ShelterType.fromRaw(type),
    address = address,
    latitude = latitude,
    longitude = longitude,
    accepts = accepts,
    hours = hours,
    contactPhone = contactPhone,
    verified = verified,
    lastVerified = lastVerified,
)
