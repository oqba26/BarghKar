package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.viewmodels.ProjectViewModel
import com.oqba26.barghkar.ui.viewmodels.UtilityViewModel
import com.oqba26.barghkar.ui.viewmodels.HomeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.utils.NumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProject: (Long) -> Unit,
    utilityViewModel: UtilityViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val projects by projectViewModel.allProjects.collectAsState()
    val income by homeViewModel.monthlyIncome.collectAsState()
    val profit by homeViewModel.monthlyProfit.collectAsState()
    val inventoryCount by homeViewModel.inventoryCount.collectAsState()

    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.app_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                actions = {
                    IconButton(onClick = { utilityViewModel.toggleFlashlight() }) {
                        Icon(
                            imageVector = if (utilityViewModel.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = stringResource(R.string.flashlight),
                            tint = if (utilityViewModel.isFlashlightOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                HomeHeader()
            }

            // Summary Section
            item {
                SummarySection(
                    income = income,
                    profit = profit,
                    inventoryCount = inventoryCount,
                    useEnglishNumbers = useEnglishNumbers
                )
            }
            
            // Recent Projects Section
            if (projects.isNotEmpty()) {
                item {
                    Text(
                        text = "پروژه‌های اخیر",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        projects.take(2).forEach { project ->
                            ElevatedCard(
                                onClick = { onNavigateToProject(project.id) },
                                modifier = Modifier.weight(1f).height(100.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = project.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text(text = project.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // Info Card
            item {
                InfoCard()
            }
        }
    }
}

@Composable
fun SummarySection(income: Long, profit: Long, inventoryCount: Int, useEnglishNumbers: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "درآمد ماه",
                value = "${NumberUtils.formatPrice(income, useEnglishNumbers)} تومان",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "سود ماه",
                value = "${NumberUtils.formatPrice(profit, useEnglishNumbers)} تومان",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        SummaryCard(
            title = "موجودی کالاها",
            value = "${NumberUtils.formatNumber(inventoryCount, useEnglishNumbers)} مورد در انبار",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun HomeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = stringResource(R.string.welcome_back),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.ready_to_work),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp
                )
            )
        }
        Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(80.dp)
                .offset(x = 10.dp, y = 10.dp),
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.safety_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
