package com.example.smsforwarder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Local-only storage for user-defined forwarding rules. */
data class ForwardingRule(
    val id: String = UUID.randomUUID().toString(),
    val source: String,
    val containsText: String = "",
    val excludesText: String = "",
    val channels: Set<String>,
    val destinations: Map<String, String>,
    val enabled: Boolean = true
)

object ForwardingRuleStore {
    const val SMS = "sms"
    const val EMAIL = "email"
    const val TELEGRAM = "telegram"
    const val WHATSAPP = "whatsapp"
    const val BALE = "bale"
    const val EITAA = "eitaa"

    val supportedChannels = listOf(SMS, EMAIL, TELEGRAM, WHATSAPP, BALE, EITAA)

    private const val PREFS = "cfg"
    private const val KEY_RULES = "forwarding_rules_v2"

    @Synchronized
    fun all(context: Context): List<ForwardingRule> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RULES, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val channelsJson = item.optJSONArray("channels") ?: JSONArray()
                    val channels = buildSet {
                        for (i in 0 until channelsJson.length()) {
                            val channel = channelsJson.optString(i)
                            if (channel in supportedChannels) add(channel)
                        }
                    }
                    val destinationsJson = item.optJSONObject("destinations") ?: JSONObject()
                    val destinations = supportedChannels.associateWith { destinationsJson.optString(it, "") }
                    val source = item.optString("source", "").trim()
                    if (source.isBlank() || channels.isEmpty()) continue
                    add(
                        ForwardingRule(
                            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                            source = source,
                            containsText = item.optString("containsText", ""),
                            excludesText = item.optString("excludesText", ""),
                            channels = channels,
                            destinations = destinations,
                            enabled = item.optBoolean("enabled", true)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun save(context: Context, rule: ForwardingRule) {
        val rules = all(context).filterNot { it.id == rule.id } + rule
        persist(context, rules)
    }

    @Synchronized
    fun delete(context: Context, id: String) {
        persist(context, all(context).filterNot { it.id == id })
    }

    private fun persist(context: Context, rules: List<ForwardingRule>) {
        val array = JSONArray()
        rules.forEach { rule ->
            val channels = JSONArray()
            rule.channels.filter { it in supportedChannels }.forEach(channels::put)
            val destinations = JSONObject()
            supportedChannels.forEach { channel ->
                destinations.put(channel, rule.destinations[channel].orEmpty())
            }
            array.put(
                JSONObject()
                    .put("id", rule.id)
                    .put("source", rule.source.trim())
                    .put("containsText", rule.containsText.trim())
                    .put("excludesText", rule.excludesText.trim())
                    .put("channels", channels)
                    .put("destinations", destinations)
                    .put("enabled", rule.enabled)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_RULES, array.toString())
            .apply()
    }
}
