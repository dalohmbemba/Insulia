package com.example.insulia.unit

import com.example.insulia.data.PatientIdAllocator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PatientIdAllocatorTest {

    @Test
    fun returnsTheFirstCandidateWhenItIsNotReserved() {
        var attempts = 0
        val patientId = PatientIdAllocator.allocate(
            generate = {
                attempts += 1
                "INS-AAAAAAAA"
            },
            isReserved = { false }
        )

        assertEquals("INS-AAAAAAAA", patientId)
        assertEquals(1, attempts)
    }

    @Test
    fun retriesWithANewCandidateWhenTheIdIsReserved() {
        val candidates = listOf("INS-AAAAAAAA", "INS-BBBBBBBB", "INS-CCCCCCCC")
        var index = 0
        val checked = mutableListOf<String>()

        val patientId = PatientIdAllocator.allocate(
            generate = { candidates[index++] },
            isReserved = { candidate ->
                checked += candidate
                candidate != "INS-CCCCCCCC"
            }
        )

        assertEquals("INS-CCCCCCCC", patientId)
        assertEquals(candidates, checked)
    }

    @Test
    fun stopsAfterTheMaximumNumberOfCollisions() {
        var attempts = 0

        assertThrows(IllegalStateException::class.java) {
            PatientIdAllocator.allocate(
                generate = { "INS-AAAAAAAA" },
                isReserved = {
                    attempts += 1
                    true
                }
            )
        }
        assertEquals(PatientIdAllocator.MAX_ATTEMPTS, attempts)
    }
}
