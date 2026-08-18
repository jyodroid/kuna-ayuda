package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.repositories.ApiUsageRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

/** Raised when a feature's monthly call cap is reached; routes map it to HTTP 429. */
class UsageLimitReachedException(val feature: String, val limit: Int) :
    RuntimeException("Monthly limit ($limit) reached for '$feature'")

/**
 * A hard **spend cap** for paid/quota external calls (Anthropic classify, Google Fact Check). This is
 * a personally-funded project, so each metered feature has an owner-configured **monthly** limit; once
 * the limit is hit the feature stops calling out and callers get a clean error (429), rather than the
 * bill growing unbounded.
 *
 * Limits come from env (per feature): unset/blank ⇒ **unlimited**; `0` ⇒ **disabled**; `N>0` ⇒ N calls
 * per calendar month (UTC). Counting is persistent via [ApiUsageRepository] when a DB is configured
 * (survives restarts); otherwise an in-memory fallback keeps the cap working in dev.
 */
class UsageLimiter(
    private val repository: ApiUsageRepository,
    private val limits: Map<String, Int?>,
) {
    private val logger = LoggerFactory.getLogger(UsageLimiter::class.java)
    private val memory = ConcurrentHashMap<String, Int>()

    companion object {
        const val FEATURE_ANTHROPIC = "anthropic"
        const val FEATURE_FACTCHECK = "factcheck"

        /** Build the limits map from env vars. */
        fun limitsFromEnv(getenv: (String) -> String? = System::getenv): Map<String, Int?> = mapOf(
            FEATURE_ANTHROPIC to parseLimit(getenv("ANTHROPIC_MONTHLY_LIMIT")),
            FEATURE_FACTCHECK to parseLimit(getenv("FACTCHECK_MONTHLY_LIMIT")),
        )

        /** null ⇒ unlimited (unset/blank/negative/garbage); otherwise the parsed non-negative cap. */
        private fun parseLimit(raw: String?): Int? {
            val n = raw?.trim()?.toIntOrNull() ?: return null
            return if (n < 0) null else n
        }
    }

    /** null ⇒ unlimited for this feature. */
    fun limitFor(feature: String): Int? = limits[feature]

    /** How many calls have been made this month for [feature]. */
    fun usedThisMonth(feature: String): Int {
        val period = currentPeriod()
        return if (DatabaseFactory.initialized) repository.countFor(feature, period)
        else memory[key(feature, period)] ?: 0
    }

    /**
     * Try to consume one call of [feature]. Returns true if allowed (and counted), false if the cap is
     * reached (or the feature is disabled with limit 0). Unlimited features always return true.
     */
    fun tryConsume(feature: String): Boolean {
        val limit = limits[feature] ?: return true // unlimited
        if (limit <= 0) {
            logger.info("[usage] '$feature' is disabled (limit=0).")
            return false
        }
        val period = currentPeriod()
        val allowed = if (DatabaseFactory.initialized) {
            repository.tryConsume(feature, period, limit)
        } else {
            memTryConsume(feature, period, limit)
        }
        if (!allowed) logger.warn("[usage] monthly limit ($limit) reached for '$feature' ($period).")
        return allowed
    }

    /**
     * Non-consuming check: is [feature] currently under its cap? Unlimited ⇒ true, disabled (0) ⇒ false.
     * Use this BEFORE a paid call, then [record] AFTER it succeeds, so failed calls don't burn budget.
     */
    fun check(feature: String): Boolean {
        val limit = limits[feature] ?: return true // unlimited
        if (limit <= 0) return false               // disabled
        return usedThisMonth(feature) < limit
    }

    /** Record one successful call of [feature] (no-op for unlimited/disabled features). */
    fun record(feature: String) {
        val limit = limits[feature] ?: return
        if (limit <= 0) return
        val period = currentPeriod()
        if (DatabaseFactory.initialized) repository.increment(feature, period)
        else synchronized(this) { val k = key(feature, period); memory[k] = (memory[k] ?: 0) + 1 }
    }

    /** Check-or-throw variant for the classify path (paired with [record] after a successful call). */
    fun require(feature: String) {
        if (!check(feature)) throw UsageLimitReachedException(feature, limits[feature] ?: 0)
    }

    @Synchronized
    private fun memTryConsume(feature: String, period: String, limit: Int): Boolean {
        val k = key(feature, period)
        val current = memory[k] ?: 0
        if (current >= limit) return false
        memory[k] = current + 1
        return true
    }

    private fun key(feature: String, period: String) = "$feature:$period"

    private fun currentPeriod(): String {
        val today = LocalDate.now(ZoneOffset.UTC)
        return "%04d-%02d".format(today.year, today.monthValue)
    }
}
