# SMS Forwarder | فوروارد پیامک

<div align="center">
  <img src="docs/images/sms-forwarder-catalog.svg" width="100%" alt="SMS Forwarder app catalog / کاتالوگ برنامه فوروارد پیامک">
  <br>
  <b>Choose a message. Choose a recipient. Forward only what you select.</b>
  <br>
  <b>پیام را انتخاب کن، گیرنده را مشخص کن و فقط همان پیام‌های موردنظر را فوروارد کن.</b>
</div>

[![Build APK](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml/badge.svg)](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**English:** SMS Forwarder is an Android app that lets you select a sender from your SMS inbox or contacts, choose a recipient, and forward matching *new incoming messages* automatically. You can also select an existing inbox message and send it once, after confirmation.

**فارسی:** «SMS Forwarder» برنامه‌ای برای اندروید است که به شما اجازه می‌دهد فرستنده را از فهرست پیامک‌های دریافتی یا مخاطبین انتخاب کنید، گیرنده را مشخص کنید و پیامک‌های جدید همان فرستنده را به‌صورت خودکار انتقال دهید. همچنین می‌توانید یک پیامک قدیمی را انتخاب کرده و پس از تأیید، فقط همان پیام را یک‌بار ارسال کنید.

## Screenshots & visual guide | تصاویر و راهنما

<div align="center">
  <img src="docs/images/forwarding-flow.svg" width="100%" alt="Forwarding flow / روند انتخاب و فوروارد">
</div>

## Features | امکانات

| English | فارسی |
|---|---|
| Choose a sender from received SMS or Contacts. | انتخاب فرستنده از پیامک‌های دریافتی یا مخاطبین |
| Choose a recipient from Contacts, recent SMS senders, or enter a number. | انتخاب گیرنده از مخاطبین، فرستنده‌های پیامک اخیر یا ورود دستی شماره |
| Auto-forward only new messages that match the saved sender. | فوروارد خودکار فقط برای پیامک‌های جدیدِ فرستندهٔ تعیین‌شده |
| Send one selected existing message manually, with confirmation. | ارسال دستی یک پیامک انتخاب‌شده پس از تأیید |
| Handles long and multi-part SMS messages. | پشتیبانی از پیامک‌های طولانی و چندبخشی |
| Settings are stored on-device; no external server is required. | تنظیمات روی گوشی ذخیره می‌شود و سرور خارجی لازم نیست |

## Quick start | راه‌اندازی سریع

1. Install the APK and open **SMS Forwarder**.  
   فایل APK را نصب و برنامه را باز کنید.
2. Grant the SMS and Contacts permissions when prompted.  
   مجوزهای پیامک و مخاطبین را تأیید کنید.
3. Under **Select sender**, choose a message from the inbox or a phone-number contact. For text sender IDs such as `MALIAT`, select a message from that sender so Android's reported sender ID is saved.  
   در بخش «انتخاب فرستنده»، یک پیامک یا مخاطب را انتخاب کنید. برای سرشماره‌های متنی مثل `MALIAT`، بهتر است پیامک همان سرویس را مستقیماً از صندوق انتخاب کنید.
4. Choose the recipient from Contacts, recent SMS senders, or enter the phone number with country code.  
   گیرنده را از مخاطبین، پیامک‌های اخیر یا با واردکردن شماره همراه کد کشور انتخاب کنید.
5. For future messages, turn on **Automatically forward new SMS from this sender**, then press **Save settings**.  
   برای فوروارد پیامک‌های آینده، گزینهٔ فوروارد خودکار را روشن و «ذخیره تنظیمات» را انتخاب کنید.
6. To send an existing message only once, select it from the inbox and tap **Send this message to recipient**; review and confirm.  
   برای ارسال یک پیام قدیمی، آن را انتخاب کنید، «ارسال همین پیام» را بزنید و تأیید کنید.

> **Important / مهم:** Automatic forwarding is off by default. Other senders are ignored. Only forward messages you are authorized to share; SMS forwarding may expose private or one-time-password information and carrier charges may apply.  
> فوروارد خودکار به‌صورت پیش‌فرض خاموش است و پیامک‌های سایر فرستنده‌ها نادیده گرفته می‌شوند. فقط پیام‌هایی را منتقل کنید که اجازهٔ انتقالشان را دارید؛ پیامک‌ها ممکن است حاوی اطلاعات خصوصی یا کد یک‌بارمصرف باشند و هزینهٔ اپراتور اعمال شود.

## Troubleshooting | عیب‌یابی

- **No automatic forward / فوروارد انجام نمی‌شود:** verify the sender, destination, permissions, and enabled switch; then save settings again.  
  فرستنده، گیرنده، مجوزها و روشن‌بودن فوروارد خودکار را بررسی و تنظیمات را دوباره ذخیره کنید.
- **A service sender such as MALIAT does not match / سرشمارهٔ متنی شناسایی نمی‌شود:** select a message from that sender directly in the inbox, then save the settings. Carriers can represent sender IDs differently.  
  پیامک همان سرویس را مستقیماً از صندوق انتخاب و تنظیمات را ذخیره کنید؛ نمایش سرشماره به اپراتور وابسته است.
- **Permissions denied / مجوزها رد شده‌اند:** enable SMS and Contacts permissions under Android Settings → Apps → SMS Forwarder.  
  از تنظیمات اندروید ← برنامه‌ها ← SMS Forwarder مجوزها را فعال کنید.
- **Background delivery is delayed / تأخیر در پس‌زمینه:** check the phone's battery optimization settings. Actual SMS delivery depends on the SIM, carrier, coverage, and account balance.  
  محدودیت باتری را بررسی کنید. تحویل واقعی پیامک به سیم‌کارت، اپراتور، آنتن‌دهی و اعتبار حساب بستگی دارد.

## Download | دریافت برنامه

- **Test APK / نسخهٔ آزمایشی:** open the latest successful [Build APK workflow](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml) and download the `sms-forwarder-debug` artifact.
- **GitHub Packages / بستهٔ منتشرشده:** [Open the account's Packages page](https://github.com/users/sdwqe34/packages?repo_name=SMS_Forwarder). Package publishing runs when a commit explicitly marked for publishing completes successfully.

## Build from source | ساخت از سورس

Requires JDK 17 and Gradle 8.7. The Android project is in `SmsForwarder/`.

```bash
cd SmsForwarder
gradle --no-daemon clean assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

برای ساخت برنامه به JDK 17 و Gradle 8.7 نیاز دارید. پوشهٔ `SmsForwarder` را در Android Studio باز کنید یا دستورات بالا را اجرا کنید؛ فایل خروجی در `SmsForwarder/app/build/outputs/apk/debug/app-debug.apk` قرار می‌گیرد.

## Privacy & permissions | حریم خصوصی و مجوزها

- `RECEIVE_SMS`: receive new SMS broadcasts / دریافت اعلان پیامک جدید
- `READ_SMS`: list inbox messages for selection / نمایش فهرست پیامک‌ها
- `SEND_SMS`: send selected or matching messages / ارسال پیامک
- `READ_CONTACTS`: choose contacts / انتخاب مخاطب

The app has no Internet permission and stores configuration locally. These permissions grant access to sensitive information, so review them carefully. A successful build does not guarantee delivery by the carrier; test with a non-sensitive message first.

این برنامه مجوز اینترنت ندارد و تنظیمات را روی دستگاه ذخیره می‌کند. مجوزها به اطلاعات حساسی دسترسی می‌دهند؛ پیش از استفاده آن‌ها را بررسی کنید و ابتدا با یک پیامک غیرحساس آزمایش کنید.

## License | مجوز

MIT — see [LICENSE](LICENSE).  
مجوز MIT — متن کامل در فایل [LICENSE](LICENSE) قرار دارد.
