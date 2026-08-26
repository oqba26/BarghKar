package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val customers by viewModel.allCustomers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.customers)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_customer))
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
            items(customers) { customer ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = customer.phoneNumber, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = customer.address, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }

            CustomDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.new_customer)) },
                text = {
                    Column {
                        TextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.customer_name)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone_number)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = address, onValueChange = { address = it }, label = { Text(stringResource(R.string.address)) })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val cleanName = name.trim()
                        val cleanPhone = phone.trim()
                        val cleanAddress = address.trim()
                        if (cleanName.isNotBlank() && cleanPhone.length >= 8 && cleanAddress.isNotBlank()) {
                            viewModel.addCustomer(cleanName, cleanPhone, cleanAddress)
                            showDialog = false
                        }
                    }) {
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
