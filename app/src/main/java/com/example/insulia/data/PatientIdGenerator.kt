package com.example.insulia.data

import java.security.SecureRandom

/**
 * Generates a candidate Patient ID in the `INS-XXXXXXXX` format.
 *
 * The alphabet omits `0`, `O`, `1` and `I` so the value is easier to read and
 * share without transcription mistakes. Generating a value does not make it
 * unique: the caller must reserve it in Firestore and generate another
 * candidate when the reservation already exists.
 */
object PatientIdGenerator {
    // 32 characters, so SecureRandom.nextInt(32) is unbiased.
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()

    fun generate(): String {
        val suffix = buildString(PatientIdFormat.LENGTH) {
            repeat(PatientIdFormat.LENGTH) {
                append(ALPHABET[random.nextInt(ALPHABET.length)])
            }
        }
        return PatientIdFormat.PREFIX + suffix
    }
}
