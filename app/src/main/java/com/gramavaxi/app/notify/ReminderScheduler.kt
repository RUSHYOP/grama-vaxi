package com.gramavaxi.app.notify

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.data.entity.Vaccination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context,
    private val wm: WorkManager = WorkManager.getInstance(context)
) {

    private val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun scheduleVaccineReminder(v: Vaccination, animalName: String) {
        val now = System.currentTimeMillis()
        // Reminder 3 days before due date
        val threeDaysBefore = v.dueDate - TimeUnit.DAYS.toMillis(3)
        if (threeDaysBefore > now) {
            enqueue(
                tag = "vacc-3d-${v.id}",
                delayMs = threeDaysBefore - now,
                title = "Vaccination in 3 days",
                body = "$animalName is due for ${v.vaccineName} on ${df.format(Date(v.dueDate))}",
                notifId = (v.id * 10 + 1).toInt()
            )
        }
        // Reminder on due date
        if (v.dueDate > now) {
            enqueue(
                tag = "vacc-due-${v.id}",
                delayMs = v.dueDate - now,
                title = "Vaccination due today",
                body = "$animalName needs ${v.vaccineName} today",
                notifId = (v.id * 10 + 2).toInt()
            )
        }
    }

    fun scheduleCampReminder(c: Camp) {
        val now = System.currentTimeMillis()
        val threeDaysBefore = c.date - TimeUnit.DAYS.toMillis(3)
        val oneDayBefore = c.date - TimeUnit.DAYS.toMillis(1)
        if (threeDaysBefore > now) {
            enqueue(
                tag = "camp-3d-${c.id}",
                delayMs = threeDaysBefore - now,
                title = "Vaccination Camp in 3 days",
                body = "${c.title} at ${c.location} on ${df.format(Date(c.date))}",
                notifId = (c.id * 10 + 3).toInt() + 1_000_000
            )
        }
        if (oneDayBefore > now) {
            enqueue(
                tag = "camp-1d-${c.id}",
                delayMs = oneDayBefore - now,
                title = "Doctor arriving tomorrow",
                body = "${c.title} at ${c.location} — be ready!",
                notifId = (c.id * 10 + 4).toInt() + 1_000_000
            )
        }
    }

    fun cancelVaccineReminders(vaccinationId: Long) {
        wm.cancelUniqueWork("vacc-3d-$vaccinationId")
        wm.cancelUniqueWork("vacc-due-$vaccinationId")
    }

    fun cancelCampReminders(campId: Long) {
        wm.cancelUniqueWork("camp-3d-$campId")
        wm.cancelUniqueWork("camp-1d-$campId")
    }

    private fun enqueue(tag: String, delayMs: Long, title: String, body: String, notifId: Int) {
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_BODY, body)
            .putInt(ReminderWorker.KEY_NOTIF_ID, notifId)
            .build()
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()
        wm.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, req)
    }
}
