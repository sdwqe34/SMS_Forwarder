"""SMS Forwarder rules engine.

This package models message-matching rules and plans destinations. It does not
read device SMS messages, send SMS, or authenticate with messenger services.
"""

from .engine import plan_deliveries, sender_matches
from .models import DeliveryChannel, ForwardingRule, IncomingMessage, PlannedDelivery

__all__ = [
    "DeliveryChannel",
    "ForwardingRule",
    "IncomingMessage",
    "PlannedDelivery",
    "plan_deliveries",
    "sender_matches",
]

__version__ = "0.1.0"
