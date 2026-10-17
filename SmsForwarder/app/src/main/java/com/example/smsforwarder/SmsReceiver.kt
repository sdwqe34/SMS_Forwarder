package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return

        val source = prefs.getString("source", "").orEmpty().trim()
        val target = prefs.getString("target", "").orEmpty().trim()
        if (source.isEmpty() || target.isEmpty()) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.firstOrNull()?.originatingAddress?.trim().orEmpty()
        if (sender.isEmpty() || !matches(sender, source)) return

        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        if (context.checkSelfPermission(android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val id = SmsHistoryStore.add(context, sender, target, body, "automatic", 1)
            SmsHistoryStore.fail(context, id, "SEND_SMS permission is not granted.")
            return
        }

        val result = SmsSender.send(context, sender, target, body, "automatic")
        if (!result.accepted) Log.e(TAG, "Automatic SMS send failed: " + result.detail)
    }

    private fun matches(sender: String, source: String): Boolean {
        if (sender.equals(source, ignoreCase = true)) return true
        val senderDigits = sender.filter { it.isDigit() }
        val sourceDigits = source.filter { it.isDigit() }
        if (senderDigits.isEmpty() || sourceDigits.isEmpty()) return false
        if (PhoneNumberUtils.compare(sender, source)) return true
        if (senderDigits.length < 8 || sourceDigits.length < 8) return senderDigits == sourceDigits
        val suffixLength = minOf(10, senderDigits.length, sourceDigits.length)
        return senderDigits.takeLast(suffixLength) == sourceDigits.takeLast(suffixLength)
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
