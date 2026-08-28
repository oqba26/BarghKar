package com.oqba26.barghkar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.barghkar.utils.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    isDownloading: Boolean,
    progress: Float,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    CustomDialog(
        onDismissRequest = { if (!updateInfo.isForceUpdate && !isDownloading) onDismiss() },
        title = { Text("به‌روزرسانی جدید موجود است") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "نسخه ${updateInfo.versionName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isDownloading) {
                    Text(
                        text = "در حال دریافت به‌روزرسانی...",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    Text(
                        text = "تغییرات این نسخه:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = updateInfo.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    if (updateInfo.isForceUpdate) {
                        Text(
                            text = "نصب این به‌روزرسانی برای ادامه استفاده از برنامه الزامی است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!updateInfo.isForceUpdate && !isDownloading) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(text = "بعداً")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDownloading
            ) {
                Text(text = if (isDownloading) "در حال دانلود..." else "تایید و بروزرسانی")
            }
        },
    )
}
