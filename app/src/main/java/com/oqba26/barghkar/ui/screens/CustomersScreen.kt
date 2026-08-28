package com.oqba26.barghkar.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.utils.NumberUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.CustomerViewModel
import com.oqba26.barghkar.utils.VibrationUtils
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<com.oqba26.barghkar.data.local.entity.CustomerEntity?>(null) }
    val customers by viewModel.allCustomers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.customers)) },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_customer))
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
                                Text(text = NumberUtils.formatNumber(customer.phoneNumber, useEnglishNumbers), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = customer.address, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { customerToDelete = customer }) {
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
                        TextField(
                            value = phone,
                            onValueChange = { 
                                val englishDigits = NumberUtils.englishizeDigits(it)
                                if (englishDigits.all { char -> char.isDigit() }) {
                                    phone = englishDigits
                                } else {
                                    android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text(stringResource(R.string.phone_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(value = address, onValueChange = { address = it }, label = { Text(stringResource(R.string.address)) })
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        val cleanName = name.trim()
                        val cleanPhone = phone.trim()
                        val cleanAddress = address.trim()
                        if (cleanName.isNotBlank() && cleanPhone.length >= 8 && cleanAddress.isNotBlank()) {
                            viewModel.addCustomerRemote(cleanName, cleanPhone, cleanAddress)
                            showDialog = false
                        } else {
                            VibrationUtils.vibrate(context)
                            Toast.makeText(context, "لطفاً تمامی فیلدها را به درستی پر کنید", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            )
        }

        if (customerToDelete != null) {
            CustomDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text("حذف مشتری") },
                text = { Text("آیا از حذف مشتری ${customerToDelete?.name} مطمئن هستید؟") },
                confirmButton = {
                    TextButton(onClick = { customerToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            customerToDelete?.let { viewModel.deleteCustomerRemote(it.id) }
                            customerToDelete = null
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
