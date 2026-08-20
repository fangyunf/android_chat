package com.machinesecond.ui.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.machinesecond.MsSdk
import com.machinesecond.R
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MsToast

/**
 * 聊天浮钮：专 / 拼 / 发包 / 设置 / 收起（对齐截图圆形白底样式）
 */
@SuppressLint("ViewConstructor")
class ChatFloatingOverlay(
    context: Context,
    private val groupId: String
) : FrameLayout(context) {

    private companion object {
        const val IDX_ZHUAN = 0
        const val IDX_PIN = 1
        const val IDX_SEND = 2
        const val IDX_SETTINGS = 3
        const val IDX_CLOSE = 4
    }

    private val btnSize = dp(48)
    private val gap = dp(14)
    private val topPad = dp(72)
    private val buttons = mutableListOf<View>()
    private var collapsed = false
    private var stackLeft = 0
    private var stackTop = topPad

    init {
        clipChildren = false
        clipToPadding = false
        buttons += makeTextBtn("专") { toggleZhuan() }
        buttons += makeTextBtn("拼") { togglePin() }
        buttons += makeIconBtn(R.drawable.ms_ic_send) { toggleSend() }
        buttons += makeIconBtn(R.drawable.ms_ic_gear) { MsSdk.presentSettings(context) }
        buttons += makeIconBtn(R.drawable.ms_ic_close) {
            collapsed = !collapsed
            refreshState()
        }
        buttons.forEach { addView(it) }
        post {
            val parentW = (parent as? View)?.width ?: resources.displayMetrics.widthPixels
            stackLeft = parentW - btnSize - dp(12)
            stackTop = topPad
            refreshState()
        }
    }

    private fun makeTextBtn(label: String, click: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(MsColors.floatText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            background = circleBg(false)
            elevation = dp(4).toFloat()
            setOnClickListener { click() }
            enableDrag(this)
        }
    }

    private fun makeIconBtn(res: Int, click: () -> Unit): ImageView {
        return ImageView(context).apply {
            setImageResource(res)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = circleBg(false)
            elevation = dp(4).toFloat()
            setOnClickListener { click() }
            enableDrag(this)
        }
    }

    private fun circleBg(on: Boolean): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (on) MsColors.floatBgOn else MsColors.floatBgOff)
            setStroke(dp(1), if (on) 0x33000000 else 0x14000000)
        }

    private fun applyToggleStyle(btn: View, on: Boolean, sendIcon: Boolean = false) {
        btn.background = circleBg(on)
        btn.alpha = 1f
        when (btn) {
            is TextView -> btn.setTextColor(if (on) MsColors.floatTextOn else MsColors.floatText)
            is ImageView -> {
                val tint = when {
                    on -> MsColors.white
                    sendIcon -> MsColors.sendIcon
                    else -> MsColors.floatIconOff
                }
                btn.setColorFilter(tint, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    fun refreshState() {
        val config = MsSdk.getConfig()
        val sendOn = MsSdk.getSendEngine().autoSendActive
        val zhuanOn = config.isGrabArmed(groupId) && config.grabTargetedRedPacket
        val pinOn = config.isGrabArmed(groupId) && config.grabNormalRedPacket

        var y = stackTop
        buttons.forEachIndexed { i, btn ->
            val hide = collapsed && i != IDX_CLOSE
            btn.visibility = if (hide) GONE else VISIBLE
            if (!hide) {
                btn.layoutParams = LayoutParams(btnSize, btnSize).apply {
                    leftMargin = stackLeft
                    topMargin = y
                }
                y += btnSize + gap
            }
            when (i) {
                IDX_ZHUAN -> applyToggleStyle(btn, zhuanOn)
                IDX_PIN -> applyToggleStyle(btn, pinOn)
                IDX_SEND -> applyToggleStyle(btn, sendOn, sendIcon = true)
                IDX_SETTINGS, IDX_CLOSE -> applyToggleStyle(btn, on = false)
            }
        }
    }

    private fun ensureReady(): Boolean {
        val config = MsSdk.getConfig()
        if (!config.masterSwitch) {
            config.masterSwitch = true
            config.synchronize()
        }
        if (groupId.isBlank()) {
            toast("群会话无效")
            return false
        }
        return true
    }

    private fun toggleZhuan() {
        if (!ensureReady()) return
        val config = MsSdk.getConfig()
        config.secondSwitch = true
        val turningOn = !(config.isGrabArmed(groupId) && config.grabTargetedRedPacket)
        config.grabTargetedRedPacket = turningOn
        if (turningOn) {
            if (!config.isGrabArmed(groupId)) config.toggleGrabSession(groupId)
        } else if (!config.grabNormalRedPacket && config.isGrabArmed(groupId)) {
            config.toggleGrabSession(groupId)
        }
        config.synchronize()
        DiagLogStore.append(context, "Float", "zhuan=$turningOn group=$groupId")
        refreshState()
        toast(if (turningOn) "已开启专属秒抢" else "已关闭专属秒抢")
    }

    private fun togglePin() {
        if (!ensureReady()) return
        val config = MsSdk.getConfig()
        config.secondSwitch = true
        val turningOn = !(config.isGrabArmed(groupId) && config.grabNormalRedPacket)
        config.grabNormalRedPacket = turningOn
        if (turningOn) {
            if (!config.isGrabArmed(groupId)) config.toggleGrabSession(groupId)
        } else if (!config.grabTargetedRedPacket && config.isGrabArmed(groupId)) {
            config.toggleGrabSession(groupId)
        }
        config.synchronize()
        DiagLogStore.append(context, "Float", "pin=$turningOn group=$groupId")
        refreshState()
        toast(if (turningOn) "已开启拼手气秒抢" else "已关闭拼手气秒抢")
    }

    private fun toggleSend() {
        if (!ensureReady()) return
        val engine = MsSdk.getSendEngine()
        if (engine.autoSendActive) {
            engine.toggleAutoSend(groupId)
            refreshState()
            toast("已关闭自动发包")
            return
        }
        val config = MsSdk.getConfig()
        val err = sendConfigError(config) ?: run {
            if (!engine.isSendConfigured()) "请先到设置完善红包金额、个数等" else null
        }
        if (err != null) {
            toast(err)
            return
        }
        config.sendSwitch = true
        config.synchronize()
        engine.toggleAutoSend(groupId)
        refreshState()
        toast("已开启自动发包")
    }

    private fun sendConfigError(config: com.machinesecond.config.MsConfig): String? {
        val amount = config.sendAmount.split("/").map { it.trim() }.firstOrNull { it.isNotEmpty() }
        if (amount.isNullOrBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            return "请先到设置填写红包金额"
        }
        val countRaw = config.packetCountText.trim()
        val count = countRaw.split("/").map { it.trim() }.firstOrNull { it.isNotEmpty() }?.toIntOrNull()
        if (count == null || count <= 0) {
            return "请先到设置填写红包个数"
        }
        if (config.autoSendPassword.length != 6) {
            return "请先到设置填写6位支付密码"
        }
        return null
    }

    private fun toast(text: String) {
        if (text.isNotBlank()) MsToast.show(context, text)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: View) {
        var downX = 0f
        var downY = 0f
        var startStackLeft = 0
        var startStackTop = 0
        var moved = false
        view.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startStackLeft = stackLeft
                    startStackTop = stackTop
                    moved = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val parent = parent as? ViewGroup ?: return@setOnTouchListener false
                    val dx = (e.rawX - downX).toInt()
                    val dy = (e.rawY - downY).toInt()
                    if (!moved && (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8)) {
                        moved = true
                    }
                    if (!moved) return@setOnTouchListener false
                    val maxL = (parent.width - btnSize).coerceAtLeast(0)
                    val maxT = (parent.height - btnSize).coerceAtLeast(0)
                    stackLeft = (startStackLeft + dx).coerceIn(0, maxL)
                    stackTop = (startStackTop + dy).coerceIn(0, maxT)
                    refreshState()
                    true
                }
                else -> false
            }
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
}
