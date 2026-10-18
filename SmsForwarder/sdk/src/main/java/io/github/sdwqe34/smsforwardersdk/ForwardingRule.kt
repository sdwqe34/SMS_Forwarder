package io.github.sdwqe34.smsforwardersdk

/**
 * Delivery channel requested by a forwarding rule.
 *
 * This enum describes routing intent only. The host app is responsible for
 * implementing channel delivery and obtaining any platform permissions.
 */
enum class DeliveryChannel(val key: String) {
    SMS("sms"),
    EMAIL("email"),
    TELEGRAM("telegram"),
    WHATSAPP("whatsapp"),
    BALE("bale"),
    EITAA("eitaa");

    companion object {
        fun fromKey(key: String): DeliveryChannel? =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}

/** An incoming message supplied by the host application. */
data class IncomingMessage(
    val sender: String,
    val body: String,
    val messageId: String? = null
)

/**
 * A reusable routing rule.
 *
 * [containsText] is optional and case-insensitive. If [excludesText] is set,
 * a message containing that phrase is not routed by this rule.
 * Every selected channel must have a destination.
 */
data class ForwardingRule(
    val id: String,
    val sender: String,
    val channels: Set<DeliveryChannel>,
    val destinations: Map<DeliveryChannel, String>,
    val containsText: String = "",
    val excludesText: String = "",
    val enabled: Boolean = true
) {
    init {
        require(id.isNotBlank()) { "Rule id must not be blank." }
        require(sender.isNotBlank()) { "Rule sender must not be blank." }
        require(channels.isNotEmpty()) { "Select at least one delivery channel." }
        val missing = channels.filter { destinations[it].isNullOrBlank() }
        require(missing.isEmpty()) {
            "Every selected channel must have a destination: " + missing.joinToString { it.key }
        }
    }
}

/** One delivery instruction returned by [ForwardingEngine]. */
data class PlannedDelivery(
    val ruleId: String,
    val source: String,
    val channel: DeliveryChannel,
    val destination: String,
    val body: String,
    val sourceMessageId: String? = null
)
