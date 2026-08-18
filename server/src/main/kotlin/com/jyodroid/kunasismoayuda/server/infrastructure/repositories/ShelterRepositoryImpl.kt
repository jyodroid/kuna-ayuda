package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.NewShelter
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.Shelters
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

class ShelterRepositoryImpl : com.jyodroid.kunasismoayuda.server.domain.repositories.ShelterRepository {

    override fun listActive(country: String): List<Shelter> {
        if (!DatabaseFactory.initialized) return emptyList()
        val code = country.uppercase()
        return transaction {
            Shelters.selectAll()
                .where { (Shelters.active eq true) and (Shelters.country eq code) }
                .map { it.toShelter() }
        }
    }

    override fun create(shelter: NewShelter): Shelter = transaction {
        val insertedId = Shelters.insert {
            it[name] = shelter.name
            it[type] = shelter.type
            it[address] = shelter.address
            it[latitude] = shelter.latitude
            it[longitude] = shelter.longitude
            it[accepts] = shelter.accepts
            it[hours] = shelter.hours
            it[contactPhone] = shelter.contactPhone
            it[verified] = true
            it[lastVerified] = LocalDate.now()
            it[active] = true
            it[country] = shelter.country.uppercase()
        } get Shelters.id

        Shelters.selectAll().where { Shelters.id eq insertedId }.single().toShelter()
    }

    override fun update(id: Int, shelter: NewShelter): Shelter? = transaction {
        val updated = Shelters.update({ (Shelters.id eq id) and (Shelters.active eq true) }) {
            it[name] = shelter.name
            it[type] = shelter.type
            it[address] = shelter.address
            it[latitude] = shelter.latitude
            it[longitude] = shelter.longitude
            it[accepts] = shelter.accepts
            it[hours] = shelter.hours
            it[contactPhone] = shelter.contactPhone
            it[lastVerified] = LocalDate.now()
            it[country] = shelter.country.uppercase()
        }
        if (updated > 0) {
            Shelters.selectAll().where { Shelters.id eq id }.single().toShelter()
        } else {
            null
        }
    }

    override fun deactivate(id: Int): Boolean = transaction {
        Shelters.update({ Shelters.id eq id }) { it[active] = false } > 0
    }

    private fun ResultRow.toShelter() = Shelter(
        id = this[Shelters.id],
        name = this[Shelters.name],
        type = this[Shelters.type],
        address = this[Shelters.address],
        latitude = this[Shelters.latitude],
        longitude = this[Shelters.longitude],
        accepts = this[Shelters.accepts],
        hours = this[Shelters.hours],
        contactPhone = this[Shelters.contactPhone],
        verified = this[Shelters.verified],
        lastVerified = this[Shelters.lastVerified],
        active = this[Shelters.active],
        country = this[Shelters.country],
    )
}
