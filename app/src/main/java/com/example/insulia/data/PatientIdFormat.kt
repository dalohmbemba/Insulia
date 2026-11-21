package com.example.insulia.data

/**
 * The format of an Insulia Patient ID.
 *
 * IDs are generated on the device with [PatientIdGenerator] and reserved in
 * Firestore so that no two patients can hold the same value. The app never
 * lets a patient type or edit an ID.
 */
object PatientIdFormat {
    const val PREFIX = "INS-"
    const val LENGTH = 8

    /**
     * `INS-` followed by eight characters from the unambiguous alphabet.
     *
     * The alphabet excludes the easily confused characters `0`, `O`, `1` and
     * `I`, so validation only accepts letters and `2`-`9` after the prefix.
     */
    private val pattern = Regex("^INS-[A-HJ-NP-Z2-9]{8}$")

    fun isValid(value: String): Boolean = pattern.matches(value)
}
