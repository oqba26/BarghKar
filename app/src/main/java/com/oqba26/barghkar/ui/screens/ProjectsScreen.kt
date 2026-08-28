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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.utils.NumberUtils
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel
import com.oqba26.barghkar.ui.viewmodels.CustomerViewModel
import com.oqba26.barghkar.ui.viewmodels.ProjectViewModel
import com.oqba26.barghkar.utils.VibrationUtils
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProject: (Long) -> Unit,
    viewModel: ProjectViewModel = viewModel(),
    customerViewModel: CustomerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(value = false) }
    var projectToDelete by remember { mutableStateOf<com.oqba26.barghkar.data.local.entity.ProjectEntity?>(null) }
    val projects by viewModel.allProjects.collectAsState()
    val customers by customerViewModel.allCustomers.collectAsState()
    val isMaster = authViewModel.isMaster()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.projects)) },
                actions = {
                    if (isMaster) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_project))
                        }
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
                            // نمایش متراژ و قیمت در لیست پروژه
                            if (project.infrastructureArea > 0) {
                                Text(
                                    text = "متراژ: ${NumberUtils.formatNumber(project.infrastructureArea, useEnglishNumbers)} متر",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        if (isMaster) {
                            IconButton(onClick = { projectToDelete = project }) {
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
                            
                            TextField(
                                value = area,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() || char == '.' }) {
                                        area = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.infrastructure_area)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = priceFixture,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() }) {
                                        priceFixture = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.price_per_fixture)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = priceMeter,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() }) {
                                        priceMeter = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.price_per_meter)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "مراحل پرداخت", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextField(
                                value = p1,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() }) {
                                        p1 = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.first_payment)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = p2,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() }) {
                                        p2 = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.second_payment)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = p3,
                                onValueChange = { 
                                    val englishDigits = NumberUtils.englishizeDigits(it)
                                    if (englishDigits.all { char -> char.isDigit() }) {
                                        p3 = englishDigits
                                    } else {
                                        android.widget.Toast.makeText(context, "لطفاً فقط عدد وارد کنید", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(stringResource(R.string.third_payment)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                            )
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
                            val cleanDescription = description.trim()
                            val parsedArea = area.toDoubleOrNull() ?: 0.0
                            val parsedFixture = priceFixture.toLongOrNull() ?: 0L
                            val parsedMeter = priceMeter.toLongOrNull() ?: 0L
                            val parsedP1 = p1.toLongOrNull() ?: 0L
                            val parsedP2 = p2.toLongOrNull() ?: 0L
                            val parsedP3 = p3.toLongOrNull() ?: 0L

                            if (cleanName.isNotBlank() && cleanDescription.isNotBlank() && selectedCustomerId != null) {
                                viewModel.addProjectRemote(
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
                            } else {
                                VibrationUtils.vibrate(context)
                                Toast.makeText(context, "لطفاً نام پروژه، توضیحات و مشتری را انتخاب کنید", Toast.LENGTH_SHORT).show()
                            }
                        },
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
            )
        }

        if (projectToDelete != null) {
            CustomDialog(
                onDismissRequest = { projectToDelete = null },
                title = { Text("حذف پروژه") },
                text = { Text("آیا از حذف پروژه ${projectToDelete?.name} مطمئن هستید؟") },
                confirmButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            projectToDelete?.let { viewModel.deleteProjectRemote(it) }
                            projectToDelete = null
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
