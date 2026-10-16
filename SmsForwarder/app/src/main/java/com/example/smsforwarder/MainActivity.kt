package com.example.smsforwarder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var etSource: EditText
    private lateinit var etTarget: EditText
    private lateinit var swEnabled: Switch

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        val title = TextView(this).apply {
            text = "فوروارد پیامک"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        }

        etSource = EditText(this).apply {
            hint = "شماره یا نام فرستنده (مبدأ)"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(prefs.getString("source", ""))
        }
        etTarget = EditText(this).apply {
            hint = "شماره مقصد (مثلاً 09123456789)"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(prefs.getString("target", ""))
        }
        swEnabled = Switch(this).apply {
            text = "فعال"
            isChecked = prefs.getBoolean("enabled", true)
            setPadding(0, dp(12), 0, dp(12))
        }

        val btnSave = Button(this).apply {
            text = "ذخیره"
            setOnClickListener { save() }
        }
        val btnPerm = Button(this).apply {
            text = "درخواست مجوزهای پیامک"
            setOnClickListener { requestSmsPermissions() }
        }
        val btnBattery = Button(this).apply {
            text = "استثنا از بهینه‌سازی باتری"
            setOnClickListener { requestIgnoreBattery() }
        }

        listOf(title, etSource, etTarget, swEnabled, btnSave, btnPerm, btnBattery)
            .forEach { root.addView(it) }

        setContentView(ScrollView(this).apply { addView(root) })
        requestSmsPermissions()
    }

    private fun save() {
        getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
            .putString("source", etSource.text.toString().trim())
            .putString("target", etTarget.text.toString().trim())
            .putBoolean("enabled", swEnabled.isChecked)
            .apply()
        Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
    }

    private fun requestSmsPermissions() {
        val perms = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        )
        val missing = perms.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 1)
        }
    }

    private fun requestIgnoreBattery() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(this, "قبلاً مستثنی شده است", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:$packageName"))
            )
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}
