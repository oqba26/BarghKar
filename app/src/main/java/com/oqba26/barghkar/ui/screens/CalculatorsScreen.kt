package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    onNavigateToOhmLaw: () -> Unit,
    onNavigateToVoltageDrop: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.technical_calculations)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorItem(title = stringResource(R.string.ohm_law), onClick = onNavigateToOhmLaw)
            CalculatorItem(title = stringResource(R.string.voltage_drop), onClick = onNavigateToVoltageDrop)
        }
    }
}

@Composable
fun CalculatorItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
