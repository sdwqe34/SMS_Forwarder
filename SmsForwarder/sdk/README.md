# SMS Forwarder Android SDK

This Android library exposes reusable routing models and a deterministic rule-matching engine. It lets a host app evaluate an incoming message against multiple rules and obtain one delivery plan per selected channel.

**The SDK does not receive SMS, read the inbox, request SMS permissions, send messages, or integrate directly with consumer messaging APIs.** Those responsibilities belong to the host application.

## API example

    import io.github.sdwqe34.smsforwardersdk.DeliveryChannel
    import io.github.sdwqe34.smsforwardersdk.ForwardingEngine
    import io.github.sdwqe34.smsforwardersdk.ForwardingRule
    import io.github.sdwqe34.smsforwardersdk.IncomingMessage

    val rule = ForwardingRule(
        id = "bank-alert",
        sender = "BANK",
        channels = setOf(DeliveryChannel.SMS, DeliveryChannel.EMAIL),
        destinations = mapOf(
            DeliveryChannel.SMS to "+989121234567",
            DeliveryChannel.EMAIL to "alerts@example.com"
        ),
        containsText = "verification",
        excludesText = "promotion"
    )

    val plan = ForwardingEngine.plan(
        IncomingMessage("BANK", "Your verification code is 1234"),
        listOf(rule)
    )

The result is a list of delivery-plan objects. The host app chooses whether and how to carry each delivery out.

## Download the AAR

A tested release AAR is available here:
- [Download sms-forwarder-sdk-1.0.0.aar](https://github.com/sdwqe34/SMS_Forwarder/releases/download/sdk-v1.0.0/sms-forwarder-sdk-1.0.0.aar)
- [Release notes and SHA-256 checksum](https://github.com/sdwqe34/SMS_Forwarder/releases/tag/sdk-v1.0.0)

## Add it to an Android app

After the SDK tag has been published, add JitPack to the host project's repositories:

    dependencyResolutionManagement {
        repositories {
            google()
            mavenCentral()
            maven { url = uri("https://jitpack.io") }
        }
    }

Then add the versioned dependency:

    dependencies {
        implementation("com.github.sdwqe34.SMS_Forwarder:sdk:sdk-v1.0.0")
    }

The coordinates become available after a successful JitPack build of the corresponding tag. JitPack is a third-party distribution service, so verify the tag/build page before adopting a version in production.

## Build locally

From the nested Android project:

    cd SmsForwarder
    gradle --no-daemon :sdk:testDebugUnitTest :sdk:assembleRelease

The AAR is generated at:

    SmsForwarder/sdk/build/outputs/aar/sdk-release.aar

## License

MIT.
