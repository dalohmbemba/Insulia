package com.example.insulia.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.insulia.data.GlucoseStatus
import com.example.insulia.ui.theme.InsuliaHigh
import com.example.insulia.ui.theme.InsuliaLow
import com.example.insulia.ui.theme.InsuliaNormal

/** The colour used to show a glucose status (low, in range, high). */
@Composable
fun glucoseStatusColor(status: GlucoseStatus): Color = when (status) {
    GlucoseStatus.LOW -> InsuliaLow
    GlucoseStatus.IN_RANGE -> InsuliaNormal
    GlucoseStatus.HIGH -> InsuliaHigh
}
