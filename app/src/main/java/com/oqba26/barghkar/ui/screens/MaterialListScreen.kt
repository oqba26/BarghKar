package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.CustomerViewModel
import com.oqba26.barghkar.ui.viewmodels.ProjectViewModel
import com.oqba26.barghkar.utils.InvoiceExporter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialListScreen(
    projectId: Long,
    projectViewModel: ProjectViewModel = viewModel(),
    customerViewModel: CustomerViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val project = projectViewModel.allProjects.collectAsState().value.find { it.id == projectId }
    val materials by projectViewModel.getMaterials(projectId).collectAsState()
    val installments by projectViewModel.getInstallments(projectId).collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: stringResource(R.string.project_details)) },
                actions = {
                    IconButton(onClick = {
                        project?.let {
                            val text = InvoiceExporter.generateTextInvoice(it, materials)
                            InvoiceExporter.shareTextInvoice(context, text)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "SMS")
                    }
                    IconButton(onClick = {
                        project?.let {
                            InvoiceExporter.exportPdfInvoice(context, it, materials)
                        }
                    }) {
                        Icon(Icons.Default.Calculate, contentDescription = "PDF")
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
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text(stringResource(R.string.invoice), modifier = Modifier.padding(16.dp))
                }
            }

            when (selectedTab) {
                0 -> MaterialsTab(projectId, projectViewModel, materials)
                1 -> InstallmentsTab(projectId, projectViewModel, installments)
                2 -> InvoiceTab(project, materials, projectViewModel)
            }
        }
    }
}

@Composable
fun MaterialsTab(projectId: Long, viewModel: ProjectViewModel, materials: List<com.oqba26.barghkar.data.local.entity.MaterialEntity>) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materials) { material ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = material.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${material.quantity} ${material.unit} × ${material.pricePerUnit} تومان",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { viewModel.deleteMaterial(material) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("عدد") }
        var price by remember { mutableStateOf("") }

        CustomDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.new_material)) },
            text = {
                Column {
                    TextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.item_name)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = quantity, onValueChange = { quantity = it }, label = { Text(stringResource(R.string.quantity)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = unit, onValueChange = { unit = it }, label = { Text(stringResource(R.string.unit_hint)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = price, onValueChange = { price = it }, label = { Text(stringResource(R.string.price_per_unit)) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val q = quantity.toIntOrNull() ?: 0
                    val p = price.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && q > 0) {
                        viewModel.addMaterial(projectId, name, q, unit, p)
                        showDialog = false
                    }
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun InstallmentsTab(projectId: Long, viewModel: ProjectViewModel, installments: List<com.oqba26.barghkar.data.local.entity.InstallmentEntity>) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installments) { installment ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${installment.amount} تومان", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(installment.dueDate)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Checkbox(checked = installment.isPaid, onCheckedChange = {
                            viewModel.updateInstallment(installment.copy(isPaid = it))
                        })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    if (showDialog) {
        var amount by remember { mutableStateOf("") }
        // Simple date picker placeholder logic
        val dueDate = System.currentTimeMillis() + (86400000L * 7L) // Default 7 days later

        CustomDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add_installment)) },
            text = {
                Column {
                    TextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.amount)) })
                    Text("سررسید: ۷ روز آینده (پیش‌فرض)", modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amount.toLongOrNull() ?: 0L
                    if (a > 0) {
                        viewModel.addInstallment(projectId, a, dueDate)
                        showDialog = false
                    }
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun InvoiceTab(project: com.oqba26.barghkar.data.local.entity.ProjectEntity?, materials: List<com.oqba26.barghkar.data.local.entity.MaterialEntity>, viewModel: ProjectViewModel) {
    if (project == null) return
    var totalWage by remember { mutableStateOf(project.totalWage.toString()) }
    val totalMaterial = materials.sumOf { it.quantity * it.pricePerUnit }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        TextField(
            value = totalWage,
            onValueChange = { 
                totalWage = it
                it.toLongOrNull()?.let { wage ->
                    viewModel.updateProject(project.copy(totalWage = wage))
                }
            },
            label = { Text(stringResource(R.string.total_wage)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "جمع متریال: $totalMaterial تومان", style = MaterialTheme.typography.titleMedium)
        Text(text = "دستمزد: ${project.totalWage} تومان", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "جمع کل: ${totalMaterial + project.totalWage} تومان",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
