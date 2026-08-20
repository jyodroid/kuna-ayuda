package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewSearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import java.time.LocalDateTime

interface SearchRepository {
    fun listActive(subject: String?, state: String?, country: String = "CO"): List<SearchReport>
    fun create(report: NewSearchReport): SearchReport
    fun close(id: Int): Boolean

    /** Any report by id, active or not (for the audit before-snapshot). */
    fun find(id: Int): SearchReport?

    /** Re-open a closed report (revert of an admin delete). True if the row existed. */
    fun reopen(id: Int): Boolean

    /** Closes every ACTIVE report created before [cutoff]. Returns the count. */
    fun expireOlderThan(cutoff: LocalDateTime): Int
}
