package com.example.insulia.data

/**
 * Chooses a Patient ID that is not already reserved.
 *
 * The generation and reservation reads happen inside the Firestore transaction
 * in [com.example.insulia.data.remote.FirestoreDataSource]. Keeping the retry
 * loop in one small function makes collision behaviour easy to test.
 */
internal object PatientIdAllocator {
    /** A collision is extremely unlikely, so this is only a safety net. */
    const val MAX_ATTEMPTS = 8

    fun allocate(
        generate: () -> String = PatientIdGenerator::generate,
        isReserved: (String) -> Boolean
    ): String {
        repeat(MAX_ATTEMPTS) {
            val candidate = generate()
            if (!isReserved(candidate)) {
                return candidate
            }
        }
        throw IllegalStateException("Unable to allocate a unique Patient ID")
    }
}
