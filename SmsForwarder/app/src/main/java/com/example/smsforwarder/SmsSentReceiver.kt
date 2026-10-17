package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SmsSender.ACTION_SMS_SENT) return
        val recordId = intent.getStringExtra(SmsSender.EXTRA_RECORD_ID).orEmpty()
        if (recordId.isBlank()) return
        val partIndex = intent.getIntExtra(SmsSender.EXTRA_PART_INDEX, 0)
        val totalParts = intent.getIntExtra(SmsSender.EXTRA_TOTAL_PARTS, 1)
        val success = resultCode == android.app.Activity.RESULT_OK
        val description = when (resultCode) {
            android.app.Activity.RESULT_OK -> "OK"
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "No service"
            SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off"
            else -> "Result code " + resultCode
        }
        SmsHistoryStore.updatePart(context, recordId, partIndex, totalParts, success, resultCode)
        Log.i(TAG, "SMS part " + (partIndex + 1) + "/" + totalParts + " callback: " + description)
    }

    companion object {
        private const val TAG = "SmsSentReceiver"
    }
}
