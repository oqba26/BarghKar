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
fun ReferencesScreen(
    onNavigateToColorCodes: () -> Unit,
    onNavigateToSymbols: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.references_and_standards)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReferenceItem(title = stringResource(R.string.wire_color_codes), onClick = onNavigateToColorCodes)
            ReferenceItem(title = stringResource(R.string.electrical_symbols), onClick = onNavigateToSymbols)
        }
    }
}

@Composable
fun ReferenceItem(title: String, onClick: () -> Unit) {
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
