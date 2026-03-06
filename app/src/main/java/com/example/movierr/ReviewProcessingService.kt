package com.example.movierr

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class ReviewProcessingService : Service() {

    private val CHANNEL_ID = "ReviewProcessingChannel"
    private val NOTIFICATION_ID = 1001
    private var handler = Handler(Looper.getMainLooper())
    private var startTime = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTime = System.currentTimeMillis()
        
        // Intent to open DiaryActivity
        val diaryIntent = Intent(this, DiaryActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, diaryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Review Processing")
            .setContentText("Your movie review is being saved to the diary")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setUsesChronometer(true)
            .addAction(android.R.drawable.ic_menu_view, "View Diary", pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Simulate processing for 5 seconds
        handler.postDelayed({
            stopServiceAndNotifyComplete()
        }, 5000)

        return START_NOT_STICKY
    }

    private fun stopServiceAndNotifyComplete() {
        val completeNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Process Complete")
            .setContentText("Your movie review has been successfully saved")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1002, completeNotification)

        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Review Processing Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
