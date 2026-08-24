package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.oqba26.barghkar.data.model.UserRole
import androidx.compose.ui.unit.dp
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.ui.theme.AppFont
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as BarghKarApp).settingsManager
    val selectedFont by settingsManager.selectedFont.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    var expanded by remember { mutableStateOf(value = false) }
    
    var masterEmail by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.appearance_settings),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // بخش مدیریت تیم (اتصال به اوستا)
            if (userProfile?.role == UserRole.MASTER && userProfile?.masterId == null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "مدیریت تیم (اتصال به اوستا)", style = MaterialTheme.typography.titleMedium)
                        Text(text = "اگر شما شاگرد هستید، ایمیل اوستای خود را وارد کنید:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = masterEmail,
                            onValueChange = { masterEmail = it },
                            label = { Text("ایمیل اوستا") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { authViewModel.linkMaster(masterEmail) },
                            enabled = !isLoading && masterEmail.isNotBlank(),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("اتصال به اوستا")
                        }
                        if (error != null) {
                            Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else if (userProfile?.role == UserRole.APPRENTICE) {
                Text(
                    text = "شما به عنوان شاگرد متصل هستید",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Text(
                text = stringResource(R.string.select_font),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFont.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontFamily = selectedFont.fontFamily),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AppFont.entries.forEach { font ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = font.displayName,
                                    fontFamily = font.fontFamily
                                )
                            },
                            onClick = {
                                settingsManager.setSelectedFont(font)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "خروج از حساب کاربری")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
