package com.synosoftware.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synosoftware.battery.i18n.asString
import com.synosoftware.battery.i18n.text
import com.synosoftware.battery.ui.components.SectionHeader
import com.synosoftware.battery.ui.model.BatteryUiState

@Composable
fun HealthScreen(
    state: BatteryUiState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        item {
            SectionHeader(
                title = text("health_title").asString(),
                subtitle = text("health_subtitle").asString(),
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.usefulSessionCount < 5) {
                            text("health_not_enough_data").asString()
                        } else {
                            text("health_documented_in_backlog").asString()
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = state.healthMessage.asString(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(text("health_useful_sessions").asString(), style = MaterialTheme.typography.titleMedium)
                    Text(text("health_useful_sessions_count", state.usefulSessionCount).asString())
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
