package io.github.sdwqe34.smsforwardersdk

/**
 * Evaluates rules and returns delivery instructions.
 *
 * This class does not read the SMS inbox, register a receiver, request Android
 * permissions, or send messages. Those responsibilities stay in the host app.
 */
object ForwardingEngine {
    /**
     * Return one instruction per selected channel of every enabled matching rule.
     * The returned list is a plan; it does not send anything.
     */
    @JvmStatic
    fun plan(message: IncomingMessage, rules: Iterable<ForwardingRule>): List<PlannedDelivery> {
        if (message.sender.isBlank() || message.body.isBlank()) return emptyList()

        return buildList {
            rules.forEach { rule ->
                if (!rule.enabled || !matchesSender(message.sender, rule.sender)) return@forEach
                if (rule.containsText.isNotBlank() &&
                    !message.body.contains(rule.containsText, ignoreCase = true)
                ) return@forEach
                if (rule.excludesText.isNotBlank() &&
                    message.body.contains(rule.excludesText, ignoreCase = true)
                ) return@forEach

                rule.channels.forEach { channel ->
                    val destination = rule.destinations[channel]?.trim().orEmpty()
                    if (destination.isNotBlank()) {
                        add(
                            PlannedDelivery(
                                ruleId = rule.id,
                                source = message.sender,
                                channel = channel,
                                destination = destination,
                                body = message.body,
                                sourceMessageId = message.messageId
                            )
                        )
                    }
                }
            }
        }
    }

    @JvmStatic
    fun matchesSender(actual: String, configured: String): Boolean {
        if (actual.equals(configured, ignoreCase = true)) return true
        val actualDigits = actual.filter(Char::isDigit)
        val configuredDigits = configured.filter(Char::isDigit)
        if (actualDigits.isEmpty() || configuredDigits.isEmpty()) return false
        if (actualDigits.length < 8 || configuredDigits.length < 8) {
            return actualDigits == configuredDigits
        }
        val suffixLength = minOf(10, actualDigits.length, configuredDigits.length)
        return actualDigits.takeLast(suffixLength) == configuredDigits.takeLast(suffixLength)
    }
}
