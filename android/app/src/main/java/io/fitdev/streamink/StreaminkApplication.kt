package io.fitdev.streamink

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import io.flutter.app.FlutterApplication


class StreaminkApplication : FlutterApplication() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}