package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.mapper.toDomain
import com.jyodroid.kunasismoayuda.core.data.remote.QuakeApi
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.repository.QuakeRepository

class QuakeRepositoryImpl(
    private val api: QuakeApi,
) : QuakeRepository {
    override suspend fun getQuakes(minMagnitude: Double, country: String): List<Quake> =
        api.getQuakes(minMagnitude, country).map { it.toDomain() }
}
