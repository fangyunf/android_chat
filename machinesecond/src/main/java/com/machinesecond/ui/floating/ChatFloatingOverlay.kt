package com.machinesecond.ui.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.machinesecond.MsSdk
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.DragClamp
import com.machinesecond.util.MsToast
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ChatFloatingOverlay(
    context: Context,
    private val groupId: String
) : FrameLayout(context) {

    private val labels = listOf("总设置", "秒开关", "发包开关", "赔付开关", "爆粉", "隐")
    private val buttons = mutableListOf<TextView>()
    private var collapsed = false
    private var unifiedWidth = dp(72)
    private val btnHeight = dp(36)
    private val gap = dp(12)
    private val topPad = dp(56)

    init {
        clipChildren = false
        post { layoutButtons() }
        labels.forEachIndexed { index, label ->
            val tv = TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(MsColors.white)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                includeFontPadding = false
                setPadding(dp(8), 0, dp(8), 0)
                setOnClickListener { onButtonClick(index) }
                enableDrag(this)
            }
            buttons.add(tv)
            addView(tv)
        }
        refreshState()
    }

    private fun layoutButtons() {
        unifiedWidth = max(unifiedWidth, labels.maxOf { measureLabel(it) } + dp(20))
        val parentW = (parent as? View)?.width ?: resources.displayMetrics.widthPixels
        val x = parentW - unifiedWidth - dp(10)
        var y = topPad
        buttons.forEachIndexed { i, btn ->
            if (collapsed && i < buttons.size - 1) {
                btn.visibility = GONE
            } else {
                btn.visibility = VISIBLE
                btn.layoutParams = LayoutParams(unifiedWidth, btnHeight).apply {
                    leftMargin = x
                    topMargin = y
                }
                y += btnHeight + gap
            }
            styleButton(btn, i)
        }
    }

    private fun measureLabel(text: String): Int {
        val paint = android.graphics.Paint().apply { textSize = sp(13f) }
        return paint.measureText(text).toInt()
    }

    private fun styleButton(btn: TextView, index: Int) {
        val selected = isSelected(index)
        val bg = GradientDrawable().apply {
            cornerRadius = btnHeight / 2f
            if (selected) {
                setColor(MsColors.xingyanTheme)
                setStroke(dp(1), 0x66000000.toInt()) // black @ 40%, ~1.2pt≈1dp
            } else {
                setColor(MsColors.floatOff)
                setStroke(dp(1), 0x38000000.toInt()) // black @ 22%
            }
        }
        btn.background = bg
        btn.setTextColor(if (selected) MsColors.white else 0xE0FFFFFF.toInt())
        btn.alpha = 1f
    }

    private fun isSelected(index: Int): Boolean {
        val config = MsSdk.getConfig()
        val send = MsSdk.getSendEngine()
        return when (index) {
            0, 4 -> true
            1 -> config.isGrabArmed(groupId)
            2 -> send.autoSendActive
            3 -> config.isCompensationArmed(groupId)
            5 -> !collapsed
            else -> false
        }
    }

    fun refreshState() {
        layoutButtons()
    }

    private fun onButtonClick(index: Int) {
        when (index) {
            0 -> MsSdk.presentSettings(context)
            1 -> toggleSecond()
            2 -> toggleSend()
            3 -> toggleCompensation()
            4 -> tapFan()
            5 -> {
                collapsed = !collapsed
                refreshState()
            }
        }
    }

    private fun toggleSecond() {
        if (!ensureMasterAuthorized()) return
        if (!ensureGroupId()) return
        val config = MsSdk.getConfig()
        if (!config.secondSwitch) {
            config.secondSwitch = true
            config.synchronize()
        }
        val armed = config.toggleGrabSession(groupId)
        config.synchronize()
        DiagLogStore.append(context, "Float", "secondToggle group=$groupId armed=$armed")
        refreshState()
        if (config.isGrabArmed(groupId) && !anyGrabTypeEnabled()) {
            toast("已开启秒抢，请到「秒抢设置」打开「抢普通红包」等类型")
        }
    }

    private fun toggleSend() {
        if (!ensureMasterAuthorized()) return
        if (!ensureGroupId()) return
        val config = MsSdk.getConfig()
        if (!config.sendSwitch) {
            config.sendSwitch = true
            config.synchronize()
        }
        if (config.autoSendPassword.length != 6) {
            DiagLogStore.append(context, "Float", "send reject no password group=$groupId")
            toast("请先到「发包设置」设置支付密码")
            return
        }
        if (!MsSdk.getSendEngine().isSendConfigured()) {
            DiagLogStore.append(context, "Float", "send reject not configured group=$groupId")
            toast("请先到「发包设置」完善金额、个数等")
            return
        }
        DiagLogStore.append(context, "Float", "sendToggle group=$groupId")
        MsSdk.getSendEngine().toggleAutoSend(groupId)
        refreshState()
    }

    private fun toggleCompensation() {
        if (!ensureMasterAuthorized()) return
        if (!ensureGroupId()) return
        val config = MsSdk.getConfig()
        if (!config.compensationSwitch) {
            config.compensationSwitch = true
            config.synchronize()
        }
        if (config.compensationPassword.isEmpty()) {
            DiagLogStore.append(context, "Float", "comp reject no password group=$groupId")
            toast("请先到「赔付设置」设置赔付密码")
            return
        }
        val armed = config.toggleCompensationSession(groupId)
        config.synchronize()
        DiagLogStore.append(context, "Float", "compToggle group=$groupId armed=$armed autoComp=${config.autoCompensation}")
        refreshState()
        if (config.isCompensationArmed(groupId) && !config.autoCompensation) {
            toast("已开启赔付，请到「赔付设置」打开「自动赔付」")
        }
    }

    private fun tapFan() {
        if (!ensureMasterAuthorized()) return
        if (!ensureGroupId()) return
        MsSdk.saveNormalFanCrowd(groupId) { _, msg ->
            MsToast.show(context, msg)
        }
    }

    private fun ensureMasterAuthorized(): Boolean {
        val config = MsSdk.getConfig()
        if (!config.masterSwitch) {
            DiagLogStore.append(context, "Float", "reject master off")
            toast("请先打开总开关")
            return false
        }
        return true
    }

    private fun ensureGroupId(): Boolean {
        if (groupId.isNotBlank()) return true
        DiagLogStore.append(context, "Float", "reject missing groupId")
        return false
    }

    private fun anyGrabTypeEnabled(): Boolean {
        val config = MsSdk.getConfig()
        return config.grabNormalRedPacket ||
            config.grabTargetedRedPacket ||
            config.grabPrivateRedPacket ||
            config.autoReceiveTransfer
    }

    private fun toast(text: String) {
        if (text.isNotBlank()) MsToast.show(context, text)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: TextView) {
        var downX = 0f
        var downY = 0f
        var startLeft = 0
        var startTop = 0
        view.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    startLeft = (v.layoutParams as LayoutParams).leftMargin
                    startTop = (v.layoutParams as LayoutParams).topMargin
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val parent = v.parent as? ViewGroup ?: return@setOnTouchListener false
                    val lp = v.layoutParams as LayoutParams
                    val (left, top) = DragClamp.clampMargins(
                        parent, v,
                        (startLeft + (e.rawX - downX)).toInt(),
                        (startTop + (e.rawY - downY)).toInt()
                    )
                    lp.leftMargin = left
                    lp.topMargin = top
                    v.layoutParams = lp
                    true
                }
                else -> false
            }
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun sp(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}
