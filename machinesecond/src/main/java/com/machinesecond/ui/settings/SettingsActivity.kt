package com.machinesecond.ui.settings

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.machinesecond.MsSdk
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.MsToast
import com.machinesecond.util.MsVoiceAlert

/**
 * 双 Tab 设置：发红包 / 抢红包（对齐截图绿色样式）
 */
class SettingsActivity : AppCompatActivity() {

    private var tabSend: TextView? = null
    private var tabGrab: TextView? = null
    private var sendPanel: View? = null
    private var grabPanel: View? = null
    private var tabIndex = 0

    private lateinit var etInterval: EditText
    private lateinit var etAmount: EditText
    private lateinit var etCount: EditText
    private lateinit var etGreeting: EditText
    private lateinit var etPassword: EditText

    private lateinit var etDelay: EditText
    private lateinit var swExclusive: SwitchCompat
    private lateinit var swLucky: SwitchCompat
    private lateinit var swVoice: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar())
        root.addView(buildTabs())
        val body = FrameLayout(this)
        sendPanel = buildSendPanel().also { body.addView(it) }
        grabPanel = buildGrabPanel().also {
            it.visibility = View.GONE
            body.addView(it)
        }
        root.addView(
            body,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        setContentView(root)
        bindFromConfig()
        selectTab(0)
    }

    private fun buildTopBar(): View {
        val bar = FrameLayout(this).apply {
            setBackgroundColor(MsColors.primary)
            minimumHeight = dp(48)
        }
        val back = ImageView(this).apply {
            setImageResource(com.machinesecond.R.drawable.ms_ic_back)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            contentDescription = "返回"
            setOnClickListener { finish() }
        }
        val title = TextView(this).apply {
            text = "设置"
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        bar.addView(
            back,
            FrameLayout.LayoutParams(dp(48), dp(48)).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
        )
        bar.addView(
            title,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )
        return bar
    }

    private fun buildTabs(): View {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.white)
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }
        tabSend = makeTab("发红包") { selectTab(0) }
        tabGrab = makeTab("抢红包") { selectTab(1) }
        row.addView(tabSend, LinearLayout.LayoutParams(0, dp(40), 1f))
        row.addView(tabGrab, LinearLayout.LayoutParams(0, dp(40), 1f))
        wrap.addView(row)
        wrap.addView(View(this).apply {
            setBackgroundColor(MsColors.divider)
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)))
        return wrap
    }

    private fun makeTab(text: String, click: () -> Unit): TextView =
        TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            setOnClickListener { click() }
        }

    private fun selectTab(index: Int) {
        tabIndex = index
        sendPanel?.visibility = if (index == 0) View.VISIBLE else View.GONE
        grabPanel?.visibility = if (index == 1) View.VISIBLE else View.GONE
        styleTab(tabSend, index == 0)
        styleTab(tabGrab, index == 1)
    }

    private fun styleTab(tv: TextView?, selected: Boolean) {
        tv ?: return
        tv.setTextColor(if (selected) MsColors.primary else MsColors.tabInactive)
        tv.typeface = if (selected) {
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
            Typeface.DEFAULT
        }
        val underline = GradientDrawable().apply {
            setColor(if (selected) MsColors.primary else 0x00000000)
        }
        tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        tv.setPadding(0, 0, 0, dp(8))
        // 底部指示条用 background layer 简化：底部 padding + 下划线 view 已在 row 外
        tv.background = if (selected) {
            GradientDrawable().apply {
                setColor(0x00000000)
                setStroke(0, 0)
            }.also {
                // 用底部边框模拟
            }
            object : android.graphics.drawable.LayerDrawable(
                arrayOf(
                    GradientDrawable().apply { setColor(0x00000000) },
                    GradientDrawable().apply { setColor(MsColors.primary) }
                )
            ) {
                init {
                    setLayerInset(1, dp(48), dp(38), dp(48), 0)
                }
            }
        } else {
            null
        }
    }

    private fun buildSendPanel(): View {
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(24))
        }
        etInterval = addFieldRow(col, "自动发包间隔(秒)", "1", InputType.TYPE_CLASS_NUMBER)
        etAmount = addFieldRow(col, "红包金额", "红包金额 (多个用/隔开)", InputType.TYPE_CLASS_TEXT)
        etCount = addFieldRow(col, "红包个数", "红包个数 (多个用/隔开)", InputType.TYPE_CLASS_TEXT)
        etGreeting = addFieldRow(col, "红包语", "红包语 (多个用/隔开)", InputType.TYPE_CLASS_TEXT)
        etPassword = addFieldRow(
            col, "支付密码", "支付密码",
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        )
        col.addView(spacer(dp(24)))
        col.addView(saveButton { saveSend() })
        col.addView(spacer(dp(20)))
        col.addView(disclaimerView())
        scroll.addView(col)
        return scroll
    }

    private fun buildGrabPanel(): View {
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(24))
        }
        etDelay = addFieldRow(col, "抢包延迟(秒)", "0", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        swExclusive = addSwitchRow(col, "自动领取专属红包")
        swLucky = addSwitchRow(col, "自动领取拼手气红包")
        swVoice = addSwitchRow(col, "领取成功语音播报")
        col.addView(spacer(dp(24)))
        col.addView(saveButton { saveGrab() })
        col.addView(spacer(dp(20)))
        col.addView(disclaimerView())
        scroll.addView(col)
        return scroll
    }

    private fun disclaimerView(): TextView = TextView(this).apply {
        text = "本软件仅供个人测试使用，禁止用于商业用途及任何违法违规操作，感谢您的配合。"
        setTextColor(MsColors.disclaimer)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
        gravity = Gravity.CENTER
        setPadding(dp(8), 0, dp(8), 0)
    }

    private fun addFieldRow(
        parent: LinearLayout,
        label: String,
        hint: String,
        inputType: Int
    ): EditText {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        row.addView(TextView(this).apply {
            text = label
            setTextColor(MsColors.labelText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            minWidth = dp(120)
        })
        val et = EditText(this).apply {
            this.hint = hint
            setHintTextColor(MsColors.fieldHint)
            setTextColor(MsColors.fieldText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            this.inputType = inputType
            background = fieldBg()
            setPadding(dp(12), dp(10), dp(12), dp(10))
            maxLines = 1
            isSingleLine = true
        }
        row.addView(
            et,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        parent.addView(row)
        parent.addView(divider())
        return et
    }

    private fun addSwitchRow(parent: LinearLayout, label: String): SwitchCompat {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }
        row.addView(
            TextView(this).apply {
                text = label
                setTextColor(MsColors.labelText)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        val sw = SwitchCompat(this).apply {
            isChecked = false
            // tint
            thumbTintList = android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(MsColors.white, MsColors.white)
            )
            trackTintList = android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(MsColors.primary, 0xFFCCCCCC.toInt())
            )
        }
        row.addView(sw)
        parent.addView(row)
        parent.addView(divider())
        return sw
    }

    private fun saveButton(onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = "保存"
            gravity = Gravity.CENTER
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setColor(MsColors.primary)
            }
            setPadding(0, dp(12), 0, dp(12))
            setOnClickListener { onClick() }
        }

    private fun fieldBg(): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(MsColors.fieldBg)
        }

    private fun divider(): View =
        View(this).apply {
            setBackgroundColor(MsColors.divider)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            )
        }

    private fun spacer(h: Int): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, h
            )
        }

    private fun bindFromConfig() {
        val c = MsSdk.getConfig()
        etInterval.setText(c.autoSendInterval.toInt().coerceAtLeast(1).toString())
        etAmount.setText(c.sendAmount)
        etCount.setText(c.packetCountText.ifBlank { c.packetCount.toString() })
        etGreeting.setText(c.greetingAmount)
        etPassword.setText(c.autoSendPassword)
        val sec = if (c.grabDelayMs <= 0) 0.0 else c.grabDelayMs / 1000.0
        etDelay.setText(if (sec == 0.0) "0" else trimNum(sec))
        swExclusive.isChecked = c.grabTargetedRedPacket
        swLucky.isChecked = c.grabNormalRedPacket
        swVoice.isChecked = c.redPacketReminder
    }

    private fun trimNum(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

    private fun saveSend() {
        val amountRaw = etAmount.text.toString().trim()
        val countRaw = etCount.text.toString().trim()
        val pwd = etPassword.text.toString().trim()
        val amount = amountRaw.split("/").map { it.trim() }.firstOrNull { it.isNotEmpty() }
        val count = countRaw.split("/").map { it.trim() }.firstOrNull { it.isNotEmpty() }?.toIntOrNull()
        when {
            amount.isNullOrBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> {
                MsToast.show(this, "请填写红包金额")
                return
            }
            count == null || count <= 0 -> {
                MsToast.show(this, "请填写红包个数")
                return
            }
            pwd.length != 6 || pwd.any { !it.isDigit() } -> {
                MsToast.show(this, "请填写6位数字支付密码")
                return
            }
        }
        val c = MsSdk.getConfig()
        c.masterSwitch = true
        c.sendSwitch = true
        c.autoSendInterval = etInterval.text.toString().trim().toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
        c.sendAmount = amountRaw
        c.packetCountText = countRaw
        c.syncPacketCountFromText()
        c.greetingAmount = etGreeting.text.toString().trim()
        c.setAutoSendPassword(pwd)
        c.synchronize()
        MsToast.show(this, "已保存")
    }

    private fun saveGrab() {
        val c = MsSdk.getConfig()
        c.masterSwitch = true
        c.secondSwitch = true
        val sec = etDelay.text.toString().trim().toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        c.grabDelayMs = (sec * 1000).toInt()
        c.grabTargetedRedPacket = swExclusive.isChecked
        c.grabNormalRedPacket = swLucky.isChecked
        c.redPacketReminder = swVoice.isChecked
        c.synchronize()
        if (c.redPacketReminder) {
            MsVoiceAlert.warmUp(this)
        }
        MsToast.show(this, "已保存")
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    companion object {
        const val EXTRA_TITLE = "settings_title"
    }
}
