package com.regisk.legacy.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.*
import com.regisk.legacy.view.Notifications
import java.util.concurrent.TimeUnit

class UpdateCheckService(context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return Result.success()
    }

    companion object {
        @SuppressLint("NewApi")
        fun schedule(context: Context) {
            if (Config.checkUpdate) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresDeviceIdle(true)
                    .build()
                val request = PeriodicWorkRequestBuilder<UpdateCheckService>(12, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    Const.ID.CHECK_REGISK_UPDATE_WORKER_ID,
                    ExistingPeriodicWorkPolicy.REPLACE, request)
            } else {
                WorkManager.getInstance(context)
                    .cancelUniqueWork(Const.ID.CHECK_REGISK_UPDATE_WORKER_ID)
            }
        }
    }
}
