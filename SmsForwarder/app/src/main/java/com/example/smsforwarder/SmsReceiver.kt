package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
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

        // SMS parts belonging to one message are joined without inserting extra characters.
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        try {
            val manager = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = manager.divideMessage(body)
            if (parts.size > 1) {
                manager.sendMultipartTextMessage(target, null, parts, null, null)
            } else {
                manager.sendTextMessage(target, null, body, null, null)
            }
            Log.i(TAG, "Forward request queued for configured sender")
        } catch (e: Exception) {
            Log.e(TAG, "Could not forward incoming SMS", e)
        }
    }

    /**
     * Prefer Android's phone-number comparison. For country-code formatting differences,
     * fall back to a long suffix only for full-length phone numbers. Short codes must match
     * exactly, and alphanumeric sender IDs such as MALIAT are compared case-insensitively.
     */
    private fun matches(sender: String, source: String): Boolean {
        if (sender.equals(source, ignoreCase = true)) return true

        val senderDigits = sender.filter { it.isDigit() }
        val sourceDigits = source.filter { it.isDigit() }
        if (senderDigits.isEmpty() || sourceDigits.isEmpty()) return false

        if (PhoneNumberUtils.compare(sender, source)) return true

        if (senderDigits.length < 8 || sourceDigits.length < 8) {
            return senderDigits == sourceDigits
        }
        val suffixLength = minOf(10, senderDigits.length, sourceDigits.length)
        return senderDigits.takeLast(suffixLength) == sourceDigits.takeLast(suffixLength)
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
