package br.ufg.inf.hemogramaapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.ufg.inf.hemogramaapp.AlertUiState
import br.ufg.inf.hemogramaapp.model.Alert

@Composable
fun AlertListScreen(uiState: AlertUiState) {
    when {
        uiState.isLoading -> {
            // Exibe um indicador de progresso enquanto carrega
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            // Exibe uma mensagem de erro se a busca falhar
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            // Exibe a lista de alertas
            AlertList(alerts = uiState.alerts)
        }
    }
}

@Composable
fun AlertList(alerts: List<Alert>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(alerts) {
            alert -> AlertItem(alert = alert)
        }
    }
}

@Composable
fun AlertItem(alert: Alert, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = alert.message, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Paciente: ${alert.region}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Recebido em: ${alert.parameter}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
