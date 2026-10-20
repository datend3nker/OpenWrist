package io.github.datend3nker.openwrist

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.datend3nker.openwrist.presentation.MainActivity
import org.fdroid.database.UpdatableApp

private const val CHANNEL_ID = "updates"
private const val NOTIFICATION_ID = 1

/**
 * Shows how many installed apps have an update, or removes the notification
 * if there are none. Tapping it opens the Updates screen. Does nothing while
 * the user has not allowed notifications.
 */
fun showUpdateNotification(
    context: Context,
    updates: List<UpdatableApp>,
) {
    val manager = NotificationManagerCompat.from(context)
    if (updates.isEmpty()) {
        manager.cancel(NOTIFICATION_ID)
        return
    }
    val allowed =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    if (!allowed) return

    manager.createNotificationChannel(
        NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_updates))
            .build(),
    )
    val openUpdates =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_UPDATES, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    val notification =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                context.resources.getQuantityString(R.plurals.notification_updates, updates.size, updates.size),
            ).setContentText(updates.joinToString { it.name ?: it.packageName })
            .setContentIntent(openUpdates)
            .setAutoCancel(true)
            .build()
    manager.notify(NOTIFICATION_ID, notification)
}
