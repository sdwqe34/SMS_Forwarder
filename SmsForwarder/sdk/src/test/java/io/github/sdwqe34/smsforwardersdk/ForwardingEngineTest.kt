package io.github.sdwqe34.smsforwardersdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardingEngineTest {
    @Test
    fun routesToEverySelectedChannelWhenSenderAndTextMatch() {
        val rule = ForwardingRule(
            id = "bank-alerts",
            sender = "BANK",
            channels = setOf(DeliveryChannel.SMS, DeliveryChannel.EMAIL),
            destinations = mapOf(
                DeliveryChannel.SMS to "+989121234567",
                DeliveryChannel.EMAIL to "alerts@example.com"
            ),
            containsText = "Verification"
        )

        val result = ForwardingEngine.plan(
            IncomingMessage("bank", "Your VERIFICATION code is 1234.", "msg-1"),
            listOf(rule)
        )

        assertEquals(2, result.size)
        assertEquals(setOf(DeliveryChannel.SMS, DeliveryChannel.EMAIL), result.map { it.channel }.toSet())
        assertTrue(result.all { it.ruleId == "bank-alerts" })
        assertTrue(result.all { it.sourceMessageId == "msg-1" })
    }

    @Test
    fun excludesMessagesContainingExcludedPhrase() {
        val rule = ForwardingRule(
            id = "rule-1",
            sender = "BANK",
            channels = setOf(DeliveryChannel.EMAIL),
            destinations = mapOf(DeliveryChannel.EMAIL to "alerts@example.com"),
            excludesText = "promotion"
        )

        val result = ForwardingEngine.plan(
            IncomingMessage("BANK", "Limited PROMOTION today"),
            listOf(rule)
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun ignoresDisabledRulesAndDifferentSenders() {
        val disabled = ForwardingRule(
            id = "disabled",
            sender = "BANK",
            channels = setOf(DeliveryChannel.SMS),
            destinations = mapOf(DeliveryChannel.SMS to "+989121234567"),
            enabled = false
        )
        val otherSender = disabled.copy(id = "other", sender = "SHOP", enabled = true)

        assertTrue(ForwardingEngine.plan(IncomingMessage("BANK", "hello"), listOf(disabled)).isEmpty())
        assertTrue(ForwardingEngine.plan(IncomingMessage("BANK", "hello"), listOf(otherSender)).isEmpty())
    }

    @Test
    fun normalizesPhoneNumberFormatting() {
        assertTrue(ForwardingEngine.matchesSender("+98 912 123 4567", "0912-123-4567"))
        assertTrue(ForwardingEngine.matchesSender("BANK", "bank"))
    }

    @Test
    fun returnsNoDeliveriesForBlankMessage() {
        val rule = ForwardingRule(
            id = "rule",
            sender = "BANK",
            channels = setOf(DeliveryChannel.SMS),
            destinations = mapOf(DeliveryChannel.SMS to "+989121234567")
        )
        assertTrue(ForwardingEngine.plan(IncomingMessage("BANK", "  "), listOf(rule)).isEmpty())
    }
}
