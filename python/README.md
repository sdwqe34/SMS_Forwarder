# sms-forwarder-rules

A small, dependency-free Python rules engine that models the routing logic used by SMS Forwarder. It evaluates supplied messages against multiple rules and produces a list of planned delivery destinations.

**It does not access a phone inbox, send SMS/email, open Telegram/WhatsApp/Bale/Eitaa, or call those services.** A host application or provider integration must supply incoming messages and implement each transport.

## Install

After the project has been published to PyPI:

    python -m pip install sms-forwarder-rules

## Quick start

    from sms_forwarder import DeliveryChannel, ForwardingRule, IncomingMessage, plan_deliveries

    rule = ForwardingRule(
        id="bank-verification",
        sender="BANK",
        contains_text="verification",
        excludes_text="promotion",
        channels={DeliveryChannel.EMAIL, DeliveryChannel.SMS},
        destinations={
            DeliveryChannel.EMAIL: "alerts@example.com",
            DeliveryChannel.SMS: "+989121234567",
        },
    )

    message = IncomingMessage(
        sender="BANK",
        body="Your verification code is 123456",
        message_id="msg-42",
    )

    for delivery in plan_deliveries(message, [rule]):
        print(delivery.channel.value, delivery.destination, delivery.body)

The example returns two **delivery plans**. It sends neither message.

## API

- DeliveryChannel: sms, email, telegram, whatsapp, bale, and eitaa.
- IncomingMessage(sender, body, message_id=None): message data supplied by your host system.
- ForwardingRule: sender, optional contains_text and excludes_text filters, selected channels, per-channel destinations and an enabled switch.
- plan_deliveries(message, rules): return a PlannedDelivery for every selected channel from every matching enabled rule.
- sender_matches(actual, configured): compare sender IDs ignoring case and phone numbers while tolerating common formatting/country-code differences.

Text filtering is case-insensitive. Numeric sender IDs shorter than eight digits must match exactly. For longer phone numbers, the final ten digits may match to accommodate country-code prefixes.

## Test locally

From this directory, install pytest and run:

    python -m pip install pytest
    python -m pytest

## Important boundaries

The package plans destinations only. For automatic SMS forwarding on Android, an Android host app must obtain required platform permissions and obey Android and app-store policies. Email and consumer messenger integrations need a supported API or an explicit user-confirmed handoff. A planned route is not proof of sending or delivery.

## License

MIT. See the repository's LICENSE file.
