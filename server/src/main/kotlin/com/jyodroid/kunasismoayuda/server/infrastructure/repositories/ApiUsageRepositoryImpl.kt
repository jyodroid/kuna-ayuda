package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.repositories.ApiUsageRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.ApiUsage
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Postgres-backed usage counter. Single small transaction per call (read → check → increment). Not
 * strictly serialized against concurrent callers, so under heavy concurrency the cap can overshoot by
 * a few calls — acceptable for a spend guard on a low-traffic single instance (the point is to stop the
 * budget being blown, not bill-accurate metering). Persistence means the monthly count survives
 * restarts, unlike an in-memory counter.
 */
class ApiUsageRepositoryImpl : ApiUsageRepository {

    override fun tryConsume(feature: String, period: String, limit: Int): Boolean {
        if (!DatabaseFactory.initialized) return true // no store → don't block (dev)
        return transaction {
            val current = ApiUsage.selectAll()
                .where { (ApiUsage.feature eq feature) and (ApiUsage.period eq period) }
                .singleOrNull()?.get(ApiUsage.count)
                ?: run {
                    ApiUsage.insert {
                        it[ApiUsage.feature] = feature
                        it[ApiUsage.period] = period
                        it[count] = 0
                    }
                    0
                }
            if (current >= limit) {
                false
            } else {
                ApiUsage.update({ (ApiUsage.feature eq feature) and (ApiUsage.period eq period) }) {
                    it[count] = current + 1
                }
                true
            }
        }
    }

    override fun increment(feature: String, period: String) {
        if (!DatabaseFactory.initialized) return
        transaction {
            val current = ApiUsage.selectAll()
                .where { (ApiUsage.feature eq feature) and (ApiUsage.period eq period) }
                .singleOrNull()?.get(ApiUsage.count)
            if (current == null) {
                ApiUsage.insert {
                    it[ApiUsage.feature] = feature
                    it[ApiUsage.period] = period
                    it[count] = 1
                }
            } else {
                ApiUsage.update({ (ApiUsage.feature eq feature) and (ApiUsage.period eq period) }) {
                    it[count] = current + 1
                }
            }
        }
    }

    override fun countFor(feature: String, period: String): Int {
        if (!DatabaseFactory.initialized) return 0
        return transaction {
            ApiUsage.selectAll()
                .where { (ApiUsage.feature eq feature) and (ApiUsage.period eq period) }
                .singleOrNull()?.get(ApiUsage.count) ?: 0
        }
    }
}
