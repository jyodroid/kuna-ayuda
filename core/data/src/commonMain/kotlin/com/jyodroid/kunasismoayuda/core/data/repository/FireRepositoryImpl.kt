package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.mapper.toDomain
import com.jyodroid.kunasismoayuda.core.data.remote.FireApi
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.core.domain.repository.FireRepository

class FireRepositoryImpl(
    private val api: FireApi,
) : FireRepository {
    override suspend fun getFires(country: String): List<Fire> =
        api.getFires(country).map { it.toDomain() }
}
