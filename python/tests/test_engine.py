from __future__ import annotations

import pytest

from sms_forwarder import (
    DeliveryChannel,
    ForwardingRule,
    IncomingMessage,
    plan_deliveries,
    sender_matches,
)


def test_one_rule_can_plan_multiple_channels() -> None:
    rule = ForwardingRule(
        id="bank-alerts",
        sender="BANK",
        channels={DeliveryChannel.SMS, DeliveryChannel.EMAIL},
        destinations={
            DeliveryChannel.SMS: "+989121234567",
            DeliveryChannel.EMAIL: "alerts@example.com",
        },
        contains_text="Verification",
    )
    plan = plan_deliveries(
        IncomingMessage("bank", "Your VERIFICATION code is 1234.", "msg-1"),
        [rule],
    )
    assert len(plan) == 2
    assert {item.channel for item in plan} == {
        DeliveryChannel.SMS,
        DeliveryChannel.EMAIL,
    }
    assert all(item.rule_id == "bank-alerts" for item in plan)
    assert all(item.source_message_id == "msg-1" for item in plan)


def test_multiple_rules_match_same_message() -> None:
    sms_rule = ForwardingRule(
        id="sms",
        sender="BANK",
        channels={DeliveryChannel.SMS},
        destinations={DeliveryChannel.SMS: "+989121234567"},
    )
    email_rule = ForwardingRule(
        id="email",
        sender="bank",
        channels={DeliveryChannel.EMAIL},
        destinations={DeliveryChannel.EMAIL: "ops@example.com"},
        contains_text="code",
    )
    plan = plan_deliveries(IncomingMessage("BANK", "Your code is 1234"), [sms_rule, email_rule])
    assert [item.rule_id for item in plan] == ["sms", "email"]


def test_excluded_phrase_prevents_routing() -> None:
    rule = ForwardingRule(
        id="no-promos",
        sender="BANK",
        channels={DeliveryChannel.EMAIL},
        destinations={DeliveryChannel.EMAIL: "ops@example.com"},
        excludes_text="promotion",
    )
    assert not plan_deliveries(IncomingMessage("BANK", "Limited PROMOTION today"), [rule])


def test_disabled_rule_and_wrong_sender_do_not_match() -> None:
    rule = ForwardingRule(
        id="off",
        sender="BANK",
        channels={DeliveryChannel.SMS},
        destinations={DeliveryChannel.SMS: "+989121234567"},
        enabled=False,
    )
    assert plan_deliveries(IncomingMessage("BANK", "hello"), [rule]) == []
    other_sender = ForwardingRule(
        id="other",
        sender="SHOP",
        channels={DeliveryChannel.SMS},
        destinations={DeliveryChannel.SMS: "+989121234567"},
    )
    assert plan_deliveries(IncomingMessage("BANK", "hello"), [other_sender]) == []


def test_phone_numbers_normalize_formatting() -> None:
    assert sender_matches("+98 912 123 4567", "0912-123-4567")
    assert sender_matches("BANK", "bank")
    assert not sender_matches("12345", "512345")


def test_empty_message_is_not_routed() -> None:
    rule = ForwardingRule(
        id="rule",
        sender="BANK",
        channels={DeliveryChannel.SMS},
        destinations={DeliveryChannel.SMS: "+989121234567"},
    )
    assert not plan_deliveries(IncomingMessage("BANK", " "), [rule])


def test_destinations_required_for_every_selected_channel() -> None:
    with pytest.raises(ValueError, match="destination"):
        ForwardingRule(
            id="broken",
            sender="BANK",
            channels={DeliveryChannel.SMS, DeliveryChannel.EMAIL},
            destinations={DeliveryChannel.SMS: "+989121234567"},
        )
