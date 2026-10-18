"""Data models shared by the Python SMS Forwarder rules package."""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Mapping


class DeliveryChannel(str, Enum):
    """A requested route type; this does not implement the transport."""

    SMS = "sms"
    EMAIL = "email"
    TELEGRAM = "telegram"
    WHATSAPP = "whatsapp"
    BALE = "bale"
    EITAA = "eitaa"


@dataclass(frozen=True, slots=True)
class IncomingMessage:
    """A message supplied by a host application or integration."""

    sender: str
    body: str
    message_id: str | None = None

    def __post_init__(self) -> None:
        if not isinstance(self.sender, str):
            raise TypeError("sender must be a string")
        if not isinstance(self.body, str):
            raise TypeError("body must be a string")


@dataclass(frozen=True, slots=True)
class ForwardingRule:
    """One conditional routing rule with one or more channel destinations."""

    id: str
    sender: str
    channels: frozenset[DeliveryChannel] | set[DeliveryChannel]
    destinations: Mapping[DeliveryChannel, str]
    contains_text: str = ""
    excludes_text: str = ""
    enabled: bool = True

    def __post_init__(self) -> None:
        if not isinstance(self.id, str) or not self.id.strip():
            raise ValueError("id must not be blank")
        if not isinstance(self.sender, str) or not self.sender.strip():
            raise ValueError("sender must not be blank")
        normalized_channels = frozenset(
            channel if isinstance(channel, DeliveryChannel) else DeliveryChannel(channel)
            for channel in self.channels
        )
        if not normalized_channels:
            raise ValueError("select at least one delivery channel")
        normalized_destinations: dict[DeliveryChannel, str] = {}
        for key, value in self.destinations.items():
            channel = key if isinstance(key, DeliveryChannel) else DeliveryChannel(key)
            if not isinstance(value, str):
                raise TypeError(f"destination for {channel.value} must be a string")
            normalized_destinations[channel] = value.strip()
        missing = [
            channel.value
            for channel in normalized_channels
            if not normalized_destinations.get(channel)
        ]
        if missing:
            raise ValueError(
                "every selected channel must have a destination: " + ", ".join(sorted(missing))
            )
        if not isinstance(self.contains_text, str) or not isinstance(self.excludes_text, str):
            raise TypeError("text filters must be strings")
        if not isinstance(self.enabled, bool):
            raise TypeError("enabled must be a bool")
        object.__setattr__(self, "channels", normalized_channels)
        object.__setattr__(self, "destinations", normalized_destinations)


@dataclass(frozen=True, slots=True)
class PlannedDelivery:
    """A routing instruction. It is not proof of a sent or delivered message."""

    rule_id: str
    source: str
    channel: DeliveryChannel
    destination: str
    body: str
    source_message_id: str | None = None
