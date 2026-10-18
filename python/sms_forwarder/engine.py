"""Rule matching and delivery planning; no message transport is performed."""

from __future__ import annotations

from collections.abc import Iterable

from .models import ForwardingRule, IncomingMessage, PlannedDelivery


def _digits(value: str) -> str:
    return "".join(character for character in value if character.isdigit())


def sender_matches(actual: str, configured: str) -> bool:
    """Compare sender IDs case-insensitively or normalize phone-number formatting.

    Numeric senders shorter than eight digits must match exactly. Longer phone
    numbers may match by their last ten digits to handle country-code prefixes.
    Alphanumeric sender IDs are compared case-insensitively.
    """
    if not actual or not configured:
        return False
    if actual.casefold() == configured.casefold():
        return True

    actual_digits = _digits(actual)
    configured_digits = _digits(configured)
    if not actual_digits or not configured_digits:
        return False
    if len(actual_digits) < 8 or len(configured_digits) < 8:
        return actual_digits == configured_digits
    suffix_length = min(10, len(actual_digits), len(configured_digits))
    return actual_digits[-suffix_length:] == configured_digits[-suffix_length:]


def plan_deliveries(
    message: IncomingMessage,
    rules: Iterable[ForwardingRule],
) -> list[PlannedDelivery]:
    """Return planned destinations for all enabled rules matching the message.

    Matching is case-insensitive for optional text filters. This function does
    not read a device inbox, send SMS, open an app, send email, call APIs, or
    verify final delivery.
    """
    if not message.sender.strip() or not message.body.strip():
        return []

    planned: list[PlannedDelivery] = []
    for rule in rules:
        if not rule.enabled or not sender_matches(message.sender, rule.sender):
            continue
        if rule.contains_text and rule.contains_text.casefold() not in message.body.casefold():
            continue
        if rule.excludes_text and rule.excludes_text.casefold() in message.body.casefold():
            continue

        for channel in sorted(rule.channels, key=lambda item: item.value):
            destination = rule.destinations[channel].strip()
            if destination:
                planned.append(
                    PlannedDelivery(
                        rule_id=rule.id,
                        source=message.sender,
                        channel=channel,
                        destination=destination,
                        body=message.body,
                        source_message_id=message.message_id,
                    )
                )
    return planned
