package com.jyodroid.kunasismoayuda.server.domain.repositories

/** Persistent per-feature monthly call counter backing the spend cap. */
interface ApiUsageRepository {
    /**
     * Atomically record one call of [feature] in [period] ('YYYY-MM') if it's still under [limit].
     * Returns true if the call is allowed (and was counted), false if the limit is already reached.
     */
    fun tryConsume(feature: String, period: String, limit: Int): Boolean

    /** Unconditionally record one call (used to count a paid call only after it succeeded). */
    fun increment(feature: String, period: String)

    /** Current recorded count for [feature] in [period] (0 if none). */
    fun countFor(feature: String, period: String): Int
}
