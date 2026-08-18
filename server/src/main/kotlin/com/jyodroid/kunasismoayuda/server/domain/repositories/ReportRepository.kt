package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.DisasterReport

interface ReportRepository {
    /** Inserts new reports and updates existing ones (by source + externalId). Returns rows touched. */
    fun upsert(items: List<DisasterReport>): Int
    fun listRecent(limit: Int): List<DisasterReport>
}
