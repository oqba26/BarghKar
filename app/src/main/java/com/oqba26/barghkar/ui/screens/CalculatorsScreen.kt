package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.utils.NumberUtils
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.viewmodels.UtilityViewModel
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    onNavigateToOhmLaw: () -> Unit,
    onNavigateToVoltageDrop: () -> Unit,
    utilityViewModel: UtilityViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.technical_calculations)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UnitConverterCard(
                awgValue = utilityViewModel.awgValue,
                mm2Value = utilityViewModel.mm2Value,
                useEnglishNumbers = useEnglishNumbers,
                onAwgChange = { utilityViewModel.onAwgChange(NumberUtils.englishizeDigits(it)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            CalculatorItem(title = stringResource(R.string.ohm_law), onClick = onNavigateToOhmLaw)
            CalculatorItem(title = stringResource(R.string.voltage_drop), onClick = onNavigateToVoltageDrop)
        }
    }
}

@Composable
fun CalculatorItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun UnitConverterCard(
    awgValue: String,
    mm2Value: String,
    useEnglishNumbers: Boolean,
    onAwgChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.wire_unit_converter),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = awgValue,
                    onValueChange = onAwgChange,
                    label = { Text("AWG") },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    singleLine = true,
                    visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                )
                OutlinedTextField(
                    value = mm2Value,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("mm²") },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    visualTransformation = if (useEnglishNumbers) VisualTransformation.None else NumberUtils.getPersianNumberTransformation()
                )
            }
        }
    }
}
