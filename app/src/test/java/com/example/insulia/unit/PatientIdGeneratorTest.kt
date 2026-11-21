package com.example.insulia.unit

import com.example.insulia.data.PatientIdFormat
import com.example.insulia.data.PatientIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientIdGeneratorTest {

    @Test
    fun generatedIdsMatchThePatientIdFormat() {
        repeat(1_000) {
            val patientId = PatientIdGenerator.generate()
            assertTrue("Unexpected Patient ID: $patientId", PatientIdFormat.isValid(patientId))
            assertEquals("INS-", patientId.take(PatientIdFormat.PREFIX.length))
            assertEquals(
                PatientIdFormat.PREFIX.length + PatientIdFormat.LENGTH,
                patientId.length
            )
        }
    }

    @Test
    fun generatedIdsUseOnlyTheUnambiguousAlphabet() {
        val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()
        repeat(500) {
            val suffix = PatientIdGenerator.generate().removePrefix(PatientIdFormat.PREFIX)
            assertTrue("Ambiguous character in $suffix", suffix.all { it in allowed })
        }
    }

    @Test
    fun formatAcceptsValidIds() {
        assertTrue(PatientIdFormat.isValid("INS-ABCDEFGH"))
        assertTrue(PatientIdFormat.isValid("INS-23456789"))
        assertTrue(PatientIdFormat.isValid("INS-A2B3C4D5"))
    }

    @Test
    fun formatRejectsMalformedIds() {
        assertFalse(PatientIdFormat.isValid(""))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFG"))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFGHI"))
        assertFalse(PatientIdFormat.isValid("ins-ABCDEFGH"))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFG0"))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFG1"))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFGO"))
        assertFalse(PatientIdFormat.isValid("INS-ABCDEFGI"))
        assertFalse(PatientIdFormat.isValid("INS-ABCD EFGH"))
    }
}
