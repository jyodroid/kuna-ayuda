package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PurgeServiceTest {

    private val boardRepo = mockk<ResourceBoardRepository>()
    private val searchRepo = mockk<SearchRepository>()
    private val sosRepo = mockk<SosRepository>()
    private val photoRepo = mockk<PhotoRepository>()
    private val service = PurgeService(boardRepo, searchRepo, sosRepo, photoRepo)

    @Test
    fun deletes_from_every_repo_with_a_60_day_cutoff_and_aggregates_counts() {
        val cutoff = slot<LocalDateTime>()
        every { boardRepo.deleteOlderThan(capture(cutoff)) } returns 3
        every { searchRepo.deleteOlderThan(any()) } returns 2
        every { sosRepo.deleteOlderThan(any()) } returns 1
        every { photoRepo.deleteOrphansOlderThan(any()) } returns 4

        val result = service.purgeOnce()

        assertEquals(PurgeService.PurgeResult(boardPosts = 3, searchReports = 2, sosReports = 1, orphanPhotos = 4), result)
        // The cutoff is ~PURGE_DAYS (60) days ago.
        val daysAgo = java.time.Duration.between(cutoff.captured, LocalDateTime.now()).toDays()
        assertTrue(daysAgo in 59..61, "expected ~60-day cutoff, was $daysAgo days")
        verify(exactly = 1) { boardRepo.deleteOlderThan(any()) }
        verify(exactly = 1) { searchRepo.deleteOlderThan(any()) }
        verify(exactly = 1) { sosRepo.deleteOlderThan(any()) }
        verify(exactly = 1) { photoRepo.deleteOrphansOlderThan(any()) }
    }

    @Test
    fun a_failing_table_is_isolated_and_the_rest_still_purge() {
        every { boardRepo.deleteOlderThan(any()) } throws RuntimeException("db down")
        every { searchRepo.deleteOlderThan(any()) } returns 5
        every { sosRepo.deleteOlderThan(any()) } returns 6
        every { photoRepo.deleteOrphansOlderThan(any()) } returns 0

        val result = service.purgeOnce()

        // Board failed → counted 0, but the others still ran.
        assertEquals(0, result.boardPosts)
        assertEquals(5, result.searchReports)
        assertEquals(6, result.sosReports)
        verify(exactly = 1) { sosRepo.deleteOlderThan(any()) }
    }
}
