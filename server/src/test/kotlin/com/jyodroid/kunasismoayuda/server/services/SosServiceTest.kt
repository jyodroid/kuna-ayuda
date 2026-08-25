package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SafeCheckIn
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import com.jyodroid.kunasismoayuda.server.routes.dto.SosRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SosServiceTest {

    @Test
    fun `public safe list maps to name+region+time and uppercases country`() {
        val repo = mockk<SosRepository>()
        every { repo.listPublicSafe("ES", any(), any()) } returns
            listOf(SafeCheckIn(id = 7, displayName = "María", region = "Madrid", createdAtEpochMs = 123L))
        val service = SosService(repo)

        val out = service.listPublicSafe("es")

        assertEquals(1, out.size)
        assertEquals(7, out[0].id)
        assertEquals("María", out[0].name)
        assertEquals("Madrid", out[0].region)
        assertEquals(123L, out[0].createdAtEpochMs)
        verify { repo.listPublicSafe("ES", SosService.SAFE_WINDOW_DAYS, SosService.SAFE_LIMIT) }
    }

    @Test
    fun `create trims the name and defaults the country to CO`() {
        val repo = mockk<SosRepository>()
        val captured = slot<NewSosReport>()
        every { repo.create(capture(captured)) } returns SosReport(
            id = 1, status = "SAFE", latitude = null, longitude = null, region = "Bogotá",
            message = null, contactPhone = null, displayName = "Ana", country = "CO",
            createdAt = LocalDateTime.now(),
        )
        val service = SosService(repo)

        service.create(SosRequest(status = "safe", region = "Bogotá", displayName = "  Ana  "))

        assertEquals("Ana", captured.captured.displayName)
        assertEquals("CO", captured.captured.country)
        assertEquals("SAFE", captured.captured.status)
    }
}
