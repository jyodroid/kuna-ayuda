package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.mapper.toDomain
import com.jyodroid.kunasismoayuda.core.data.remote.NewShelterDto
import com.jyodroid.kunasismoayuda.core.data.remote.ShelterApi
import com.jyodroid.kunasismoayuda.core.domain.model.NewShelter
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.core.domain.repository.ShelterRepository

class ShelterRepositoryImpl(
    private val api: ShelterApi,
    private val sessionManager: SessionManager,
) : ShelterRepository {
    override suspend fun getShelters(country: String): List<Shelter> =
        api.getShelters(country).map { it.toDomain() }

    override suspend fun create(shelter: NewShelter): Shelter =
        api.create(shelter.toDto(), sessionManager.requireToken()).toDomain()

    override suspend fun update(id: Int, shelter: NewShelter): Shelter =
        api.update(id, shelter.toDto(), sessionManager.requireToken()).toDomain()

    override suspend fun delete(id: Int) =
        api.delete(id, sessionManager.requireToken())

    private fun NewShelter.toDto() = NewShelterDto(
        name = name,
        type = type.name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        accepts = accepts,
        hours = hours,
        contactPhone = contactPhone,
        country = country,
    )
}
