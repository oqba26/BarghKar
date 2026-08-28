package com.oqba26.barghkar.data.sync

import android.content.Context
import androidx.work.*

object SyncManager {
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SupabaseImmediateSync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
