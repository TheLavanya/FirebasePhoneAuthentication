package com.lav.bluetoothscanning

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class ScanService : Service() {

    val CHANNEL_ID = "Scanning-Channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = getNotification()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotification(): Notification {
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(), 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("BLE scanning")
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentIntent(pendingIntent)
            .build()
        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "APK Download Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setDescription("no sound");
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(false);
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(notificationChannel)
        }
    }
}