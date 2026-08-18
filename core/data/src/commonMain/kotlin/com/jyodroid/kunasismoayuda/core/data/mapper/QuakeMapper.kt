package com.jyodroid.kunasismoayuda.core.data.mapper

import com.jyodroid.kunasismoayuda.core.data.remote.QuakeDto
import com.jyodroid.kunasismoayuda.core.domain.model.Quake

fun QuakeDto.toDomain(): Quake = Quake(
    id = id,
    timeMillis = time,
    magnitude = magnitude,
    depthKm = depthKm,
    latitude = latitude,
    longitude = longitude,
    place = place,
    source = source,
    url = url,
)
