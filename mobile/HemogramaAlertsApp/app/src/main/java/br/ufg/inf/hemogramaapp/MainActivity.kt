package br.ufg.inf.hemogramaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import br.ufg.inf.hemogramaapp.ui.AlertListScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.fetchAlerts()
            }
            AlertListScreen(uiState = viewModel.uiState)
        }
    }
}
