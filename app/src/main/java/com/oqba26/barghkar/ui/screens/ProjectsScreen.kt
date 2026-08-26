package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel
import com.oqba26.barghkar.ui.viewmodels.CustomerViewModel
import com.oqba26.barghkar.ui.viewmodels.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProject: (Long) -> Unit,
    viewModel: ProjectViewModel = viewModel(),
    customerViewModel: CustomerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(value = false) }
    val projects by viewModel.allProjects.collectAsState()
    val customers by customerViewModel.allCustomers.collectAsState()
    val isMaster = authViewModel.isMaster()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.projects)) })
        },
        floatingActionButton = {
            if (isMaster) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_project))
                }
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
            items(projects) { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProject(project.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = project.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = project.description, style = MaterialTheme.typography.bodySmall)
                            val customer = customers.find { it.id == project.customerId }
                            if (customer != null) {
                                Text(
                                    text = "مشتری: ${customer.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (isMaster) {
                            IconButton(onClick = { viewModel.deleteProject(project) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
            var expanded by remember { mutableStateOf(false) }
            
            var area by remember { mutableStateOf("") }
            var priceFixture by remember { mutableStateOf("") }
            var priceMeter by remember { mutableStateOf("") }
            var p1 by remember { mutableStateOf("") }
            var p2 by remember { mutableStateOf("") }
            var p3 by remember { mutableStateOf("") }

            CustomDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.new_project)) },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        item {
                            TextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.project_name)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    value = customers.find { it.id == selectedCustomerId }?.name ?: "انتخاب مشتری",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.customers)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    customers.forEach { customer ->
                                        DropdownMenuItem(
                                            text = { Text(customer.name) },
                                            onClick = {
                                                selectedCustomerId = customer.id
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "جزئیات فنی و مالی", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextField(value = area, onValueChange = { area = it }, label = { Text(stringResource(R.string.infrastructure_area)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = priceFixture, onValueChange = { priceFixture = it }, label = { Text(stringResource(R.string.price_per_fixture)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = priceMeter, onValueChange = { priceMeter = it }, label = { Text(stringResource(R.string.price_per_meter)) }, modifier = Modifier.fillMaxWidth())
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "مراحل پرداخت", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextField(value = p1, onValueChange = { p1 = it }, label = { Text(stringResource(R.string.first_payment)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = p2, onValueChange = { p2 = it }, label = { Text(stringResource(R.string.second_payment)) }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(value = p3, onValueChange = { p3 = it }, label = { Text(stringResource(R.string.third_payment)) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanDescription = description.trim()
                            val parsedArea = area.toDoubleOrNull() ?: 0.0
                            val parsedFixture = priceFixture.toLongOrNull() ?: 0L
                            val parsedMeter = priceMeter.toLongOrNull() ?: 0L
                            val parsedP1 = p1.toLongOrNull() ?: 0L
                            val parsedP2 = p2.toLongOrNull() ?: 0L
                            val parsedP3 = p3.toLongOrNull() ?: 0L

                            if (cleanName.isNotBlank() && cleanDescription.isNotBlank()) {
                                viewModel.addProject(
                                    name = cleanName,
                                    description = cleanDescription,
                                    customerId = selectedCustomerId,
                                    area = parsedArea,
                                    priceFixture = parsedFixture,
                                    priceMeter = parsedMeter,
                                    p1 = parsedP1,
                                    p2 = parsedP2,
                                    p3 = parsedP3
                                )
                                showDialog = false
                            }
                        },
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}
