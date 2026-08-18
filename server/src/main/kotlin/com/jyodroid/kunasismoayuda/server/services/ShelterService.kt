package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.NewShelter
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter
import com.jyodroid.kunasismoayuda.server.domain.repositories.ShelterRepository
import com.jyodroid.kunasismoayuda.server.routes.dto.ShelterRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.ShelterResponse

class ShelterService(private val repository: ShelterRepository) {

    fun listActive(country: String = "CO"): List<ShelterResponse> =
        repository.listActive(country).map { it.toResponse() }

    fun create(request: ShelterRequest): ShelterResponse = repository.create(
        NewShelter(
            name = request.name,
            type = request.type,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            accepts = request.accepts,
            hours = request.hours,
            contactPhone = request.contactPhone,
            country = request.country,
        ),
    ).toResponse()

    fun update(id: Int, request: ShelterRequest): ShelterResponse? = repository.update(
        id,
        NewShelter(
            name = request.name,
            type = request.type,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            accepts = request.accepts,
            hours = request.hours,
            contactPhone = request.contactPhone,
            country = request.country,
        ),
    )?.toResponse()

    fun deactivate(id: Int): Boolean = repository.deactivate(id)

    private fun Shelter.toResponse() = ShelterResponse(
        id = id,
        name = name,
        type = type,
        address = address,
        latitude = latitude,
        longitude = longitude,
        accepts = accepts,
        hours = hours,
        contactPhone = contactPhone,
        verified = verified,
        lastVerified = lastVerified?.toString(),
    )
}
