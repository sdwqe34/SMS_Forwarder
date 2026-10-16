package com.example.smsforwarder

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {

    private lateinit var etSource: EditText
    private lateinit var etTarget: EditText
    private lateinit var swEnabled: Switch
    private lateinit var tvSelectedSms: TextView
    private lateinit var btnForwardSelected: Button
    private var selectedSmsBody: String? = null

    private data class SmsItem(val address: String, val body: String, val date: Long)
    private data class ContactItem(val name: String, val number: String)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(24))
        }

        val title = TextView(this).apply {
            text = "فوروارد پیامک"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        }
        val intro = TextView(this).apply {
            text = "فرستنده را از پیامک‌های دریافتی یا مخاطبین انتخاب کن؛ سپس گیرنده را انتخاب کن. در حالت خودکار، پیامک‌های جدیدِ فرستندهٔ انتخاب‌شده به مقصد می‌روند."
            textSize = 14f
            setPadding(0, 0, 0, dp(18))
        }

        root.addView(title)
        root.addView(intro)
        addSectionTitle(root, "۱) انتخاب فرستنده")
        etSource = EditText(this).apply {
            hint = "شماره یا شناسه فرستنده؛ مثلاً MALIAT"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(prefs.getString("source", ""))
            isSingleLine = true
        }
        root.addView(etSource, matchWidth())
        addButtonRow(
            root,
            "انتخاب از پیامک‌ها" to { showSmsPicker(forSource = true) },
            "انتخاب از مخاطبین" to { showContactsPicker(forSource = true) }
        )

        addSectionTitle(root, "۲) انتخاب گیرنده")
        etTarget = EditText(this).apply {
            hint = "شماره مقصد با کد کشور، یا انتخاب از فهرست"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(prefs.getString("target", ""))
            isSingleLine = true
        }
        root.addView(etTarget, matchWidth())
        addButtonRow(
            root,
            "از پیامک‌های اخیر" to { showSmsPicker(forSource = false) },
            "از مخاطبین" to { showContactsPicker(forSource = false) }
        )

        swEnabled = Switch(this).apply {
            text = "فوروارد خودکار پیامک‌های جدید از این فرستنده"
            isChecked = prefs.getBoolean("enabled", false)
            setPadding(0, dp(12), 0, dp(12))
        }
        root.addView(swEnabled, matchWidth())

        val btnSave = Button(this).apply {
            text = "ذخیره تنظیمات"
            setOnClickListener { saveSettings() }
        }
        root.addView(btnSave, matchWidth())

        addSectionTitle(root, "ارسال دستی یک پیام انتخاب‌شده")
        tvSelectedSms = TextView(this).apply {
            text = "هنوز پیامی از صندوق انتخاب نشده است. انتخاب پیام، فرستنده را نیز برای حالت خودکار تنظیم می‌کند."
            textSize = 13f
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        root.addView(tvSelectedSms, matchWidth())

        btnForwardSelected = Button(this).apply {
            text = "ارسال همین پیام به گیرنده"
            isEnabled = false
            setOnClickListener { confirmForwardSelectedSms() }
        }
        root.addView(btnForwardSelected, matchWidth())

        addButtonRow(
            root,
            "مجوزهای پیامک و مخاطبین" to { requestRequiredPermissions() },
            "تنظیمات باتری" to { requestIgnoreBattery() }
        )

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
        requestRequiredPermissions()
    }

    private fun addSectionTitle(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = 17f
            setPadding(0, dp(14), 0, dp(4))
        }, matchWidth())
    }

    private fun matchWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(5)
        }

    private fun addButtonRow(
        parent: LinearLayout,
        vararg buttons: Pair<String, () -> Unit>
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        buttons.forEach { (label, action) ->
            val button = Button(this).apply {
                text = label
                textSize = 12f
                setOnClickListener { action() }
            }
            row.addView(
                button,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = dp(4)
                }
            )
        }
        parent.addView(row, matchWidth())
    }

    private fun saveSettings() {
        val source = etSource.text.toString().trim()
        val target = etTarget.text.toString().trim()
        if (swEnabled.isChecked && (source.isEmpty() || target.isEmpty())) {
            Toast.makeText(
                this,
                "برای فوروارد خودکار، فرستنده و شماره مقصد را وارد یا انتخاب کن.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
            .putString("source", source)
            .putString("target", target)
            .putBoolean("enabled", swEnabled.isChecked)
            .apply()
        Toast.makeText(this, "تنظیمات ذخیره شد", Toast.LENGTH_SHORT).show()
    }

    private fun requestRequiredPermissions() {
        val required = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        val missing = required.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun hasPermission(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun showSmsPicker(forSource: Boolean) {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
            requestRequiredPermissions()
            Toast.makeText(this, "برای نمایش پیامک‌ها، مجوز خواندن پیامک را تأیید کن.", Toast.LENGTH_LONG).show()
            return
        }

        val messages = mutableListOf<SmsItem>()
        try {
            val uri = Uri.parse("content://sms/inbox")
            contentResolver.query(
                uri,
                arrayOf("address", "body", "date"),
                null,
                null,
                "date DESC"
            )?.use { cursor: Cursor ->
                val addressIndex = cursor.getColumnIndex("address")
                val bodyIndex = cursor.getColumnIndex("body")
                val dateIndex = cursor.getColumnIndex("date")
                while (cursor.moveToNext() && messages.size < MAX_SMS_ITEMS) {
                    val address = if (addressIndex >= 0) cursor.getString(addressIndex).orEmpty() else ""
                    if (address.isBlank()) continue
                    val body = if (bodyIndex >= 0) cursor.getString(bodyIndex).orEmpty() else ""
                    val date = if (dateIndex >= 0) cursor.getLong(dateIndex) else 0L
                    messages.add(SmsItem(address.trim(), body, date))
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "دسترسی به صندوق پیامک مجاز نیست.", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "خواندن فهرست پیامک‌ها ناموفق بود: ${e.localizedMessage ?: "خطای نامشخص"}", Toast.LENGTH_LONG).show()
            return
        }

        if (messages.isEmpty()) {
            Toast.makeText(this, "پیامک دریافتی‌ای برای نمایش پیدا نشد.", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = messages.map { item ->
            val date = if (item.date > 0) {
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.date))
            } else {
                ""
            }
            val excerpt = item.body.replace("\n", " ").take(72)
            "${item.address}  ·  ${date}\n${excerpt}"
        }
        AlertDialog.Builder(this)
            .setTitle(if (forSource) "پیام را برای تعیین فرستنده انتخاب کن" else "گیرنده را از فرستنده‌های اخیر انتخاب کن")
            .setItems(labels.toTypedArray()) { _, index ->
                val selected = messages[index]
                if (forSource) {
                    etSource.setText(selected.address)
                    selectedSmsBody = selected.body
                    tvSelectedSms.text = "پیام انتخاب‌شده از ${selected.address}:\n${selected.body.take(280)}"
                    btnForwardSelected.isEnabled = selected.body.isNotBlank()
                } else {
                    etTarget.setText(selected.address)
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun showContactsPicker(forSource: Boolean) {
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            requestRequiredPermissions()
            Toast.makeText(this, "برای انتخاب مخاطب، مجوز مخاطبین را تأیید کن.", Toast.LENGTH_LONG).show()
            return
        }

        val contacts = mutableListOf<ContactItem>()
        val seenNumbers = mutableSetOf<String>()
        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                while (cursor.moveToNext() && contacts.size < MAX_CONTACT_ITEMS) {
                    val name = cursor.getString(0).orEmpty().ifBlank { "مخاطب بدون نام" }
                    val number = cursor.getString(1).orEmpty().trim()
                    if (number.isBlank()) continue
                    val key = number.filter { it.isDigit() }.ifBlank { number }
                    if (seenNumbers.add(key)) contacts.add(ContactItem(name, number))
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "دسترسی به مخاطبین مجاز نیست.", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "خواندن مخاطبین ناموفق بود.", Toast.LENGTH_LONG).show()
            return
        }

        if (contacts.isEmpty()) {
            Toast.makeText(this, "مخاطب دارای شماره تلفن پیدا نشد.", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = contacts.map { "${it.name}   ·   ${it.number}" }
        AlertDialog.Builder(this)
            .setTitle(if (forSource) "انتخاب فرستنده از مخاطبین" else "انتخاب گیرنده از مخاطبین")
            .setItems(labels.toTypedArray()) { _, index ->
                val selected = contacts[index]
                if (forSource) {
                    etSource.setText(selected.number)
                    clearSelectedSms()
                } else {
                    etTarget.setText(selected.number)
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun clearSelectedSms() {
        selectedSmsBody = null
        tvSelectedSms.text = "برای ارسال دستی، ابتدا یک پیام را از صندوق پیامک انتخاب کن."
        btnForwardSelected.isEnabled = false
    }

    private fun confirmForwardSelectedSms() {
        val body = selectedSmsBody
        val target = etTarget.text.toString().trim()
        if (body.isNullOrBlank()) {
            Toast.makeText(this, "ابتدا یک پیام از صندوق انتخاب کن.", Toast.LENGTH_SHORT).show()
            return
        }
        if (target.isBlank()) {
            Toast.makeText(this, "شماره گیرنده را وارد یا انتخاب کن.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            requestRequiredPermissions()
            Toast.makeText(this, "برای ارسال، مجوز ارسال پیامک لازم است.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("تأیید ارسال")
            .setMessage("پیام انتخاب‌شده به شماره زیر ارسال شود؟\n$target")
            .setPositiveButton("ارسال") { _, _ -> sendSmsNow(target, body) }
            .setNegativeButton("لغو", null)
            .show()
    }

    private fun sendSmsNow(target: String, body: String) {
        try {
            val manager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = manager.divideMessage(body)
            if (parts.size > 1) {
                manager.sendMultipartTextMessage(target, null, parts, null, null)
            } else {
                manager.sendTextMessage(target, null, body, null, null)
            }
            Toast.makeText(this, "درخواست ارسال پیامک ثبت شد؛ نتیجه نهایی به شبکه و اپراتور بستگی دارد.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "ارسال پیامک ناموفق بود: ${e.localizedMessage ?: "خطای نامشخص"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestIgnoreBattery() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(this, "قبلاً از بهینه‌سازی باتری مستثنی شده است.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:$packageName"))
            )
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 100
        private const val MAX_SMS_ITEMS = 100
        private const val MAX_CONTACT_ITEMS = 300
    }
}
