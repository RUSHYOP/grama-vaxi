package com.gramavaxi.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Grama-Vaxi"
        val body = inputData.getString(KEY_BODY) ?: ""
        val notifId = inputData.getInt(KEY_NOTIF_ID, 1)
        NotificationHelper.show(applicationContext, notifId, title, body)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_NOTIF_ID = "notif_id"
    }
}
