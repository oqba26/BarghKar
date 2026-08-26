package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.domain.ElectricalSupplies
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
) {
    var showDialog by remember { mutableStateOf(value = false) }
    val inventory by viewModel.allInventory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.inventory)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_to_inventory))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventory) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.material_quantity_format, item.quantity, item.unit),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { viewModel.deleteInventoryItem(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf("") }
            var unit by remember { mutableStateOf("عدد") }
            
            var nameExpanded by remember { mutableStateOf(false) }
            var unitExpanded by remember { mutableStateOf(false) }

            val filteredItems = remember(name) {
                ElectricalSupplies.commonItems.filter { it.name.contains(name, ignoreCase = true) }
            }

            CustomDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.new_material)) },
                text = {
                    Column {
                        // ردیف پیشنهادات سریع
                        Text(text = "پیشنهادات سریع:", style = MaterialTheme.typography.labelSmall)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElectricalSupplies.commonItems.take(8).forEach { item ->
                                item {
                                    FilterChip(
                                        selected = name == item.name,
                                        onClick = {
                                            name = item.name
                                            unit = item.defaultUnit
                                        },
                                        label = { Text(item.name) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = nameExpanded,
                            onExpandedChange = { nameExpanded = !nameExpanded }
                        ) {
                            TextField(
                                value = name,
                                onValueChange = { 
                                    name = it
                                    nameExpanded = true
                                },
                                label = { Text(stringResource(R.string.item_name)) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameExpanded) }
                            )
                            if (filteredItems.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = nameExpanded,
                                    onDismissRequest = { nameExpanded = false }
                                ) {
                                    filteredItems.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item.name) },
                                            onClick = {
                                                name = item.name
                                                unit = item.defaultUnit
                                                nameExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = quantity, 
                            onValueChange = { quantity = it }, 
                            label = { Text(stringResource(R.string.quantity)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded }
                        ) {
                            TextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text(stringResource(R.string.unit_hint)) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                ElectricalSupplies.commonUnits.forEach { commonUnit ->
                                    DropdownMenuItem(
                                        text = { Text(commonUnit) },
                                        onClick = {
                                            unit = commonUnit
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val q = quantity.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && q > 0) {
                                viewModel.addInventoryItem(name, q, unit)
                                showDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Red)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
