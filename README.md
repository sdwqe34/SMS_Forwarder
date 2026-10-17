# SMS Forwarder 1.3.0 | فوروارد پیامک ۱.۳.۰

<div align="center">
  <img src="docs/images/sms-forwarder-catalog.svg" width="100%" alt="SMS Forwarder 1.3.0 bilingual interface preview / پیش‌نمایش رابط دوزبانه برنامه">
  <br>
  <b>Choose a sender. Choose a recipient. Keep a clear local record.</b>
  <br>
  <b>فرستنده و گیرنده را انتخاب کن و سوابق ارسال را روی دستگاه ببین.</b>
</div>

[![Build APK](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml/badge.svg)](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**English:** SMS Forwarder is an open-source Android utility for choosing a sender and recipient, optionally forwarding future matching incoming messages, manually sending one selected inbox message after confirmation, and reviewing a local history of send attempts. The interface can be switched between English and Persian.

**فارسی:** «SMS Forwarder» ابزاری متن‌باز برای اندروید است. می‌توانی فرستنده و گیرنده را انتخاب کنی، فوروارد خودکار پیامک‌های جدیدِ منطبق را فعال کنی، یک پیام قدیمی را پس از تأیید بفرستی و سوابق تلاش‌های ارسال را ببینی. زبان رابط بین فارسی و انگلیسی قابل تغییر است.

> **Important:** The history tracks Android send callbacks; it cannot prove that a carrier delivered a message to the final recipient. SMS forwarding can expose sensitive messages, including authentication codes. Use it only for messages you are authorized to share.

> **مهم:** تاریخچه، callbackهای ارسال اندروید را ثبت می‌کند و تحویل قطعی پیام توسط اپراتور به گیرنده را اثبات نمی‌کند. فوروارد پیامک ممکن است اطلاعات حساس، از جمله کدهای ورود، را افشا کند. فقط پیام‌هایی را فوروارد کن که اجازه‌ی انتقالشان را داری.

## Screens & workflow | تصاویر و روند کار

<div align="center">
  <img src="docs/images/forwarding-flow.svg" width="100%" alt="SMS Forwarder 1.3.0 setup, send status and local history flow / روند راه‌اندازی، وضعیت ارسال و سوابق محلی">
</div>

The artwork above is an updated interface/flow preview, not a screenshot of a running device. The History page in the app itself is populated from local send attempts and Android send callbacks; illustrative entries in the design preview are examples only.

تصاویر بالا پیش‌نمایش به‌روزشده‌ی رابط و روند کار هستند و اسکرین‌شات واقعی از گوشی نیستند. صفحه‌ی سوابق در برنامه از تلاش‌های واقعی ارسال و callbackهای اندروید پر می‌شود؛ اطلاعات نمونه در تصویر صرفاً برای نمایش ظاهر است.

## What's new in 1.3.0 | امکانات نسخه ۱.۳.۰

| English | فارسی |
|---|---|
| Switch the UI between Persian and English. | تغییر زبان رابط بین فارسی و انگلیسی |
| Choose a source from the SMS inbox or Contacts. | انتخاب فرستنده از صندوق پیامک یا مخاطبین |
| Choose a destination from Contacts, recent SMS senders, or type a number. | انتخاب گیرنده از مخاطبین، پیامک‌های اخیر یا ورود شماره |
| Optionally auto-forward future messages from the configured sender. | فوروارد خودکار اختیاری پیامک‌های آینده از فرستنده‌ی تعیین‌شده |
| Send one existing inbox SMS after explicit confirmation. | ارسال یک پیام قدیمی پس از تأیید صریح |
| Local history: source, destination, body, date/time, mode, Android callback status and details. | تاریخچه‌ی محلی شامل مبدأ، مقصد، متن، تاریخ/زمان، نوع و وضعیت callback ارسال |
| A dedicated permissions help page and direct link to Android app settings. | راهنمای مجوزها و پیوند مستقیم به تنظیمات برنامه |
| About page with the repository, MIT license, version and project credit. | صفحه‌ی درباره شامل مخزن، مجوز MIT، نسخه و قدردانی پروژه |

## Download | دریافت برنامه

- **APK release:** [SMS-Forwarder-1.3.0.apk](https://github.com/sdwqe34/SMS_Forwarder/releases/download/v1.3.0/SMS-Forwarder-1.3.0.apk)
- **Release notes and checksum:** [GitHub Release v1.3.0](https://github.com/sdwqe34/SMS_Forwarder/releases/tag/v1.3.0)
- **Builds and CI status:** [GitHub Actions](https://github.com/sdwqe34/SMS_Forwarder/actions/workflows/android.yml)
- **Source code:** [sdwqe34/SMS_Forwarder](https://github.com/sdwqe34/SMS_Forwarder)
- **GitHub Packages (Maven):** [Repository package page](https://github.com/users/sdwqe34/packages?repo_name=SMS_Forwarder)

The release APK is built with Android's debug signing configuration for testing. It is not a Play Store production-signed package. Install only if you trust the source and understand the sensitive SMS permissions.

فایل Release با امضای Debug برای آزمایش ساخته می‌شود و بسته‌ی امضاشده‌ی تولیدی برای Play Store نیست. فقط در صورتی نصب کن که به منبع فایل اعتماد داری و از حساسیت مجوزهای پیامک آگاه هستی.

## Setup | راه‌اندازی

1. Install the APK and open SMS Forwarder.
2. Review **Permissions & troubleshooting**. Grant SMS and Contacts access if Android offers the relevant controls.
3. Choose the sender from an actual inbox message (recommended for alphanumeric sender IDs such as `MALIAT`) or a phone-number contact.
4. Choose the destination from Contacts, recent SMS senders, or enter a number with country code.
5. Keep auto-forwarding off until the sender/destination are correct; enable the switch and save the rule.
6. For one-time sending, choose an inbox message, press **Send this message**, inspect the destination, and confirm.
7. Open **History** to review the text, source, destination, timestamp, send mode and current Android result. The log can be cleared from that page.

۱. فایل APK را نصب و برنامه را باز کن.
۲. بخش «مجوزها و راهنمای رفع مشکل» را بخوان؛ اگر اندروید گزینه‌های مربوط را نمایش داد، مجوزهای پیامک و مخاطبین را فعال کن.
۳. فرستنده را از یک پیام واقعی صندوق انتخاب کن (برای سرشماره‌ی متنی مثل `MALIAT` این روش بهتر است) یا مخاطبی با شماره تلفن انتخاب کن.
۴. گیرنده را از مخاطبین، پیامک‌های اخیر یا با کد کشور وارد کن.
۵. تا وقتی فرستنده و گیرنده را بررسی نکرده‌ای، فوروارد خودکار را خاموش نگه دار؛ سپس گزینه را فعال و قانون را ذخیره کن.
۶. برای ارسال یک‌باره، پیام را انتخاب کن، «ارسال همین پیام» را بزن، شماره مقصد را بررسی و ارسال را تأیید کن.
۷. در «سوابق»، متن، مبدأ، مقصد، زمان، نوع ارسال و آخرین وضعیت اندروید را ببین. از همین صفحه می‌توانی تاریخچه را پاک کنی.

## Samsung Galaxy / Android permission troubleshooting | رفع مشکل مجوز پیامک

### Why the SMS permission button may seem not to work

The permission button first asks Android for the required runtime permissions. On some installations Android does not present a grant option for SMS permissions, or the permissions remain unavailable even after visiting Settings. This is not necessarily caused by Samsung Auto Blocker.

Android treats `READ_SMS`, `RECEIVE_SMS` and `SEND_SMS` as hard-restricted permissions in relevant platform configurations. Granting them can depend on the installer or distribution channel explicitly allowlisting them. An ordinary app cannot add itself to that allowlist.

### Steps to try on a Galaxy S24 FE

1. Open **Settings → Apps → SMS Forwarder → Permissions**. Inspect whether the SMS permissions appear at all; if shown, grant the available ones. Under **Contacts**, grant contact access separately.
2. Return to **Settings → Apps → SMS Forwarder → ⋮**. If **Allow restricted settings** appears, enable it only if you trust the app, then revisit the Permissions page. This setting is not a guaranteed solution for hard-restricted SMS permissions.
3. If the SMS permissions are still missing or unavailable, reinstalling the same APK or pressing the app's button repeatedly may not fix it. Use a compliant installation/distribution route that can grant/allowlist these restricted permissions. Do not disable device security, root the phone, or install an unknown APK to get around the restriction.
4. After any permission change, return to the app and revisit the dashboard. The permission status is refreshed when the app resumes.
5. If Contacts access is denied, press either Contacts button again and use its prompt. If Android no longer offers the dialog, use **Permissions → Contacts** in Settings, then retry the picker.

### Android permission links

- [Android: Change app permissions](https://support.google.com/android/answer/9431959?hl=en)
- [Android: Restricted settings](https://support.google.com/android/answer/12623953?hl=en)
- [Android compatibility definition — permissions](https://source.android.com/docs/compatibility/14/android-14-cdd)
- [Android permission model and allowlisting](https://source.android.com/docs/core/permissions/runtime_perms)

### Persian instructions

دکمه‌ی مجوزها ابتدا از اندروید درخواست می‌کند مجوزهای زمان اجرا را نمایش دهد. در بعضی نصب‌ها، اندروید گزینه‌ی اعطای مجوز پیامک را نمایش نمی‌دهد یا حتی پس از ورود به تنظیمات هم امکان فعال‌سازی آن وجود ندارد؛ این مسئله لزوماً به Auto Blocker سامسونگ مربوط نیست.

در برخی پیکربندی‌های اندروید، مجوزهای `READ_SMS`، `RECEIVE_SMS` و `SEND_SMS` از نوع «محدودشده‌ی سخت» هستند. اعطای آن‌ها ممکن است به این بستگی داشته باشد که نصب‌کننده یا کانال توزیع، مجوزها را صریحاً در فهرست مجاز قرار داده باشد. برنامه نمی‌تواند خودش این محدودیت را برطرف کند.

**مراحل پیشنهادی برای Galaxy S24 FE:**

۱. به **تنظیمات ← برنامه‌ها ← SMS Forwarder ← مجوزها** برو. بررسی کن مجوزهای SMS اصلاً نمایش داده می‌شوند یا نه؛ اگر قابل فعال‌سازی‌اند آن‌ها را فعال کن. مجوز «مخاطبین» را نیز جداگانه بررسی کن.
۲. به **تنظیمات ← برنامه‌ها ← SMS Forwarder ← سه‌نقطه** برو. اگر **Allow restricted settings** نمایش داده شد، فقط در صورت اعتماد به برنامه آن را فعال کن و دوباره صفحه‌ی مجوزها را بررسی کن. این گزینه برای مجوزهای پیامکِ محدودشده‌ی سخت راه‌حل تضمین‌شده نیست.
۳. اگر مجوزهای پیامک همچنان نمایش داده نمی‌شوند یا غیرفعال‌اند، نصب مجدد همان APK یا فشردن مکرر دکمه احتمالاً مشکل را حل نمی‌کند. از روش نصب/توزیع مجازی استفاده کن که بتواند این مجوزها را allowlist کند. برای دورزدن مشکل، امنیت دستگاه را خاموش نکن، گوشی را root نکن و APK ناشناس نصب نکن.
۴. پس از تغییر مجوز، به برنامه برگرد؛ وضعیت دسترسی هنگام بازگشت به برنامه به‌روزرسانی می‌شود.
۵. اگر مجوز مخاطبین رد شده است، یکی از دکمه‌های مخاطبین را دوباره بزن. اگر اندروید دیگر پنجره درخواست را نشان نداد، از تنظیمات برنامه ← مجوزها ← مخاطبین آن را فعال کن و دوباره انتخاب مخاطب را امتحان کن.

## Message history & privacy | تاریخچه و حریم خصوصی

- History is saved locally on the device using app-private storage. It is not uploaded to a server.
- Up to 500 records are retained; older entries are dropped when the limit is exceeded.
- Each record stores the source, recipient, message body, timestamp, manual/automatic mode, Android result status and a diagnostic detail. Message bodies may contain sensitive data.
- The History screen contains a **Clear history** action.
- “Sent to network” means Android reported success for all SMS parts. This app does not receive a carrier delivery receipt, so this status is not proof the recipient received the message.
- The app does not request Internet permission. Carrier charges may apply.

- سوابق ارسال با ذخیره‌سازی خصوصی برنامه روی همان دستگاه نگهداری می‌شوند و به سرور ارسال نمی‌شوند.
- حداکثر ۵۰۰ رکورد نگهداری می‌شود؛ پس از رسیدن به سقف، رکوردهای قدیمی حذف می‌شوند.
- هر رکورد مبدأ، گیرنده، متن پیام، زمان، نوع ارسال دستی/خودکار، وضعیت نتیجه‌ی اندروید و توضیحات خطا را ذخیره می‌کند. متن پیام ممکن است حاوی اطلاعات حساس باشد.
- در صفحه‌ی سوابق دکمه‌ی «پاک‌کردن سوابق» وجود دارد.
- «ارسال‌شده به شبکه» یعنی اندروید برای تمام بخش‌های پیام نتیجه‌ی موفق گزارش کرده است؛ برنامه رسید تحویل اپراتور را دریافت نمی‌کند و این وضعیت تحویل به گیرنده را ثابت نمی‌کند.
- برنامه مجوز اینترنت نمی‌خواهد؛ هزینه‌ی پیامک اپراتور ممکن است اعمال شود.

## Build from source | ساخت از سورس

Requirements: JDK 17 and Gradle 8.7. Open `SmsForwarder/` in Android Studio or run:

```bash
cd SmsForwarder
gradle --no-daemon clean :app:assembleDebug
```

The APK is generated at `SmsForwarder/app/build/outputs/apk/debug/app-debug.apk`.

برای ساخت، JDK 17 و Gradle 8.7 لازم است. پوشه‌ی `SmsForwarder/` را در Android Studio باز کن یا دستورهای بالا را اجرا کن. فایل خروجی در `SmsForwarder/app/build/outputs/apk/debug/app-debug.apk` قرار می‌گیرد.

## About | درباره

- **Repository:** [sdwqe34/SMS_Forwarder](https://github.com/sdwqe34/SMS_Forwarder)
- **License:** [MIT](LICENSE)
- **Version:** 1.3.0
- **Credit:** با تشکر از M.H.K

## License | مجوز

MIT — see [LICENSE](LICENSE).  
مجوز MIT — متن کامل در فایل [LICENSE](LICENSE) قرار دارد.
