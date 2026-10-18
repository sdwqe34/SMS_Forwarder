package com.example.smsforwarder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Local-only SMS send log. Entries are never uploaded. */
object SmsHistoryStore {
    private const val PREFS = "sms_history"
    private const val KEY_RECORDS = "records"
    private const val MAX_RECORDS = 500

    @Synchronized
    fun add(
        context: Context,
        source: String,
        target: String,
        body: String,
        mode: String,
        totalParts: Int
    ): String {
        val id = UUID.randomUUID().toString()
        val entry = JSONObject().apply {
            put("id", id)
            put("timestamp", System.currentTimeMillis())
            put("source", source)
            put("target", target)
            put("body", body)
            put("mode", mode)
            put("status", "sending")
            put("detail", "Awaiting Android SMS sent callback.")
            put("totalParts", totalParts.coerceAtLeast(1))
            put("seenParts", "")
            put("completedParts", 0)
        }
        val all = readArray(context)
        all.put(entry)
        while (all.length() > MAX_RECORDS) all.remove(0)
        writeArray(context, all)
        return id
    }

    @Synchronized
    fun queued(context: Context, id: String, detail: String) {
        update(context, id) { entry ->
            entry.put("status", "queued")
            entry.put("detail", detail)
        }
    }

    @Synchronized
    fun fail(context: Context, id: String, reason: String) {
        update(context, id) { entry ->
            entry.put("status", "failed")
            entry.put("detail", reason)
        }
    }

    @Synchronized
    fun updatePart(context: Context, id: String, partIndex: Int, totalParts: Int, success: Boolean, resultCode: Int) {
        update(context, id) { entry ->
            val total = entry.optInt("totalParts", totalParts).coerceAtLeast(1)
            if (!success) {
                entry.put("status", "failed")
                entry.put("detail", "Android reported send failure (result code " + resultCode + ").")
            } else if (entry.optString("status") != "failed") {
                val seen = entry.optString("seenParts", "").split(",")
                    .filter { it.isNotBlank() }.toMutableSet()
                seen.add(partIndex.toString())
                entry.put("seenParts", seen.joinToString(","))
                entry.put("completedParts", seen.size)
                if (seen.size >= total) {
                    entry.put("status", "sent")
                    entry.put("detail", "Android accepted all SMS parts for sending; final recipient delivery is not confirmed.")
                } else {
                    entry.put("status", "sending")
                    entry.put("detail", "Accepted " + seen.size + " of " + total + " SMS parts.")
                }
            }
        }
    }

    @Synchronized
    fun records(context: Context): List<JSONObject> {
        val source = readArray(context)
        val result = mutableListOf<JSONObject>()
        for (i in source.length() - 1 downTo 0) {
            source.optJSONObject(i)?.let { result.add(JSONObject(it.toString())) }
        }
        return result
    }

    @Synchronized
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_RECORDS).apply()
    }

    private fun update(context: Context, id: String, action: (JSONObject) -> Unit) {
        val all = readArray(context)
        for (i in 0 until all.length()) {
            val item = all.optJSONObject(i) ?: continue
            if (item.optString("id") == id) {
                action(item)
                break
            }
        }
        writeArray(context, all)
    }

    private fun readArray(context: Context): JSONArray {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_RECORDS, "[]")
        return try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
    }

    private fun writeArray(context: Context, array: JSONArray) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_RECORDS, array.toString()).commit()
    }
}
