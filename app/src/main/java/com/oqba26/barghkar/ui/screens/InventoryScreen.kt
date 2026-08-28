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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.utils.NumberUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.domain.ElectricalSupplies
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.InventoryViewModel
import com.oqba26.barghkar.utils.VibrationUtils
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(value = false) }
    var itemToDelete by remember { mutableStateOf<com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity?>(null) }
    val inventory by viewModel.allInventory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventory)) },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_to_inventory))
                    }
                }
            )
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
                                text = stringResource(R.string.material_quantity_format, NumberUtils.formatNumber(item.quantity, useEnglishNumbers), item.unit),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { itemToDelete = item }) {
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
                            onValueChange = { 
                                val englishDigits = NumberUtils.englishizeDigits(it)
                                if (englishDigits.all { char -> char.isDigit() || char == '.' }) {
                                    quantity = englishDigits
                                } else {
                                    android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }, 
                            label = { Text(stringResource(R.string.quantity)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
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
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanUnit = unit.trim()
                            val q = quantity.toDoubleOrNull() ?: 0.0
                            if (cleanName.isNotBlank() && cleanUnit.isNotBlank() && q > 0) {
                                viewModel.addInventoryItemRemote(cleanName, q, cleanUnit)
                                showDialog = false
                            } else {
                                VibrationUtils.vibrate(context)
                                Toast.makeText(context, "لطفاً تمامی فیلدها را پر کنید", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            )
        }

        if (itemToDelete != null) {
            CustomDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("حذف از انبار") },
                text = { Text("آیا از حذف ${itemToDelete?.name} از انبار مطمئن هستید؟") },
                confirmButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            itemToDelete?.let { viewModel.deleteInventoryItemRemote(it.id) }
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            )
        }
    }
}
