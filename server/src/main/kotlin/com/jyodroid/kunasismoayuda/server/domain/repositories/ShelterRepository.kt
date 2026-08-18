package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewShelter
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter

interface ShelterRepository {
    fun listActive(country: String = "CO"): List<Shelter>
    fun create(shelter: NewShelter): Shelter
    /** Update an existing active shelter's editable fields; null if it doesn't exist / is inactive. */
    fun update(id: Int, shelter: NewShelter): Shelter?
    fun deactivate(id: Int): Boolean
}
