package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewShelter
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter

interface ShelterRepository {
    fun listActive(country: String = "CO"): List<Shelter>
    fun create(shelter: NewShelter): Shelter
    /** Update an existing active shelter's editable fields; null if it doesn't exist / is inactive. */
    fun update(id: Int, shelter: NewShelter): Shelter?
    fun deactivate(id: Int): Boolean

    /** Any shelter by id, active or not (for the audit before-snapshot). */
    fun find(id: Int): Shelter?

    /** Re-activate a deactivated shelter (revert of a delete). True if the row existed. */
    fun reactivate(id: Int): Boolean
}
