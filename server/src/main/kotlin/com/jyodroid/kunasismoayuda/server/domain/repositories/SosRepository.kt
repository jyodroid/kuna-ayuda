package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SafeCheckIn
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosStats

interface SosRepository {
    fun create(report: NewSosReport): SosReport

    /** A single report by id (for the audit before-snapshot). */
    fun find(id: Int): SosReport?

    /**
     * Public reassurance list: SAFE check-ins for [country] that have a name, newest first.
     * Projects only id/name/region/created_at — coordinates and phone are never selected.
     * @param sinceDays only include check-ins from the last N days; [limit] caps the result.
     */
    fun listPublicSafe(country: String, sinceDays: Long, limit: Int): List<SafeCheckIn>

    /**
     * Reports for the responder view, newest first.
     * @param status "SOS" or "SAFE" to filter; null returns both.
     * @param archived false = pending only (handled_at null); true = archived only; null = both.
     */
    fun list(status: String?, archived: Boolean?): List<SosReport>

    /** Archive a report as attended/notified, stamping [by] (moderator email). Returns false if absent. */
    fun markHandled(id: Int, by: String?): Boolean

    /** Restore an archived report to the active list. Returns false if absent. */
    fun reopen(id: Int): Boolean

    /** Permanently delete a report. Returns false if absent. */
    fun delete(id: Int): Boolean

    /** Permanently deletes every report created before [cutoff] (60-day purge). Returns the count. */
    fun deleteOlderThan(cutoff: java.time.LocalDateTime): Int

    /** Pending-vs-handled counts for the responder dashboard. */
    fun stats(): SosStats
}
