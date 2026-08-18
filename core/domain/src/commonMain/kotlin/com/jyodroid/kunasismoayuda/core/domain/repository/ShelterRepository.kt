package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.NewShelter
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter

/** Reads the moderated list of shelters/acopios from the backend (`GET /api/shelters`). */
interface ShelterRepository {
    suspend fun getShelters(country: String = "CO"): List<Shelter>

    /** Admin-only: create a shelter (`POST /api/shelters`, bearer token). Returns the created row. */
    suspend fun create(shelter: NewShelter): Shelter

    /** Admin-only: update an existing shelter (`PUT /api/shelters/{id}`). Returns the updated row. */
    suspend fun update(id: Int, shelter: NewShelter): Shelter

    /** Admin-only: remove a shelter (`DELETE /api/shelters/{id}`). */
    suspend fun delete(id: Int)
}
