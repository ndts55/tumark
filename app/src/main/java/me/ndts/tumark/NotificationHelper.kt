package me.ndts.tumark

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


object NotificationHelper {
    const val ChannelId = "Tumark Notification Channel"
    const val NotificationId = 348754

    fun createDefaultChannel(context: Context) {
        val channel = NotificationChannel(
            ChannelId,
            context.getString(R.string.grade_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { enableVibration(true) }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(
        context: Context,
        title: String,
        content: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(androidx.appcompat.R.drawable.abc_btn_check_material)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(NotificationId, builder.build())
        }
    }
}