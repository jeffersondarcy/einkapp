package io.fitdev.streamink

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class MediaProjectionService : Service() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(this, "messages")
                    .setContentText("streaming enabled")
                    .setContentTitle("Stream to e-ink")
            startForeground(101, builder.build())
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}