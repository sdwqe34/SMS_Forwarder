package com.example.smsforwarder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return
        val sender = messages.firstOrNull()?.originatingAddress?.trim().orEmpty()
        if (sender.isEmpty()) return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        val rules = ForwardingRuleStore.all(context).ifEmpty {
            // Migrate the original single sender/recipient settings for existing users.
            val prefs = context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
            val source = prefs.getString("source", "").orEmpty().trim()
            val target = prefs.getString("target", "").orEmpty().trim()
            if (prefs.getBoolean("enabled", false) && source.isNotBlank() && target.isNotBlank()) {
                listOf(
                    ForwardingRule(
                        id = "legacy-single-rule",
                        source = source,
                        channels = setOf(ForwardingRuleStore.SMS),
                        destinations = mapOf(ForwardingRuleStore.SMS to target),
                        enabled = true
                    )
                )
            } else emptyList()
        }
        if (rules.isEmpty()) return

        val matchingRules = rules.filter { rule ->
            rule.enabled &&
                matches(sender, rule.source) &&
                (rule.containsText.isBlank() || body.contains(rule.containsText, ignoreCase = true)) &&
                (rule.excludesText.isBlank() || !body.contains(rule.excludesText, ignoreCase = true))
        }
        if (matchingRules.isEmpty()) return

        for (rule in matchingRules) {
            for (channel in rule.channels) {
                val target = rule.destinations[channel].orEmpty().trim()
                if (target.isBlank()) continue
                when (channel) {
                    ForwardingRuleStore.SMS -> {
                        if (context.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            val id = SmsHistoryStore.add(context, sender, target, body, "automatic", 1)
                            SmsHistoryStore.fail(context, id, "SEND_SMS permission is not granted.")
                            continue
                        }
                        val result = SmsSender.send(context, sender, target, body, "automatic")
                        if (!result.accepted) Log.e(TAG, "Automatic SMS send failed: " + result.detail)
                    }
                    ForwardingRuleStore.EMAIL,
                    ForwardingRuleStore.TELEGRAM,
                    ForwardingRuleStore.WHATSAPP,
                    ForwardingRuleStore.BALE,
                    ForwardingRuleStore.EITAA -> {
                        ForwardingChannelNotifier.post(context, sender, channel, target, body)
                    }
                }
            }
        }
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
