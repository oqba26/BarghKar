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
import com.oqba26.barghkar.ui.viewmodels.OhmLawViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhmLawScreen(viewModel: OhmLawViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.ohm_law)) })
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
                value = viewModel.voltage,
                onValueChange = { viewModel.onVoltageChange(it) },
                label = { Text(stringResource(R.string.voltage_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = viewModel.current,
                onValueChange = { viewModel.onCurrentChange(it) },
                label = { Text(stringResource(R.string.current_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = viewModel.resistance,
                onValueChange = { viewModel.onResistanceChange(it) },
                label = { Text(stringResource(R.string.resistance_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            HorizontalDivider()
            
            Text(
                text = stringResource(R.string.power_consumption, viewModel.power),
                style = MaterialTheme.typography.titleLarge
            )
            
            Button(
                onClick = { viewModel.clear() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}
