package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosStats

interface SosRepository {
    fun create(report: NewSosReport): SosReport

    /** A single report by id (for the audit before-snapshot). */
    fun find(id: Int): SosReport?

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

    /** Pending-vs-handled counts for the responder dashboard. */
    fun stats(): SosStats
}
