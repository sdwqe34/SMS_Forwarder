# SMS Forwarder

An Android app for **selectively forwarding SMS messages**. Choose a sender from your SMS inbox or contacts, choose a destination from contacts, recent SMS senders, or enter a phone number, and decide whether future messages from that sender should be forwarded automatically.

> **Important:** Selecting a message from the inbox sets its sender as the filter for future incoming messages. You can also send the selected existing message once using **Send this message to recipient**. The app does not automatically forward every SMS from every sender.

## Features

- **Selective automatic forwarding:** forward future incoming SMS messages only when the sender matches the saved sender filter.
- **Choose a sender from the inbox:** selecting a received message fills the filter with the sender address reported by Android, which is useful for service IDs such as `MALIAT`.
- **Choose a sender from Contacts:** select a contact phone number as the filter.
- **Choose a recipient:** use a number from Contacts, choose a sender from recent received SMS, or type the destination number manually.
- **One-time forwarding:** explicitly forward the currently selected existing SMS to the chosen recipient.
- **Multi-part SMS support:** long messages are split and sent using Android's SMS APIs.
- **Local configuration:** settings are stored on the device; this app does not need an internet permission or an external server.

## How to use

1. Install the APK and open **SMS Forwarder**.
2. Approve the SMS and Contacts permissions requested by the app.
3. Under **Select sender**, choose **From SMS** and tap a message from the desired sender, or choose a person from Contacts. You can also type a service ID such as `MALIAT` manually.
4. Under **Select recipient**, choose a contact, select a sender from recent SMS, or enter the destination phone number with its country code.
5. Turn on **Automatically forward new SMS from this sender** and tap **Save settings**.
6. Test with a non-sensitive SMS. Only new incoming messages matching the saved sender filter should be forwarded automatically.
7. To send the selected old message once, select it in the inbox and tap **Send this message to recipient**. This action asks for confirmation before sending.

## Sender matching notes

- Selecting a message from the SMS inbox stores the sender address supplied by Android, rather than relying on the contact's display name.
- For ordinary phone numbers, the app uses Android phone-number comparison and a conservative suffix fallback for country-code formatting differences.
- Alphanumeric service sender IDs (for example, `MALIAT`) are matched case-insensitively. If a service message is not forwarded, select one of its messages from the SMS inbox and save the settings again. Some carriers may present sender IDs differently.
- Selecting a contact as the source uses that contact's phone number; it is best for senders that appear as phone numbers, not alphanumeric service IDs.

## Permissions

- `RECEIVE_SMS`: receive new SMS broadcasts.
- `READ_SMS`: show received messages so you can choose a sender or select a message for one-time forwarding.
- `SEND_SMS`: send the forwarded message.
- `READ_CONTACTS`: select source or recipient phone numbers from contacts.
- Battery optimization exemption is optional and can help on some devices where aggressive battery management delays background processing.

Only grant permissions if you understand why the app needs them. Message content and contact details are sensitive; use this app only with messages and destinations you are authorized to forward. Forwarding can incur carrier SMS charges.

## Troubleshooting

- **Nothing is forwarded:** verify that automatic forwarding is enabled, press **Save settings**, confirm the sender and destination, and ensure SMS permissions are granted.
- **A service sender does not match:** select an SMS from that service directly in the inbox so the app stores the originating address Android reports, then save the settings.
- **Permission prompts were denied:** tap **SMS and Contacts permissions** in the app or re-enable them in Android Settings → Apps → SMS Forwarder → Permissions.
- **Background delivery is delayed:** check your phone's battery restrictions and allow background activity for the app.
- **Destination receives no SMS:** verify the phone number and country code, SIM service, SMS balance/plan, and that the device has network coverage.

The app logs forwarding failures under the `SmsReceiver` tag, which can be inspected with Android logcat during debugging. A successful build does not guarantee delivery by the mobile carrier; always test on your own device.

## Build from source

The Android project is in the `SmsForwarder/` directory. Open that directory in Android Studio, or install JDK 17 and Gradle 8.7 and run:

```bash
cd SmsForwarder
gradle --no-daemon assembleDebug
```

The debug APK is generated at:

```
SmsForwarder/app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions also builds a debug APK on pushes to `main` and `master`. Open the repository's **Actions → Build APK** workflow and download the `sms-forwarder-maliat-debug` artifact from the latest successful run.

## App description

**SMS Forwarder** lets you select the sender you care about from your SMS inbox or Contacts, select the intended recipient, and forward only matching incoming messages automatically. You can also send one selected existing message manually. The application is designed to keep the selection and forwarding rules explicit, reducing accidental forwarding of unrelated messages.
