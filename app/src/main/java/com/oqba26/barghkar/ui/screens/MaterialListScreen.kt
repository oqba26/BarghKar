package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oqba26.barghkar.data.model.RecordStatus
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
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel
import com.oqba26.barghkar.ui.viewmodels.ProjectViewModel
import com.oqba26.barghkar.utils.InvoiceExporter
import com.oqba26.barghkar.utils.VibrationUtils
import android.widget.Toast
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialListScreen(
    projectId: Long,
    projectViewModel: ProjectViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val project = projectViewModel.allProjects.collectAsState().value.find { it.id == projectId }
    val materials by projectViewModel.getMaterialsRemote(projectId).collectAsState()
    val installments by projectViewModel.getInstallmentsRemote(projectId).collectAsState()
    val isMaster = authViewModel.isMaster()

    var materialToDelete by remember { mutableStateOf<com.oqba26.barghkar.data.local.entity.MaterialEntity?>(null) }
    var installmentToDelete by remember { mutableStateOf<com.oqba26.barghkar.data.local.entity.InstallmentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: stringResource(R.string.project_details)) },
                actions = {
                    if (isMaster) {
                        IconButton(
                            onClick = {
                                project?.let {
                                    val text = InvoiceExporter.generateTextInvoice(it, materials, useEnglishNumbers)
                                    InvoiceExporter.shareTextInvoice(context, text)
                                }
                            },
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "SMS")
                        }
                        IconButton(onClick = {
                            project?.let {
                                InvoiceExporter.exportPdfInvoice(context, it, materials, useEnglishNumbers)
                            }
                        }) {
                            Icon(Icons.Default.Calculate, contentDescription = "PDF")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(stringResource(R.string.material_list), modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(stringResource(R.string.installments), modifier = Modifier.padding(16.dp))
                }
                if (isMaster) {
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text(stringResource(R.string.invoice), modifier = Modifier.padding(16.dp))
                    }
                }
            }

            when (selectedTab) {
                0 -> MaterialsTab(projectId, projectViewModel, materials, isMaster, useEnglishNumbers) { materialToDelete = it }
                1 -> InstallmentsTab(projectId, projectViewModel, installments, isMaster, useEnglishNumbers) { installmentToDelete = it }
                2 -> if (isMaster) InvoiceTab(project, materials, projectViewModel, useEnglishNumbers)
            }
        }

        if (materialToDelete != null) {
            CustomDialog(
                onDismissRequest = { materialToDelete = null },
                title = { Text("حذف متریال") },
                text = { Text("آیا از حذف ${materialToDelete?.name} مطمئن هستید؟") },
                confirmButton = {
                    TextButton(onClick = { materialToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            materialToDelete?.let { projectViewModel.deleteMaterialRemote(it) }
                            materialToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            )
        }

        if (installmentToDelete != null) {
            CustomDialog(
                onDismissRequest = { installmentToDelete = null },
                title = { Text("حذف قسط") },
                text = { Text("آیا از حذف این قسط مطمئن هستید؟") },
                confirmButton = {
                    TextButton(onClick = { installmentToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            installmentToDelete?.let { projectViewModel.deleteInstallmentRemote(it) }
                            installmentToDelete = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsTab(
    projectId: Long,
    viewModel: ProjectViewModel,
    materials: List<com.oqba26.barghkar.data.local.entity.MaterialEntity>,
    isMaster: Boolean,
    useEnglishNumbers: Boolean,
    onDelete: (com.oqba26.barghkar.data.local.entity.MaterialEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(value = false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materials) { material ->
                val backgroundColor = when (material.status) {
                    RecordStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    RecordStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = material.name, style = MaterialTheme.typography.titleMedium)
                                val priceText = if (isMaster) {
                                    "${NumberUtils.formatNumber(material.quantity, useEnglishNumbers)} ${material.unit} × ${NumberUtils.formatPrice(material.pricePerUnit, useEnglishNumbers)} تومان"
                                } else {
                                    "${NumberUtils.formatNumber(material.quantity, useEnglishNumbers)} ${material.unit}"
                                }
                                Text(text = priceText, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            if (isMaster) {
                                Row {
                                    if (material.status == RecordStatus.PENDING) {
                                        IconButton(onClick = { viewModel.updateMaterialRemote(material.copy(status = RecordStatus.APPROVED)) }) {
                                            Icon(Icons.Default.Check, contentDescription = "تایید", tint = Color.Green)
                                        }
                                        IconButton(onClick = { viewModel.updateMaterialRemote(material.copy(status = RecordStatus.REJECTED)) }) {
                                            Icon(Icons.Default.Close, contentDescription = "رد", tint = Color.Red)
                                        }
                                    }
                                    IconButton(onClick = { onDelete(material) }) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                                    }
                                }
                            }
                        }
                        
                        if (material.status != RecordStatus.APPROVED) {
                            Text(
                                text = if (material.status == RecordStatus.PENDING) "در انتظار تایید اوستا" else "توسط اوستا رد شد",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (material.status == RecordStatus.PENDING) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isMaster) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("عدد") }
        var price by remember { mutableStateOf("") }
        
        var nameExpanded by remember { mutableStateOf(value = false) }
        var unitExpanded by remember { mutableStateOf(false) }

        val filteredItems = remember(name) {
            ElectricalSupplies.commonItems.filter { it.name.contains(name, ignoreCase = true) }
        }

        CustomDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.new_material)) },
            text = {
                Column {
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
                                Toast.makeText(context, "لطفاً فقط عدد وارد کنید", Toast.LENGTH_SHORT).show()
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

                    if (isMaster) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = price, 
                            onValueChange = { 
                                val englishDigits = NumberUtils.englishizeDigits(it)
                                if (englishDigits.all { char -> char.isDigit() }) {
                                    price = englishDigits
                                } else {
                                    Toast.makeText(context, "لطفاً فقط عدد وارد کنید", Toast.LENGTH_SHORT).show()
                                }
                            }, 
                            label = { Text(stringResource(R.string.price_per_unit)) },
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
                Button(onClick = {
                    val cleanName = name.trim()
                    val cleanUnit = unit.trim()
                    val q = quantity.toDoubleOrNull() ?: 0.0
                    val p = price.toLongOrNull() ?: 0L
                    if (cleanName.isNotBlank() && cleanUnit.isNotBlank() && q > 0 && p >= 0L) {
                        val status = if (isMaster) RecordStatus.APPROVED else RecordStatus.PENDING
                        viewModel.addMaterialRemote(projectId, cleanName, q.toInt(), cleanUnit, p, status)
                        showDialog = false
                    } else {
                        VibrationUtils.vibrate(context)
                        Toast.makeText(context, "لطفاً تمامی فیلدها را به درستی پر کنید", Toast.LENGTH_SHORT).show()
                    }
                }) { Text(stringResource(R.string.confirm)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentsTab(
    projectId: Long,
    viewModel: ProjectViewModel,
    installments: List<com.oqba26.barghkar.data.local.entity.InstallmentEntity>,
    isMaster: Boolean,
    useEnglishNumbers: Boolean,
    onDelete: (com.oqba26.barghkar.data.local.entity.InstallmentEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(value = false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installments) { installment ->
                val backgroundColor = when (installment.status) {
                    RecordStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    RecordStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            if (isMaster) {
                                Text(text = "${NumberUtils.formatPrice(installment.amount, useEnglishNumbers)} تومان", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = PersianDateFormat("yyyy/MM/dd").format(PersianDate(installment.dueDate)),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (installment.status != RecordStatus.APPROVED) {
                                Text(
                                    text = if (installment.status == RecordStatus.PENDING) "در انتظار تایید" else "رد شد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (installment.status == RecordStatus.PENDING) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        if (isMaster) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (installment.status == RecordStatus.PENDING) {
                                    IconButton(onClick = { viewModel.updateInstallmentRemote(installment.copy(status = RecordStatus.APPROVED)) }) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green)
                                    }
                                }
                                Checkbox(checked = installment.isPaid, onCheckedChange = {
                                    viewModel.updateInstallmentRemote(installment.copy(isPaid = it))
                                })
                                IconButton(onClick = { onDelete(installment) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isMaster) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }

    if (showDialog) {
        var amount by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

        CustomDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add_installment)) },
            text = {
                Column {
                    TextField(
                        value = amount,
                        onValueChange = { 
                            val englishDigits = NumberUtils.englishizeDigits(it)
                            if (englishDigits.all { char -> char.isDigit() }) {
                                amount = englishDigits
                            } else {
                                Toast.makeText(context, "لطفاً فقط عدد وارد کنید", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text(stringResource(R.string.amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = PersianDateFormat("yyyy/MM/dd").format(PersianDate(selectedDate)))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            dismissButton = {
                Button(onClick = {
                    val a = amount.trim().toLongOrNull() ?: 0L
                    if (a > 0L) {
                        val status = if (isMaster) RecordStatus.APPROVED else RecordStatus.PENDING
                        viewModel.addInstallmentRemote(projectId, a, selectedDate, status)
                        showDialog = false
                    } else {
                        VibrationUtils.vibrate(context)
                        Toast.makeText(context, "لطفاً مبلغ قسط را وارد کنید", Toast.LENGTH_SHORT).show()
                    }
                }) { Text(stringResource(R.string.confirm)) }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun InvoiceTab(project: com.oqba26.barghkar.data.local.entity.ProjectEntity?, materials: List<com.oqba26.barghkar.data.local.entity.MaterialEntity>, viewModel: ProjectViewModel, useEnglishNumbers: Boolean) {
    if (project == null) return
    var totalWage by remember { mutableStateOf(project.totalWage.toString()) }
    val totalMaterial = materials.sumOf { it.quantity.toLong() * it.pricePerUnit }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        TextField(
            value = totalWage,
            onValueChange = { 
                val englishDigits = NumberUtils.englishizeDigits(it)
                if (englishDigits.all { char -> char.isDigit() }) {
                    totalWage = englishDigits
                    englishDigits.toLongOrNull()?.let { wage ->
                        viewModel.updateProjectRemote(project.copy(totalWage = wage))
                    }
                }
            },
            label = { Text(stringResource(R.string.total_wage)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "جمع متریال: ${NumberUtils.formatPrice(totalMaterial, useEnglishNumbers)} تومان", style = MaterialTheme.typography.titleMedium)
        Text(text = "دستمزد: ${NumberUtils.formatPrice(project.totalWage, useEnglishNumbers)} تومان", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "جمع کل: ${NumberUtils.formatPrice(totalMaterial + project.totalWage, useEnglishNumbers)} تومان",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
