package br.ufg.inf.hemogramaapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.ufg.inf.hemogramaapp.model.Alert
import br.ufg.inf.hemogramaapp.network.ApiService
import kotlinx.coroutines.launch
import java.io.IOException

// Define o estado da UI para um melhor gerenciamento
data class AlertUiState(
    val alerts: List<Alert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel : ViewModel() {
    private val api = ApiService.create()

    var uiState by mutableStateOf(AlertUiState())
        private set

    init {
        fetchAlerts()
    }

    fun fetchAlerts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            uiState = try {
                val fetchedAlerts = api.getAlerts()
                uiState.copy(isLoading = false, alerts = fetchedAlerts)
            } catch (e: IOException) {
                uiState.copy(isLoading = false, error = "Falha ao buscar alertas: ${e.message}")
            } catch (e: Exception) {
                uiState.copy(isLoading = false, error = "Ocorreu um erro inesperado: ${e.message}")
            }
        }
    }
}
