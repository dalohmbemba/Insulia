package com.example.insulia.unit

import com.example.insulia.data.DiabetesType
import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationFilteredByDiabetesTypeTest {
    @Test
    fun medicationFilteredByDiabetesType_type1AndType2() {
        val viewModel = InsuliaViewModel()
        val type1 = viewModel.medications.filter { it.diabetesType == DiabetesType.TYPE_1 }
        val type2 = viewModel.medications.filter { it.diabetesType == DiabetesType.TYPE_2 }
        assertTrue(type1.isNotEmpty())
        assertTrue(type2.isNotEmpty())
    }
}
