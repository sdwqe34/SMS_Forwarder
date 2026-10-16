package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", true)) return
        val source = prefs.getString("source", "").orEmpty().trim()
        val target = prefs.getString("target", "").orEmpty().trim()
        if (source.isEmpty() || target.isEmpty()) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return
        val sender = messages[0].originatingAddress ?: return

        if (!matches(sender, source)) return

        val body = messages.joinToString("") { it.messageBody ?: "" }
        if (body.isEmpty()) return

        try {
            val sms: SmsManager = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = sms.divideMessage(body)
            sms.sendMultipartTextMessage(target, null, parts, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // مقایسه با ۱۰ رقم آخر تا تفاوت +98 / 0 مشکل ایجاد نکند
    private fun matches(sender: String, source: String): Boolean {
        val a = sender.filter { it.isDigit() }
        val b = source.filter { it.isDigit() }
        if (a.isNotEmpty() && b.isNotEmpty()) {
            return a.takeLast(10) == b.takeLast(10)
        }
        // فرستنده‌های متنی مثل نام سرویس
        return sender.equals(source, ignoreCase = true)
    }
}
