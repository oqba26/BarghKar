package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R
import com.oqba26.barghkar.domain.ReferenceData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorCodeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.wire_color_codes) + " (IEC)") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ReferenceData.iecColors) { wire ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(wire.color)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = wire.type, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${wire.colorName} - ${wire.description}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
