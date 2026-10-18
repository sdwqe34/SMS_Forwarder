package com.example.smsforwarder

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.database.Cursor
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var sourceInput: EditText
    private lateinit var targetInput: EditText
    private lateinit var autoSwitch: Switch
    private lateinit var rootLayout: LinearLayout
    private var selectedSmsBody: String? = null
    private var selectedSmsAddress = ""
    private var draftSource = ""
    private var draftTarget = ""
    private var draftEnabled = false
    private var activeTab = TAB_FORWARD
    private var isPersian = false
    private var pendingContactSource = false
    private var pendingSmsSource = true

    private var showRuleEditor = false
    private var editingRuleId: String? = null
    private var editingRuleDraft: ForwardingRule? = null
    private var ruleSourceInput: EditText? = null
    private var ruleContainsInput: EditText? = null
    private var ruleExcludesInput: EditText? = null
    private var ruleEnabledSwitch: Switch? = null
    private val ruleChannelChecks = linkedMapOf<String, CheckBox>()
    private val ruleDestinationInputs = linkedMapOf<String, EditText>()

    private data class InboxItem(val address: String, val body: String, val date: Long)
    private data class ContactItem(val name: String, val number: String)

    private val bg = Color.rgb(246, 248, 252)
    private val navy = Color.rgb(19, 31, 56)
    private val muted = Color.rgb(92, 107, 133)
    private val teal = Color.rgb(10, 122, 107)
    private val tealLight = Color.rgb(224, 245, 240)
    private val blue = Color.rgb(33, 79, 184)
    private val blueLight = Color.rgb(232, 240, 252)
    private val border = Color.rgb(224, 229, 240)
    private val white = Color.WHITE
    private val amber = Color.rgb(154, 99, 12)
    private val red = Color.rgb(184, 56, 61)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun t(en: String, fa: String): String = if (isPersian) fa else en

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)
        isPersian = prefs.getString("language", null)?.let { it == "fa" }
            ?: (Locale.getDefault().language == "fa")
        draftSource = prefs.getString("source", "").orEmpty()
        draftTarget = prefs.getString("target", "").orEmpty()
        draftEnabled = prefs.getBoolean("enabled", false)
        render()
        intent?.let { handleDeliveryIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeliveryIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::rootLayout.isInitialized) render()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = permissions.indices.filter {
            it < grantResults.size && grantResults[it] == PackageManager.PERMISSION_GRANTED
        }.map { permissions[it] }.toSet()
        val denied = permissions.indices.filter {
            it >= grantResults.size || grantResults[it] != PackageManager.PERMISSION_GRANTED
        }.map { permissions[it] }.toSet()

        when (requestCode) {
            REQUEST_CONTACTS -> {
                if (Manifest.permission.READ_CONTACTS in granted) showContactsPicker(pendingContactSource)
                else showPermissionHelp("contacts")
            }
            REQUEST_SMS_PICKER -> {
                if (Manifest.permission.READ_SMS in granted) showSmsPicker(pendingSmsSource)
                else showPermissionHelp("sms")
            }
            REQUEST_SEND_SMS -> {
                if (Manifest.permission.SEND_SMS in denied) showPermissionHelp("sms")
                else toast(t("Permission checked. Tap send again.", "مجوز بررسی شد؛ دوباره ارسال را بزن."))
            }
            else -> {
                when {
                    denied.any {
                        it == Manifest.permission.READ_SMS ||
                            it == Manifest.permission.RECEIVE_SMS ||
                            it == Manifest.permission.SEND_SMS
                    } -> showPermissionHelp("sms")
                    Manifest.permission.READ_CONTACTS in denied -> showPermissionHelp("contacts")
                    else -> toast(t("Permissions updated.", "مجوزها به‌روزرسانی شدند."))
                }
            }
        }
        render()
    }

    private fun captureDraft() {
        if (activeTab == TAB_FORWARD && ::sourceInput.isInitialized &&
            ::targetInput.isInitialized && ::autoSwitch.isInitialized) {
            draftSource = sourceInput.text.toString().trim()
            draftTarget = targetInput.text.toString().trim()
            draftEnabled = autoSwitch.isChecked
        }
    }

    private fun render() {
        captureDraft()
        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = if (isPersian) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(12))
            setBackgroundColor(white)
        }
        val brand = TextView(this).apply {
            text = "↗  SMS Forwarder"
            textSize = 19f
            setTextColor(navy)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(makeButton(if (isPersian) "EN  |  فا" else "FA  |  EN", false) {
            captureDraft()
            isPersian = !isPersian
            prefs.edit().putString("language", if (isPersian) "fa" else "en").apply()
            render()
        }, LinearLayout.LayoutParams(dp(88), dp(42)))
        rootLayout.addView(header, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
        }
        when (activeTab) {
            TAB_FORWARD -> buildForwardPage(page)
            TAB_RULES -> buildRulesPage(page)
            TAB_HISTORY -> buildHistoryPage(page)
            else -> buildAboutPage(page)
        }
        scroll.addView(page)
        rootLayout.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        buildBottomNavigation(rootLayout)
        setContentView(rootLayout)
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun card(parent: LinearLayout, padding: Int = 16): LinearLayout {
        val view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
            background = rounded(white, 18, border)
        }
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.bottomMargin = dp(12)
        parent.addView(view, lp)
        return view
    }

    private fun makeText(
        text: String,
        size: Float = 14f,
        color: Int = navy,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        gravity = if (isPersian) Gravity.RIGHT else Gravity.LEFT
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun label(
        parent: LinearLayout,
        text: String,
        size: Float = 14f,
        color: Int = navy,
        bold: Boolean = false
    ) {
        parent.addView(makeText(text, size, color, bold), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun makeButton(text: String, primary: Boolean = true, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 12f
            isAllCaps = false
            setTextColor(if (primary) white else teal)
            backgroundTintList = ColorStateList.valueOf(if (primary) teal else tealLight)
            minHeight = dp(42)
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { action() }
        }

    private fun addButton(
        parent: LinearLayout,
        text: String,
        primary: Boolean = true,
        action: () -> Unit
    ) {
        val btn = makeButton(text, primary, action)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(7)
        parent.addView(btn, lp)
    }

    private fun buttonRow(
        parent: LinearLayout,
        a: String,
        b: String,
        actionA: () -> Unit,
        actionB: () -> Unit
    ) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(makeButton(a, false, actionA), LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ).apply { marginEnd = dp(5); topMargin = dp(5) })
        row.addView(makeButton(b, false, actionB), LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ).apply { marginStart = dp(5); topMargin = dp(5) })
        parent.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun buildForwardPage(parent: LinearLayout) {
        label(parent, t("SMS AUTOMATION", "مدیریت هوشمند پیامک"), 10f, teal, true)
        label(parent, t("Forward with confidence", "فوروارد، با کنترل کامل"), 25f, navy, true)
        label(parent, t(
            "Choose a rule. Only matching new messages are forwarded.",
            "قانون را انتخاب کن؛ فقط پیام‌های منطبق منتقل می‌شوند."
        ), 13f, muted)

        val permissionCard = card(parent, 14)
        label(permissionCard, permissionSummary(), 14f, navy, true)
        label(permissionCard, t(
            "Some Android versions restrict SMS permissions for sideloaded apps. Review access before enabling automatic forwarding.",
            "در بعضی نسخه‌های اندروید، مجوز پیامک برای برنامه‌های نصب‌شده از فایل محدود است. پیش از فعال‌سازی، دسترسی را بررسی کن."
        ), 12f, muted)
        addButton(permissionCard, t("Permissions & troubleshooting", "مجوزها و راهنمای رفع مشکل"), false) {
            requestRequiredPermissions()
        }

        val ruleCard = card(parent)
        label(ruleCard, t("Forwarding rule", "قانون فوروارد"), 17f, navy, true)
        label(ruleCard, t("SENDER", "فرستنده"), 10f, muted, true)
        sourceInput = EditText(this).apply {
            hint = t("Sender ID or phone number, e.g. MALIAT", "شناسه یا شماره فرستنده؛ مثلاً MALIAT")
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            setText(draftSource)
            textSize = 14f
            setTextColor(navy)
            setHintTextColor(muted)
            background = rounded(bg, 10, border)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }
        ruleCard.addView(sourceInput, matchWidth())
        buttonRow(ruleCard, t("Choose SMS", "انتخاب از پیامک‌ها"), t("Contacts", "مخاطبین"),
            { showSmsPicker(true) }, { showContactsPicker(true) })

        label(ruleCard, t("RECIPIENT", "گیرنده"), 10f, muted, true)
        targetInput = EditText(this).apply {
            hint = t("Destination number with country code", "شماره مقصد با کد کشور")
            inputType = InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
            setText(draftTarget)
            textSize = 14f
            setTextColor(navy)
            setHintTextColor(muted)
            background = rounded(bg, 10, border)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }
        ruleCard.addView(targetInput, matchWidth())
        buttonRow(ruleCard, t("Recent SMS", "پیامک‌های اخیر"), t("Contacts", "مخاطبین"),
            { showSmsPicker(false) }, { showContactsPicker(false) })

        autoSwitch = Switch(this).apply {
            text = t("Auto-forward new messages", "فوروارد خودکار پیامک‌های جدید")
            textSize = 13f
            setTextColor(navy)
            isChecked = draftEnabled
            setPadding(0, dp(8), 0, dp(6))
        }
        ruleCard.addView(autoSwitch, matchWidth())
        label(ruleCard, t(
            "This screen keeps one legacy SMS rule. Use Rules for independent multi-sender and multi-channel automation.",
            "این صفحه یک قانون قدیمی پیامکی را مدیریت می‌کند؛ برای قوانین مستقل و چندروش ارسال، از تب «قوانین» استفاده کن."
        ), 11f, muted)
        addButton(ruleCard, t("Save forwarding rule", "ذخیره قانون فوروارد")) { saveSettings() }

        val manualCard = card(parent)
        label(manualCard, t("Send one existing message", "ارسال دستی یک پیام قدیمی"), 16f, navy, true)
        label(manualCard, selectedSmsBody?.let {
            t("Selected from: ", "فرستنده: ") + selectedSmsAddress + "\n" + it.take(240)
        } ?: t(
            "Choose an SMS from your inbox to send it once, after confirmation.",
            "برای ارسال یک‌باره، ابتدا پیام را از صندوق انتخاب و سپس ارسال را تأیید کن."
        ), 12f, muted)
        buttonRow(manualCard, t("Select message", "انتخاب پیام"), t("Send this message", "ارسال همین پیام"),
            { showSmsPicker(true) }, { confirmForwardSelectedSms() })

        val batteryCard = card(parent, 14)
        label(batteryCard, t("Background delivery", "عملکرد در پس‌زمینه"), 14f, navy, true)
        label(batteryCard, t(
            "Battery optimization may delay background work on some devices.",
            "بهینه‌سازی باتری ممکن است عملکرد پس‌زمینه را در برخی گوشی‌ها به تأخیر بیندازد."
        ), 12f, muted)
        addButton(batteryCard, t("Battery settings", "تنظیمات باتری"), false) { requestIgnoreBattery() }
    }


    private fun makeRuleEditText(hintText: String, value: String = "", phone: Boolean = false): EditText =
        EditText(this).apply {
            hint = hintText
            inputType = if (phone) InputType.TYPE_CLASS_PHONE else InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            setText(value)
            textSize = 14f
            setTextColor(navy)
            setHintTextColor(muted)
            background = rounded(bg, 10, border)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }

    private fun buildRulesPage(parent: LinearLayout) {
        label(parent, t("ROUTING STUDIO", "مدیریت قوانین انتقال"), 10f, teal, true)
        label(parent, t("Rules & destinations", "قوانین و مقصدها"), 25f, navy, true)
        label(parent, t(
            "Create independent rules for different senders, filters and one or more delivery channels.",
            "برای فرستنده‌های مختلف، شرط متن و یک یا چند روش انتقال، قوانین مستقل بساز."
        ), 13f, muted)

        val note = card(parent, 14)
        label(note, t("How delivery works", "روش کار ارسال"), 14f, navy, true)
        label(note, t(
            "SMS rules send directly when Android grants SMS permission. Email and messenger rules create a notification; tapping it opens a prepared message so you can review the recipient and send it yourself.",
            "قوانین SMS در صورت اعطای مجوز اندروید، پیامک را مستقیم می‌فرستند. برای ایمیل و پیام‌رسان‌ها اعلان ساخته می‌شود؛ با لمس آن، متن آماده باز می‌شود تا مقصد را بررسی و خودت ارسال کنی."
        ), 12f, muted)
        addButton(note, t("Review permissions", "بررسی مجوزها"), false) {
            requestRequiredPermissions()
        }

        if (showRuleEditor) buildRuleEditor(parent)

        val rules = ForwardingRuleStore.all(this)
        val listCard = card(parent)
        label(listCard, t("Saved rules", "قوانین ذخیره‌شده"), 17f, navy, true)
        label(listCard, t("Rule count: ", "تعداد قانون: ") + rules.size.toString(), 11f, muted)
        addButton(listCard, t("+ Add a rule", "+ افزودن قانون جدید")) {
            editingRuleId = null
            editingRuleDraft = null
            showRuleEditor = true
            render()
        }

        if (rules.isEmpty()) {
            label(listCard, t(
                "No multi-channel rules yet. Add a rule to route messages from a sender.",
                "هنوز قانونی ثبت نشده است؛ برای انتقال پیام‌های یک فرستنده، قانون جدید اضافه کن."
            ), 12f, muted)
        } else {
            rules.forEach { rule ->
                val item = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = rounded(bg, 12, border)
                }
                val itemParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(10) }
                listCard.addView(item, itemParams)
                label(item, rule.source, 15f, navy, true)
                val channelsText = rule.channels.sorted().joinToString(" · ") { channelLabel(it) }
                label(item, channelsText, 12f, teal, true)
                val conditions = buildList {
                    if (rule.containsText.isNotBlank()) add(t("Contains: ", "شامل: ") + rule.containsText)
                    if (rule.excludesText.isNotBlank()) add(t("Excludes: ", "به‌جز: ") + rule.excludesText)
                }.joinToString("  |  ")
                if (conditions.isNotBlank()) label(item, conditions, 11f, muted)
                val activeSwitch = Switch(this).apply {
                    text = t("Rule enabled", "فعال بودن قانون")
                    textSize = 12f
                    setTextColor(navy)
                    isChecked = rule.enabled
                    setOnCheckedChangeListener { _, checked ->
                        ForwardingRuleStore.save(this@MainActivity, rule.copy(enabled = checked))
                        toast(if (checked) t("Rule enabled.", "قانون فعال شد.") else t("Rule paused.", "قانون متوقف شد."))
                    }
                }
                item.addView(activeSwitch, matchWidth())
                buttonRow(
                    item,
                    t("Edit", "ویرایش"),
                    t("Delete", "حذف"),
                    {
                        editingRuleId = rule.id
                        editingRuleDraft = rule
                        showRuleEditor = true
                        render()
                    },
                    {
                        AlertDialog.Builder(this)
                            .setTitle(t("Delete this rule?", "این قانون حذف شود؟"))
                            .setMessage(rule.source)
                            .setPositiveButton(t("Delete", "حذف")) { _, _ ->
                                ForwardingRuleStore.delete(this, rule.id)
                                if (editingRuleId == rule.id) {
                                    editingRuleId = null
                                    editingRuleDraft = null
                                    showRuleEditor = false
                                }
                                render()
                            }
                            .setNegativeButton(t("Cancel", "انصراف"), null)
                            .show()
                    }
                )
            }
        }

        label(parent, t(
            "Tip: Use a short, unique phrase as a content condition when only certain messages from the same sender should be routed.",
            "نکته: اگر فقط بعضی پیام‌های یک فرستنده باید منتقل شوند، یک عبارت کوتاه و مشخص را به‌عنوان شرط متن وارد کن."
        ), 11f, muted)
    }

    private fun buildRuleEditor(parent: LinearLayout) {
        val existing = editingRuleDraft
        val editor = card(parent)
        label(editor, if (existing == null) t("New forwarding rule", "قانون انتقال جدید")
            else t("Edit forwarding rule", "ویرایش قانون انتقال"), 17f, navy, true)
        label(editor, t(
            "A rule runs only when the sender and optional text conditions match.",
            "قانون فقط زمانی اجرا می‌شود که فرستنده و شرط‌های اختیاری متن منطبق باشند."
        ), 12f, muted)

        label(editor, t("Sender address or ID", "شماره یا شناسه فرستنده"), 12f, navy, true)
        ruleSourceInput = makeRuleEditText(t("e.g. +98912… or MALIAT", "مثلاً +98912… یا MALIAT"), existing?.source.orEmpty())
        editor.addView(ruleSourceInput, matchWidth())

        label(editor, t("Message must contain (optional)", "پیام باید شامل این عبارت باشد (اختیاری)"), 12f, navy, true)
        ruleContainsInput = makeRuleEditText(t("Text to match", "عبارت مورد جست‌وجو"), existing?.containsText.orEmpty())
        editor.addView(ruleContainsInput, matchWidth())

        label(editor, t("Message must NOT contain (optional)", "پیام نباید شامل این عبارت باشد (اختیاری)"), 12f, navy, true)
        ruleExcludesInput = makeRuleEditText(t("Text to exclude", "عبارت مستثنا"), existing?.excludesText.orEmpty())
        editor.addView(ruleExcludesInput, matchWidth())

        label(editor, t("Delivery channels and destinations", "روش‌های ارسال و مقصد هرکدام"), 14f, navy, true)
        label(editor, t(
            "Select one or more channels. For messenger apps, enter a helpful recipient label/handle; you will confirm the actual recipient inside that app.",
            "یک یا چند روش را انتخاب کن. برای پیام‌رسان‌ها، نام یا شناسه مقصد را وارد کن؛ انتخاب نهایی گیرنده داخل همان پیام‌رسان انجام می‌شود."
        ), 11f, muted)

        ruleChannelChecks.clear()
        ruleDestinationInputs.clear()
        val channels = listOf(
            ForwardingRuleStore.SMS to t("SMS", "پیامک"),
            ForwardingRuleStore.EMAIL to t("Email", "ایمیل"),
            ForwardingRuleStore.TELEGRAM to "Telegram",
            ForwardingRuleStore.WHATSAPP to "WhatsApp",
            ForwardingRuleStore.BALE to "Bale",
            ForwardingRuleStore.EITAA to "Eitaa"
        )
        channels.forEach { (key, displayName) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val check = CheckBox(this).apply {
                text = displayName
                textSize = 13f
                setTextColor(navy)
                isChecked = existing?.channels?.contains(key) ?: (key == ForwardingRuleStore.SMS)
            }
            ruleChannelChecks[key] = check
            row.addView(check, LinearLayout.LayoutParams(dp(112), ViewGroup.LayoutParams.WRAP_CONTENT))
            val dest = makeRuleEditText(channelHint(key), existing?.destinations?.get(key).orEmpty(),
                phone = key == ForwardingRuleStore.SMS)
            ruleDestinationInputs[key] = dest
            row.addView(dest, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
            editor.addView(row, params)
        }

        ruleEnabledSwitch = Switch(this).apply {
            text = t("Enable this rule", "فعال‌سازی قانون")
            textSize = 13f
            setTextColor(navy)
            isChecked = existing?.enabled ?: true
        }
        editor.addView(ruleEnabledSwitch, matchWidth())

        addButton(editor, t("Save rule", "ذخیره قانون")) { saveManagedRule() }
        addButton(editor, t("Cancel", "انصراف"), false) {
            showRuleEditor = false
            editingRuleId = null
            editingRuleDraft = null
            render()
        }
    }

    private fun channelHint(channel: String): String = when (channel) {
        ForwardingRuleStore.SMS -> t("Phone number", "شماره تلفن")
        ForwardingRuleStore.EMAIL -> t("name@example.com", "name@example.com")
        ForwardingRuleStore.TELEGRAM -> t("Username or recipient label", "نام کاربری یا نام گیرنده")
        ForwardingRuleStore.WHATSAPP -> t("Phone number / recipient", "شماره یا نام گیرنده")
        ForwardingRuleStore.BALE -> t("Recipient ID / label", "شناسه یا نام گیرنده")
        ForwardingRuleStore.EITAA -> t("Recipient ID / label", "شناسه یا نام گیرنده")
        else -> ""
    }

    private fun channelLabel(channel: String): String = when (channel) {
        ForwardingRuleStore.SMS -> t("SMS", "پیامک")
        ForwardingRuleStore.EMAIL -> t("Email", "ایمیل")
        ForwardingRuleStore.TELEGRAM -> "Telegram"
        ForwardingRuleStore.WHATSAPP -> "WhatsApp"
        ForwardingRuleStore.BALE -> "Bale"
        ForwardingRuleStore.EITAA -> "Eitaa"
        else -> channel
    }

    private fun saveManagedRule() {
        val source = ruleSourceInput?.text?.toString()?.trim().orEmpty()
        val contains = ruleContainsInput?.text?.toString()?.trim().orEmpty()
        val excludes = ruleExcludesInput?.text?.toString()?.trim().orEmpty()
        val channels = ruleChannelChecks.filterValues { it.isChecked }.keys.toSet()
        val destinations = ruleDestinationInputs.mapValues { it.value.text.toString().trim() }

        if (source.isBlank()) {
            toast(t("Enter a sender address or ID.", "شماره یا شناسه فرستنده را وارد کن."))
            return
        }
        if (channels.isEmpty()) {
            toast(t("Select at least one delivery channel.", "حداقل یک روش ارسال انتخاب کن."))
            return
        }
        val missing = channels.filter { destinations[it].isNullOrBlank() }
        if (missing.isNotEmpty()) {
            toast(t(
                "Enter a destination for: " + missing.joinToString { channelLabel(it) },
                "برای این روش‌ها مقصد وارد کن: " + missing.joinToString { channelLabel(it) }
            ))
            return
        }
        val rule = ForwardingRule(
            id = editingRuleId ?: java.util.UUID.randomUUID().toString(),
            source = source,
            containsText = contains,
            excludesText = excludes,
            channels = channels,
            destinations = destinations,
            enabled = ruleEnabledSwitch?.isChecked ?: true
        )
        ForwardingRuleStore.save(this, rule)
        showRuleEditor = false
        editingRuleId = null
        editingRuleDraft = null
        toast(t("Rule saved.", "قانون ذخیره شد."))
        render()
    }

    private fun buildHistoryPage(parent: LinearLayout) {
        label(parent, t("ACTIVITY LOG", "گزارش فعالیت"), 10f, teal, true)
        label(parent, t("Message history", "سوابق پیام‌ها"), 25f, navy, true)
        label(parent, t(
            "Review the source, destination, content, date, time and send status of each attempt.",
            "مبدأ، مقصد، متن، زمان و وضعیت هر تلاش برای ارسال را ببین."
        ), 13f, muted)
        val records = SmsHistoryStore.records(this)
        val summary = card(parent, 14)
        label(summary, t(records.size.toString() + " stored records", records.size.toString() + " رکورد ذخیره‌شده"), 14f, navy, true)
        label(summary, t(
            "History stays on this device. Clear it whenever you need.",
            "سوابق فقط روی همین گوشی می‌مانند؛ هر زمان خواستی پاکشان کن."
        ), 11f, muted)
        if (records.isEmpty()) {
            val empty = card(parent, 22)
            label(empty, t("No sends recorded yet", "هنوز سابقه‌ای ثبت نشده است"), 16f, navy, true)
            label(empty, t(
                "After a manual or automatic send attempt, its details will appear here.",
                "بعد از هر تلاش ارسال دستی یا خودکار، جزئیات آن اینجا نمایش داده می‌شود."
            ), 12f, muted)
        } else {
            records.forEach { item ->
                val itemCard = card(parent, 14)
                val status = item.optString("status", "queued")
                val mode = item.optString("mode", "manual")
                val statusColor = when (status) {
                    "sent" -> teal
                    "failed" -> red
                    else -> amber
                }
                val statusText = if (mode.startsWith("handoff_") && status == "queued")
                    t("Waiting for user confirmation", "در انتظار تأیید و ارسال توسط کاربر")
                else statusLabel(status)
                label(itemCard, statusText, 12f, statusColor, true)
                label(itemCard, t("From", "مبدأ") + ": " + item.optString("source", "—"), 12f, navy, true)
                label(itemCard, t("To", "مقصد") + ": " + item.optString("target", "—"), 12f, navy, true)
                label(itemCard, t("Date & time", "تاریخ و زمان") + ": " + formatTimestamp(item.optLong("timestamp")), 11f, muted)
                label(itemCard, t("Type", "نوع") + ": " + modeLabel(mode), 11f, muted)
                val bodyBox = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    background = rounded(bg, 10)
                }
                bodyBox.addView(makeText(
                    item.optString("body", "").ifBlank { t("(empty message)", "(پیام خالی)") }.take(900),
                    12f, navy
                ))
                itemCard.addView(bodyBox, matchWidth())
                val detail = item.optString("detail", "")
                if (detail.isNotBlank()) label(itemCard, t("Details", "توضیحات") + ": " + detail, 11f, muted)
                label(itemCard, t("Record ID", "شناسه رکورد") + ": " + item.optString("id", "—"), 9f, muted)
            }
        }
        addButton(parent, t("Clear history", "پاک‌کردن سوابق"), false) {
            AlertDialog.Builder(this)
                .setTitle(t("Clear local history?", "سوابق محلی پاک شود؟"))
                .setMessage(t(
                    "This removes all saved send records from this device.",
                    "تمام رکوردهای ذخیره‌شده‌ی ارسال از همین دستگاه حذف می‌شوند."
                ))
                .setNegativeButton(t("Cancel", "انصراف"), null)
                .setPositiveButton(t("Clear", "پاک کن")) { _, _ ->
                    SmsHistoryStore.clear(this)
                    render()
                }.show()
        }
        label(parent, t(
            "“Sent to network” means Android accepted the send result; it does not prove the recipient received the SMS.",
            "«ارسال‌شده به شبکه» یعنی اندروید نتیجه‌ی ارسال را تأیید کرده؛ تحویل قطعی به گیرنده را ثابت نمی‌کند."
        ), 11f, muted)
    }

    private fun buildAboutPage(parent: LinearLayout) {
        label(parent, t("SUPPORT & INFORMATION", "پشتیبانی و اطلاعات"), 10f, teal, true)
        label(parent, t("About SMS Forwarder", "درباره SMS Forwarder"), 25f, navy, true)
        label(parent, t(
            "An open-source Android utility for controlled SMS forwarding.",
            "ابزاری متن‌باز برای فوروارد کنترل‌شده پیامک در اندروید."
        ), 13f, muted)

        val perm = card(parent)
        label(perm, t("SMS permissions on Android", "مجوز پیامک در اندروید"), 16f, navy, true)
        label(perm, t(
            "READ_SMS, RECEIVE_SMS and SEND_SMS may be classified as hard-restricted permissions. The installer must allowlist them; the app cannot grant them to itself.",
            "مجوزهای READ_SMS، RECEIVE_SMS و SEND_SMS ممکن است «محدودشده‌ی سخت» باشند. نصب‌کننده باید اجازه‌ی فهرست‌شدن آن‌ها را بدهد؛ خود برنامه نمی‌تواند این محدودیت را برطرف کند."
        ), 12f, muted)
        label(perm, t(
            "If shown, Settings → Apps → SMS Forwarder → ⋮ → Allow restricted settings may help with some restricted settings, but it does not override hard-restricted SMS permission rules.",
            "در صورت نمایش، مسیر تنظیمات ← برنامه‌ها ← SMS Forwarder ← سه‌نقطه ← Allow restricted settings ممکن است برای برخی تنظیمات محدود مفید باشد؛ اما قوانین مجوز پیامکِ محدودشده‌ی سخت را دور نمی‌زند."
        ), 12f, muted)
        label(perm, t(
            "If SMS stays unavailable, use a compliant installer/distribution method that can allowlist restricted permissions. Do not disable device security or use unknown APKs as a workaround.",
            "اگر مجوز پیامک همچنان در دسترس نیست، باید از روش نصب/توزیع مجاز استفاده شود که بتواند مجوزهای محدودشده را allowlist کند. برای دورزدن مشکل امنیت دستگاه را خاموش یا APK ناشناس نصب نکن."
        ), 12f, muted)
        addButton(perm, t("Open app settings", "بازکردن تنظیمات برنامه"), false) { openAppSettings() }

        val about = card(parent)
        label(about, "SMS Forwarder", 18f, navy, true)
        label(about, t("Project repository", "مخزن پروژه"), 12f, muted, true)
        label(about, REPOSITORY_URL, 12f, blue)
        addButton(about, t("Open GitHub repository", "بازکردن مخزن در گیت‌هاب")) { openUrl(REPOSITORY_URL) }
        addButton(about, t("View MIT license", "مشاهده مجوز MIT"), false) {
            openUrl(REPOSITORY_URL + "/blob/main/LICENSE")
        }
        val version = try {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.4.0"
        } catch (_: Exception) { "1.4.0" }
        label(about, t("Version ", "نسخه ") + version, 12f, muted)
        label(about, t(
            "Preferences and send history are stored locally. No external server or Internet permission is required.",
            "تنظیمات و سوابق ارسال روی دستگاه ذخیره می‌شوند. برنامه به سرور بیرونی یا مجوز اینترنت نیاز ندارد."
        ), 12f, muted)
        label(about, "با تشکر از M.H.K", 15f, teal, true)
    }

    private fun buildBottomNavigation(parent: LinearLayout) {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            setBackgroundColor(white)
            gravity = Gravity.CENTER
        }
        val labels = arrayOf(t("Forward", "فوروارد"), t("Rules", "قوانین"), t("History", "سوابق"), t("About", "درباره"))
        labels.forEachIndexed { index, text ->
            val button = Button(this).apply {
                this.text = text
                isAllCaps = false
                textSize = 12f
                setTextColor(if (activeTab == index) teal else muted)
                backgroundTintList = ColorStateList.valueOf(if (activeTab == index) tealLight else white)
                setOnClickListener {
                    captureDraft()
                    activeTab = index
                    render()
                }
            }
            bar.addView(button, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            })
        }
        parent.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun matchWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(6) }

    private fun permissionSummary(): String {
        val smsGranted = hasPermission(Manifest.permission.READ_SMS) &&
            hasPermission(Manifest.permission.RECEIVE_SMS) &&
            hasPermission(Manifest.permission.SEND_SMS)
        val contactsGranted = hasPermission(Manifest.permission.READ_CONTACTS)
        if (smsGranted && contactsGranted) return t("✓ Permissions ready", "✓ مجوزها آماده‌اند")
        return t("Check permissions before using", "پیش از استفاده، مجوزها را بررسی کن") +
            " · " + t("SMS", "پیامک") + ": " + if (smsGranted) "✓" else "—" +
            " · " + t("Contacts", "مخاطبین") + ": " + if (contactsGranted) "✓" else "—"
    }

    private fun saveSettings() {
        captureDraft()
        if (draftEnabled && (draftSource.isBlank() || draftTarget.isBlank())) {
            toast(t(
                "Enter or choose both a sender and a destination before enabling auto-forwarding.",
                "برای فوروارد خودکار، فرستنده و شماره مقصد را وارد یا انتخاب کن."
            ))
            return
        }
        if (draftEnabled && (!hasPermission(Manifest.permission.RECEIVE_SMS) ||
                !hasPermission(Manifest.permission.SEND_SMS))) {
            AlertDialog.Builder(this)
                .setTitle(t("SMS permissions are not ready", "مجوزهای پیامک آماده نیستند"))
                .setMessage(t(
                    "The rule can be saved, but auto-forwarding will not work until Android grants RECEIVE_SMS and SEND_SMS.",
                    "قانون ذخیره می‌شود، اما تا وقتی اندروید مجوزهای RECEIVE_SMS و SEND_SMS را ندهد، فوروارد خودکار کار نمی‌کند."
                ))
                .setPositiveButton(t("Review permissions", "بررسی مجوزها")) { _, _ -> requestRequiredPermissions() }
                .setNegativeButton(t("Save disabled", "ذخیره بدون فعال‌سازی")) { _, _ ->
                    draftEnabled = false
                    getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
                        .putString("source", draftSource).putString("target", draftTarget)
                        .putBoolean("enabled", false).apply()
                    syncLegacyRuleToManagedStore()
                    render()
                }.show()
            return
        }
        getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
            .putString("source", draftSource)
            .putString("target", draftTarget)
            .putBoolean("enabled", draftEnabled)
            .apply()
        syncLegacyRuleToManagedStore()
        toast(t("Forwarding rule saved.", "قانون فوروارد ذخیره شد."))
        render()
    }

    private fun syncLegacyRuleToManagedStore() {
        val source = draftSource.trim()
        val target = draftTarget.trim()
        if (source.isBlank() || target.isBlank()) {
            ForwardingRuleStore.delete(this, "legacy-single-rule")
            return
        }
        ForwardingRuleStore.save(
            this,
            ForwardingRule(
                id = "legacy-single-rule",
                source = source,
                channels = setOf(ForwardingRuleStore.SMS),
                destinations = mapOf(ForwardingRuleStore.SMS to target),
                enabled = draftEnabled
            )
        )
    }

    private fun requestRequiredPermissions() {
        val missing = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        ).toMutableList().apply {
            if (android.os.Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }.filterNot { hasPermission(it) }
        if (missing.isEmpty()) {
            toast(t("SMS and Contacts permissions are granted.", "مجوزهای پیامک و مخاطبین فعال‌اند."))
            render()
            return
        }
        val prefs = getSharedPreferences("cfg", Context.MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("permission_request_attempted", false)
        val hasSmsMissing = missing.any {
            it == Manifest.permission.RECEIVE_SMS ||
                it == Manifest.permission.READ_SMS ||
                it == Manifest.permission.SEND_SMS
        }
        if (alreadyAsked && hasSmsMissing) {
            showPermissionHelp("sms")
            return
        }
        prefs.edit().putBoolean("permission_request_attempted", true).apply()
        requestPermissions(missing.toTypedArray(), REQUEST_ALL)
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (hasPermission(permission)) return
        val attempted = getSharedPreferences("cfg", Context.MODE_PRIVATE)
            .getBoolean("permission_request_attempted", false)
        if (permission != Manifest.permission.READ_CONTACTS && attempted) {
            showPermissionHelp("sms")
            return
        }
        if (permission != Manifest.permission.READ_CONTACTS) {
            getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
                .putBoolean("permission_request_attempted", true).apply()
        }
        requestPermissions(arrayOf(permission), requestCode)
    }

    private fun showPermissionHelp(type: String) {
        val sms = type == "sms"
        val title = if (sms) t(
            "Android is restricting SMS access",
            "اندروید دسترسی پیامک را محدود کرده است"
        ) else t("Contacts permission is needed", "مجوز مخاطبین لازم است")
        val message = if (sms) t(
            "1. Open Android Settings → Apps → SMS Forwarder → Permissions and enable SMS items if available.\n\n" +
                "2. On some devices, App info → ⋮ → Allow restricted settings may be shown. Enable it only if you trust this app, then check Permissions again.\n\n" +
                "3. Important: Allow restricted settings does not override hard-restricted SMS permissions. The installer may need to allowlist READ_SMS, RECEIVE_SMS and SEND_SMS. The app cannot grant those permissions to itself.",
            "۱. تنظیمات اندروید ← برنامه‌ها ← SMS Forwarder ← مجوزها را باز کن و موارد پیامک را در صورت نمایش فعال کن.\n\n" +
                "۲. در بعضی گوشی‌ها، مسیر اطلاعات برنامه ← سه‌نقطه ← Allow restricted settings نمایش داده می‌شود. فقط در صورت اعتماد به برنامه آن را فعال کن و دوباره مجوزها را بررسی کن.\n\n" +
                "۳. نکته مهم: این گزینه قوانین مجوزهای پیامکِ محدودشده‌ی سخت را دور نمی‌زند. ممکن است نصب‌کننده لازم باشد مجوزهای READ_SMS، RECEIVE_SMS و SEND_SMS را allowlist کند. خود برنامه نمی‌تواند این مجوزها را به خودش بدهد."
        ) else t(
            "Open Android Settings → Apps → SMS Forwarder → Permissions → Contacts, then allow access. Return here and try the Contacts button again.",
            "تنظیمات اندروید ← برنامه‌ها ← SMS Forwarder ← مجوزها ← مخاطبین را باز و دسترسی را فعال کن؛ سپس برگرد و دکمه مخاطبین را دوباره بزن."
        )
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(t("Open app settings", "بازکردن تنظیمات برنامه")) { _, _ -> openAppSettings() }
            .setNeutralButton(t("Try permission request", "درخواست دوباره مجوز")) { _, _ ->
                getSharedPreferences("cfg", Context.MODE_PRIVATE).edit()
                    .putBoolean("permission_request_attempted", false).apply()
                requestRequiredPermissions()
            }
            .setNegativeButton(t("Close", "بستن"), null)
            .show()
    }

    private fun openAppSettings() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName)))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun showSmsPicker(forSource: Boolean) {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
            pendingSmsSource = forSource
            requestPermission(Manifest.permission.READ_SMS, REQUEST_SMS_PICKER)
            return
        }
        val messages = mutableListOf<InboxItem>()
        try {
            contentResolver.query(
                Uri.parse("content://sms/inbox"),
                arrayOf("address", "body", "date"),
                null, null, "date DESC"
            )?.use { cursor: Cursor ->
                val ai = cursor.getColumnIndex("address")
                val bi = cursor.getColumnIndex("body")
                val di = cursor.getColumnIndex("date")
                while (cursor.moveToNext() && messages.size < MAX_SMS_ITEMS) {
                    val address = if (ai >= 0) cursor.getString(ai).orEmpty().trim() else ""
                    if (address.isBlank()) continue
                    val body = if (bi >= 0) cursor.getString(bi).orEmpty() else ""
                    val date = if (di >= 0) cursor.getLong(di) else 0L
                    messages.add(InboxItem(address, body, date))
                }
            }
        } catch (_: SecurityException) {
            showPermissionHelp("sms")
            return
        } catch (e: Exception) {
            toast(t("Could not read SMS inbox: ", "خواندن صندوق پیامک ناموفق بود: ") + (e.localizedMessage ?: ""))
            return
        }
        if (messages.isEmpty()) {
            toast(t("No received SMS messages were found.", "پیامک دریافتی‌ای پیدا نشد."))
            return
        }
        val labels = messages.map { item ->
            val date = if (item.date > 0) DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT
            ).format(Date(item.date)) else ""
            item.address + "  ·  " + date + "\n" + item.body.replace("\n", " ").take(72)
        }
        AlertDialog.Builder(this)
            .setTitle(if (forSource) t("Select a sender message", "پیام فرستنده را انتخاب کن")
                else t("Choose recipient from recent SMS", "گیرنده را از پیامک‌های اخیر انتخاب کن"))
            .setItems(labels.toTypedArray()) { _, index ->
                val selected = messages[index]
                if (forSource) {
                    draftSource = selected.address
                    sourceInput.setText(selected.address)
                    selectedSmsBody = selected.body
                    selectedSmsAddress = selected.address
                } else {
                    draftTarget = selected.address
                    targetInput.setText(selected.address)
                }
                render()
            }
            .setNegativeButton(t("Cancel", "انصراف"), null)
            .show()
    }

    private fun showContactsPicker(forSource: Boolean) {
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            pendingContactSource = forSource
            requestPermission(Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS)
            return
        }
        val contacts = mutableListOf<ContactItem>()
        val seen = mutableSetOf<String>()
        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                while (cursor.moveToNext() && contacts.size < MAX_CONTACT_ITEMS) {
                    val name = cursor.getString(0).orEmpty().ifBlank { t("Unnamed contact", "مخاطب بدون نام") }
                    val number = cursor.getString(1).orEmpty().trim()
                    if (number.isBlank()) continue
                    val key = number.filter { it.isDigit() }.ifBlank { number }
                    if (seen.add(key)) contacts.add(ContactItem(name, number))
                }
            }
        } catch (_: SecurityException) {
            showPermissionHelp("contacts")
            return
        } catch (_: Exception) {
            toast(t("Could not read contacts.", "خواندن مخاطبین ناموفق بود."))
            return
        }
        if (contacts.isEmpty()) {
            toast(t("No contacts with phone numbers were found.", "مخاطبی با شماره تلفن پیدا نشد."))
            return
        }
        AlertDialog.Builder(this)
            .setTitle(if (forSource) t("Select sender contact", "انتخاب فرستنده از مخاطبین")
                else t("Select recipient contact", "انتخاب گیرنده از مخاطبین"))
            .setItems(contacts.map { it.name + "   ·   " + it.number }.toTypedArray()) { _, index ->
                val item = contacts[index]
                if (forSource) {
                    draftSource = item.number
                    sourceInput.setText(item.number)
                    selectedSmsBody = null
                    selectedSmsAddress = ""
                } else {
                    draftTarget = item.number
                    targetInput.setText(item.number)
                }
                render()
            }
            .setNegativeButton(t("Cancel", "انصراف"), null)
            .show()
    }

    private fun confirmForwardSelectedSms() {
        val body = selectedSmsBody
        val target = if (::targetInput.isInitialized) targetInput.text.toString().trim() else draftTarget
        if (body.isNullOrBlank()) {
            toast(t("Choose a message from the inbox first.", "ابتدا یک پیام از صندوق انتخاب کن."))
            return
        }
        if (target.isBlank()) {
            toast(t("Enter or choose a recipient number.", "شماره گیرنده را وارد یا انتخاب کن."))
            return
        }
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            requestPermission(Manifest.permission.SEND_SMS, REQUEST_SEND_SMS)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(t("Confirm send", "تأیید ارسال"))
            .setMessage(t("Send the selected message to:\n", "پیام انتخاب‌شده به این شماره ارسال شود؟\n") +
                target + "\n\n" + body.take(260))
            .setPositiveButton(t("Send", "ارسال")) { _, _ ->
                val result = SmsSender.send(
                    this,
                    selectedSmsAddress.ifBlank { t("Selected inbox message", "پیام انتخاب‌شده از صندوق") },
                    target, body, "manual"
                )
                toast(if (result.accepted) t(
                    "Send request queued. Check History for the result.",
                    "درخواست ارسال ثبت شد؛ نتیجه را در سوابق بررسی کن."
                ) else t("Send failed: ", "ارسال ناموفق بود: ") + result.detail)
                activeTab = TAB_HISTORY
                render()
            }
            .setNegativeButton(t("Cancel", "انصراف"), null)
            .show()
    }


    private fun handleDeliveryIntent(deliveryIntent: Intent) {
        if (deliveryIntent.action != ACTION_OPEN_CHANNEL) return
        val channel = deliveryIntent.getStringExtra(EXTRA_DELIVERY_CHANNEL).orEmpty()
        val source = deliveryIntent.getStringExtra(EXTRA_DELIVERY_SOURCE).orEmpty()
        val target = deliveryIntent.getStringExtra(EXTRA_DELIVERY_TARGET).orEmpty()
        val body = deliveryIntent.getStringExtra(EXTRA_DELIVERY_BODY).orEmpty()
        if (body.isBlank()) return
        deliveryIntent.action = null

        if (channel == ForwardingRuleStore.EMAIL) {
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", target, null)).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Forwarded SMS from $source")
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(emailIntent)
            } catch (_: Exception) {
                toast(t("No email app is available. Copy the message from History.", "برنامه‌ای برای ایمیل پیدا نشد؛ متن را از سوابق بردار."))
            }
            return
        }

        val appPackage = when (channel) {
            ForwardingRuleStore.TELEGRAM -> "org.telegram.messenger"
            ForwardingRuleStore.WHATSAPP -> "com.whatsapp"
            ForwardingRuleStore.BALE -> "ir.nasim"
            ForwardingRuleStore.EITAA -> "ir.eitaa.messenger"
            else -> null
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val appIntent = Intent(shareIntent)
        if (!appPackage.isNullOrBlank()) appIntent.setPackage(appPackage)
        AlertDialog.Builder(this)
            .setTitle(t("Review and send message", "بررسی و ارسال پیام"))
            .setMessage(t(
                "The selected app will open with the message text. Check the recipient ($target) in that app, then press Send. SMS Forwarder cannot silently send on your behalf through third-party messenger apps.",
                "برنامه‌ی انتخاب‌شده با متن پیام باز می‌شود. گیرنده ($target) را داخل آن بررسی کن و سپس ارسال را بزن. SMS Forwarder نمی‌تواند از طرف تو و بدون تأیید، در پیام‌رسان‌های دیگر پیام بفرستد."
            ))
            .setPositiveButton(t("Continue", "ادامه")) { _, _ ->
                try {
                    startActivity(appIntent)
                } catch (_: Exception) {
                    try {
                        startActivity(Intent.createChooser(shareIntent, t("Choose an app", "انتخاب برنامه")))
                    } catch (_: Exception) {
                        toast(t("No compatible app was found.", "برنامه‌ی سازگار پیدا نشد."))
                    }
                }
            }
            .setNegativeButton(t("Cancel", "انصراف"), null)
            .show()
    }

    private fun requestIgnoreBattery() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            toast(t("Battery optimization is already ignored.", "برنامه از بهینه‌سازی باتری مستثنا شده است."))
            return
        }
        try {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:" + packageName)))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun statusLabel(status: String): String = when (status) {
        "sent" -> t("Sent to network · not delivery proof", "ارسال‌شده به شبکه · نه تأیید تحویل")
        "failed" -> t("Failed", "ناموفق")
        "sending" -> t("Sending / waiting for result", "در حال ارسال / انتظار نتیجه")
        else -> t("Queued", "در صف")
    }

    private fun modeLabel(mode: String): String = when {
        mode == "automatic" -> t("Automatic SMS forwarding", "فوروارد خودکار پیامک")
        mode.startsWith("handoff_") -> t("User-confirmed handoff: ", "واگذاری برای تأیید کاربر: ") +
            channelLabel(mode.removePrefix("handoff_"))
        else -> t("Manual one-time send", "ارسال دستی یک‌باره")
    }

    private fun formatTimestamp(timestamp: Long): String =
        if (timestamp <= 0) "—" else DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.SHORT,
            if (isPersian) Locale("fa", "IR") else Locale.getDefault()
        ).format(Date(timestamp))

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            toast(t("Could not open the link.", "بازکردن پیوند ناموفق بود."))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun hasPermission(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_ALL = 100
        private const val REQUEST_CONTACTS = 101
        private const val REQUEST_SMS_PICKER = 102
        private const val REQUEST_SEND_SMS = 103
        const val TAB_FORWARD = 0
        const val TAB_RULES = 1
        const val TAB_HISTORY = 2
        const val TAB_ABOUT = 3
        const val ACTION_OPEN_CHANNEL = "com.example.smsforwarder.OPEN_CHANNEL"
        const val EXTRA_DELIVERY_CHANNEL = "delivery_channel"
        const val EXTRA_DELIVERY_SOURCE = "delivery_source"
        const val EXTRA_DELIVERY_TARGET = "delivery_target"
        const val EXTRA_DELIVERY_BODY = "delivery_body"
        private const val MAX_SMS_ITEMS = 100
        private const val MAX_CONTACT_ITEMS = 300
        private const val REPOSITORY_URL = "https://github.com/sdwqe34/SMS_Forwarder"
    }
}
