package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.Disaster

interface DisasterRepository {
    /** Inserts new disasters and updates existing ones (by source + externalId). Returns rows touched. */
    fun upsert(items: List<Disaster>): Int
    fun listRecent(limit: Int): List<Disaster>
}
