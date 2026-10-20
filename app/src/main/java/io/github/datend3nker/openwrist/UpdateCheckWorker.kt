package io.github.datend3nker.openwrist

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Checks once a day whether installed apps have a newer watch build and
 * notifies the user. It never installs anything on its own: updates are
 * started by the user, who also confirms each one in Android's dialog.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as OpenWristApplication).container
        // A failed refresh is not retried here: the old index still answers
        // which updates were known, and the next run tries again.
        if (container.repoRefresher.isRefreshDue()) container.repoRefresher.refresh()
        showUpdateNotification(applicationContext, container.watchApps.updates())
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "update-check"

        /** Schedules the daily check. Keeps an already scheduled one, so calling it on every start is fine. */
        fun schedule(context: Context) {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            val request =
                PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
