package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.oqba26.barghkar.data.model.ApprenticePermission
import com.oqba26.barghkar.data.model.UserRole
import androidx.compose.ui.unit.dp
import com.oqba26.barghkar.BarghKarApp
import com.oqba26.barghkar.ui.theme.AppFont
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.barghkar.R
import com.oqba26.barghkar.ui.components.CustomDialog
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? BarghKarApp)?.settingsManager
    val selectedFont by settingsManager?.selectedFont?.collectAsState() ?: remember { mutableStateOf(AppFont.Estedad) }
    val useEnglishNumbers by settingsManager?.useEnglishNumbers?.collectAsState() ?: remember { mutableStateOf(false) }
    val userProfile by authViewModel.userProfile.collectAsState()
    val apprentices by authViewModel.apprentices.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf(value = false) }

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            // ... بقیه موارد همان قبلی
            item {
                Text(
                    text = stringResource(R.string.appearance_settings),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // بخش مدیریت تیم (برای اوستا)
            when (userProfile?.role) {
                UserRole.MASTER -> {
                    item {
                        TeamManagementSection(
                            apprentices = apprentices,
                            isLoading = isLoading,
                            onAddApprentice = { authViewModel.addApprentice(it) },
                            onUpdatePermissions = { id, perms ->
                                authViewModel.updateApprenticePermissions(
                                    id,
                                    perms
                                )
                            }
                        ) { authViewModel.removeApprentice(it) }
                    }
                }
                UserRole.APPRENTICE -> {
                    item(key = userProfile?.permissions) {
                        val profile = userProfile
                        if (profile != null) {
                            ApprenticeStatusSection(userProfile = profile)
                        }
                    }
                }
                else -> {
                    // نمایش نقش فعلی در صورت عدم نمایش بخش مدیریت (برای عیب‌یابی)
                    item {
                        Text(
                            text = "نقش کاربری فعلی: ${userProfile?.role ?: "در حال بارگذاری..."}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
                                    settingsManager?.setSelectedFont(font)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نمایش اعداد به انگلیسی",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Switch(
                        checked = useEnglishNumbers,
                        onCheckedChange = { settingsManager?.setUseEnglishNumbers(it) }
                    )
                }
                Text(
                    text = "در صورت غیرفعال بودن، تمام اعداد به صورت فارسی نمایش داده می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
            }
        }
    }
}

@Composable
fun TeamManagementSection(
    apprentices: List<com.oqba26.barghkar.data.model.UserProfile>,
    isLoading: Boolean,
    onAddApprentice: (String) -> Unit,
    onUpdatePermissions: (String, List<ApprenticePermission>) -> Unit,
    onRemoveApprentice: (String) -> Unit
) {
    var apprenticeEmail by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "مدیریت شاگردان", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = apprenticeEmail,
                onValueChange = { apprenticeEmail = it },
                label = { Text("ایمیل شاگرد جدید") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        IconButton(
                            enabled = apprenticeEmail.trim().isNotBlank(),
                            onClick = {
                                val normalizedEmail = apprenticeEmail.trim()
                                if (normalizedEmail.isNotBlank()) {
                                    onAddApprentice(normalizedEmail)
                                    apprenticeEmail = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "افزودن")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            if (apprentices.isEmpty()) {
                Text(
                    text = "هنوز شاگردی به شما متصل نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                apprentices.forEach { apprentice ->
                    ApprenticeItem(
                        apprentice = apprentice,
                        onUpdatePermissions = { onUpdatePermissions(apprentice.id, it) },
                        onRemove = { onRemoveApprentice(apprentice.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprenticeItem(
    apprentice: com.oqba26.barghkar.data.model.UserProfile,
    onUpdatePermissions: (List<ApprenticePermission>) -> Unit,
    onRemove: () -> Unit
) {
    var showPermissionDialog by remember { mutableStateOf(value = false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (!apprentice.fullName.isNullOrBlank()) apprentice.fullName else apprentice.email?.substringBefore("@") ?: "بدون نام"
                Text(text = displayName, style = MaterialTheme.typography.bodyLarge)
                if (!apprentice.fullName.isNullOrBlank()) {
                    Text(text = apprentice.email ?: "", style = MaterialTheme.typography.labelSmall)
                }
            }
            Row {
                IconButton(onClick = { showPermissionDialog = true }) {
                    Icon(Icons.Default.Security, contentDescription = "دسترسی‌ها", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف شاگرد", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        if (showPermissionDialog) {
            CustomDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("تنظیم دسترسی‌ها") },
                text = {
                    Column {
                        ApprenticePermission.entries.forEach { permission ->
                            val isChecked = apprentice.permissions.contains(permission)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newPerms = if (isChecked) apprentice.permissions - permission else apprentice.permissions + permission
                                        onUpdatePermissions(newPerms)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isChecked, onCheckedChange = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (permission) {
                                        ApprenticePermission.MANAGE_INVENTORY -> "مدیریت انبار"
                                        ApprenticePermission.EDIT_PROJECTS -> "ویرایش پروژه‌ها"
                                        ApprenticePermission.VIEW_FINANCE -> "مشاهده گزارش مالی"
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showPermissionDialog = false }) {
                        Text("بستن")
                    }
                },
                dismissButton = {} // در این دیالوگ نیازی به دکمه انصراف نیست
            )
        }
    }
}

@Composable
fun ApprenticeStatusSection(userProfile: com.oqba26.barghkar.data.model.UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "وضعیت حساب: شاگرد", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "شما به یک اوستا متصل هستید و بر اساس دسترسی‌های تعیین شده فعالیت می‌کنید.", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "دسترسی‌های فعال شما:", style = MaterialTheme.typography.labelLarge)
            userProfile.permissions.forEach { permission ->
                Text(
                    text = "• " + when(permission) {
                        ApprenticePermission.MANAGE_INVENTORY -> "مدیریت انبار"
                        ApprenticePermission.EDIT_PROJECTS -> "ویرایش پروژه‌ها"
                        ApprenticePermission.VIEW_FINANCE -> "مشاهده گزارش مالی"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            if (userProfile.permissions.isEmpty()) {
                Text(text = "هنوز دسترسی خاصی برای شما ثبت نشده است.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
