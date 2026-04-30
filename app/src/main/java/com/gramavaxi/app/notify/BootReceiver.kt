package com.gramavaxi.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gramavaxi.app.repo.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Repository.get(context).rescheduleAllReminders()
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
