package com.jyodroid.kunasismoayuda.core.data.mapper

import com.jyodroid.kunasismoayuda.core.data.remote.FireDto
import com.jyodroid.kunasismoayuda.core.domain.model.Fire

fun FireDto.toDomain(): Fire = Fire(
    id = id,
    timeMillis = time,
    latitude = latitude,
    longitude = longitude,
    brightnessK = brightnessK,
    frpMw = frpMw,
    confidence = confidence,
    daynight = daynight,
    source = source,
    place = place,
)
