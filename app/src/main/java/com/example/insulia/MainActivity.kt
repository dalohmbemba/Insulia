package com.example.insulia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.insulia.ui.theme.InsuliaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContent {
                InsuliaTheme {
                    InsuliaApp()
                }
            }
        } catch (t: Throwable) {
            // Fallback to prevent crash
            try {
                setContent {
                    InsuliaTheme {
                        InsuliaApp()
                    }
                }
            } catch (_: Throwable) {
            }
        }
    }
}
