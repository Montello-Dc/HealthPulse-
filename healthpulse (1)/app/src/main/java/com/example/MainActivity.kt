package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.HealthPulseNavGraph
import com.example.ui.theme.HealthPulseTheme
import com.example.ui.viewmodel.HealthViewModel
import com.example.ui.viewmodel.HealthViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: HealthViewModel by viewModels {
        val app = application as HealthPulseApp
        HealthViewModelFactory(app.repository, app.authManager, app.geminiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthPulseTheme {
                HealthPulseNavGraph(viewModel = viewModel)
            }
        }
    }
}
