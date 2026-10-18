package com.example.smsforwarder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/** Hands non-SMS forwarding to third-party apps after an explicit user action. */
object ForwardingChannelNotifier {
    private const val TAG = "ChannelNotifier"
    private const val CHANNEL_ID = "forwarding_handoffs"

    fun post(context: Context, source: String, channel: String, target: String, body: String) {
        val recordId = SmsHistoryStore.add(context, source, target, body, "handoff_$channel", 1)
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            SmsHistoryStore.fail(context, recordId, "Notification permission is not granted; no handoff notification was shown.")
            Log.w(TAG, "Notification permission is not granted; cannot hand off $channel delivery.")
            return
        }
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Forwarding actions",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Tap to prepare non-SMS message forwarding in the selected app."
                    }
                )
            }
            val intent = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_OPEN_CHANNEL
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_DELIVERY_CHANNEL, channel)
                putExtra(MainActivity.EXTRA_DELIVERY_SOURCE, source)
                putExtra(MainActivity.EXTRA_DELIVERY_TARGET, target)
                putExtra(MainActivity.EXTRA_DELIVERY_BODY, body)
            }
            val requestCode = ("$channel|$source|$target|$body").hashCode() and 0x7fffffff
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flags)
            val channelName = when (channel) {
                ForwardingRuleStore.EMAIL -> "Email"
                ForwardingRuleStore.TELEGRAM -> "Telegram"
                ForwardingRuleStore.WHATSAPP -> "WhatsApp"
                ForwardingRuleStore.BALE -> "Bale"
                ForwardingRuleStore.EITAA -> "Eitaa"
                else -> channel
            }
            val summary = if (target.isBlank()) "Open to choose a recipient" else "Recipient: $target"
            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }
            val notification = builder
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("SMS Forwarder · $channelName")
                .setContentText("$summary — tap to prepare the message")
                .setStyle(Notification.BigTextStyle().bigText("$summary\n${body.take(280)}"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            manager.notify(requestCode, notification)
            SmsHistoryStore.queued(
                context,
                recordId,
                "Notification posted for $channel. Final sending requires user confirmation inside the target app."
            )
        } catch (e: SecurityException) {
            SmsHistoryStore.fail(context, recordId, "Unable to post forwarding notification.")
            Log.w(TAG, "Unable to post forwarding notification", e)
        } catch (e: Exception) {
            SmsHistoryStore.fail(context, recordId, e.localizedMessage ?: "Unable to prepare $channel handoff.")
            Log.e(TAG, "Unable to prepare $channel handoff", e)
        }
    }
}
