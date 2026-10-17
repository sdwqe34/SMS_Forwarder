package com.example.smsforwarder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log

object SmsSender {
    data class Result(val recordId: String, val accepted: Boolean, val detail: String)

    fun send(context: Context, source: String, target: String, body: String, mode: String): Result {
        var recordId = ""
        return try {
            val manager = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = manager.divideMessage(body)
            recordId = SmsHistoryStore.add(context, source, target, body, mode, parts.size)
            val sentIntents = ArrayList<PendingIntent>()
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
            for (i in parts.indices) {
                val callbackIntent = Intent(context, SmsSentReceiver::class.java).apply {
                    action = ACTION_SMS_SENT
                    data = Uri.parse("smsforwarder://sent/" + recordId + "/" + i)
                    putExtra(EXTRA_RECORD_ID, recordId)
                    putExtra(EXTRA_PART_INDEX, i)
                    putExtra(EXTRA_TOTAL_PARTS, parts.size)
                }
                sentIntents.add(PendingIntent.getBroadcast(context, i, callbackIntent, flags))
            }
            if (parts.size > 1) {
                manager.sendMultipartTextMessage(target, null, parts, sentIntents, null)
            } else {
                manager.sendTextMessage(target, null, body, null, sentIntents.first())
            }
            Log.i(TAG, "SMS send call accepted for local record " + recordId)
            Result(recordId, true, "Awaiting Android send callback.")
        } catch (e: Exception) {
            if (recordId.isBlank()) {
                recordId = SmsHistoryStore.add(context, source, target, body, mode, 1)
            }
            val detail = e.localizedMessage ?: e.javaClass.simpleName
            SmsHistoryStore.fail(context, recordId, detail)
            Log.e(TAG, "SMS send request failed for local record " + recordId, e)
            Result(recordId, false, detail)
        }
    }

    const val ACTION_SMS_SENT = "com.example.smsforwarder.ACTION_SMS_SENT"
    const val EXTRA_RECORD_ID = "record_id"
    const val EXTRA_PART_INDEX = "part_index"
    const val EXTRA_TOTAL_PARTS = "total_parts"
    private const val TAG = "SmsSender"
}
