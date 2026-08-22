package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.viewmodels.VoltageDropViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDropScreen(viewModel: VoltageDropViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.voltage_drop)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.sourceVoltage,
                onValueChange = { viewModel.onSourceVoltageChange(it) },
                label = { Text(stringResource(R.string.source_voltage)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = viewModel.length,
                onValueChange = { viewModel.onLengthChange(it) },
                label = { Text(stringResource(R.string.path_length)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = viewModel.current,
                onValueChange = { viewModel.onCurrentChange(it) },
                label = { Text(stringResource(R.string.consumption_current)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = viewModel.resistancePerKm,
                onValueChange = { viewModel.onResistanceChange(it) },
                label = { Text(stringResource(R.string.wire_resistance)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            HorizontalDivider()
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.voltage_drop_amount, viewModel.voltageDrop),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.voltage_drop_percent, viewModel.percentageDrop),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
