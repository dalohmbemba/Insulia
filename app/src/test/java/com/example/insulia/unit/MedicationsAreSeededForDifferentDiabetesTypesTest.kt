package com.example.insulia.unit

import com.example.insulia.data.DiabetesType
import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationsAreSeededForDifferentDiabetesTypesTest {
    @Test
    fun medications_areSeededForDifferentDiabetesTypes() {
        val viewModel = InsuliaViewModel()

        assertTrue(viewModel.medications.isNotEmpty())
        assertTrue(viewModel.medications.any { it.diabetesType == DiabetesType.TYPE_1 })
        assertTrue(viewModel.medications.any { it.diabetesType == DiabetesType.TYPE_2 })
    }
}
