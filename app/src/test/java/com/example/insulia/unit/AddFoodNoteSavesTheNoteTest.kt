package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.MealType
import org.junit.Assert.assertEquals
import org.junit.Test

class AddFoodNoteSavesTheNoteTest {
    @Test
    fun addFoodNote_savesTheNote() {
        val viewModel = InsuliaViewModel()

        viewModel.addFoodNote(MealType.DINNER, "Grilled chicken and vegetables")

        assertEquals(3, viewModel.foodNotes.size)
        assertEquals("Grilled chicken and vegetables", viewModel.latestFoodNote?.note)
    }
}
