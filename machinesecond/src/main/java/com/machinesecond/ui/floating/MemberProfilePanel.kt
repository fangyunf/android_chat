package com.machinesecond.ui.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.machinesecond.MsSdk
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.DragClamp
import com.machinesecond.util.MsToast

@SuppressLint("ViewConstructor")
class MemberProfilePanel(
    context: Context,
    private val userId: String,
    private val displayName: String
) : FrameLayout(context) {

    private var grabSwitch: Switch? = null
    private var skipSwitch: Switch? = null

    init {
        val config = MsSdk.getConfig()
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(MsColors.massEntryBg)
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(8), 0, dp(8), 0)
        }
        val (grabRow, grabSw) = buildSwitchRow("只抢此人")
        grabSwitch = grabSw
        panel.addView(grabRow)
        val (skipRow, skipSw) = buildSwitchRow("不抢此人")
        skipSwitch = skipSw
        panel.addView(skipRow)
        setSwitchChecked(
            grabSwitch,
            config.grabSpecifiedUsersEnabled && userId in config.grabSpecifiedUserSet()
        )
        setSwitchChecked(
            skipSwitch,
            config.skipSpecifiedUsersEnabled && userId in config.skipSpecifiedUserSet()
        )
        wireGrabSwitch()
        wireSkipSwitch()
        val addBtn = TextView(context).apply {
            text = "添加到自选爆粉区"
            gravity = Gravity.CENTER
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            minHeight = dp(34)
            background = GradientDrawable().apply {
                setColor(MsColors.massEntryBg)
                cornerRadius = dp(4).toFloat()
            }
            setOnClickListener {
                if (!gate()) return@setOnClickListener
                if (userId.isBlank() || userId == MsSdk.getHost().currentUserId()) {
                    MsToast.show(context, if (userId.isBlank()) "用户无效" else "请勿添加自己")
                    return@setOnClickListener
                }
                MsSdk.getFanStore().appendCustom(userId, displayName)
                MsToast.show(context, "已保存到本地,请到自选人群爆粉列表查看")
            }
        }
        addView(panel, LayoutParams(dp(148), dp(72)).apply {
            leftMargin = resources.displayMetrics.widthPixels - dp(158)
            topMargin = dp(80)
        })
        addView(addBtn, LayoutParams(dp(148), dp(34)).apply {
            leftMargin = dp(16)
            topMargin = dp(164)
        })
        enableDrag(panel)
        enableDrag(addBtn)
    }

    private fun buildSwitchRow(title: String): Pair<LinearLayout, Switch> {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(36)
        }
        row.addView(TextView(context).apply {
            text = title
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val sw = Switch(context).apply {
            scaleX = 0.78f
            scaleY = 0.78f
        }
        row.addView(sw)
        return row to sw
    }

    private fun wireGrabSwitch() {
        grabSwitch?.setOnCheckedChangeListener { _, checked ->
            if (!gate()) {
                setSwitchChecked(grabSwitch, !checked)
                return@setOnCheckedChangeListener
            }
            if (userId == MsSdk.getHost().currentUserId()) {
                setSwitchChecked(grabSwitch, false)
                return@setOnCheckedChangeListener
            }
            val config = MsSdk.getConfig()
            if (checked) {
                config.addGrabSpecifiedUser(userId, displayName)
                setSwitchChecked(skipSwitch, false)
                MsToast.show(context, "已加入只抢此人")
            } else {
                config.removeGrabSpecifiedUser(userId)
            }
            config.synchronize()
        }
    }

    private fun wireSkipSwitch() {
        skipSwitch?.setOnCheckedChangeListener { _, checked ->
            if (!gate()) {
                setSwitchChecked(skipSwitch, !checked)
                return@setOnCheckedChangeListener
            }
            if (userId == MsSdk.getHost().currentUserId()) {
                setSwitchChecked(skipSwitch, false)
                return@setOnCheckedChangeListener
            }
            val config = MsSdk.getConfig()
            if (checked) {
                config.addSkipSpecifiedUser(userId, displayName)
                setSwitchChecked(grabSwitch, false)
                MsToast.show(context, "已加入不抢此人")
            } else {
                config.removeSkipSpecifiedUser(userId)
            }
            config.synchronize()
        }
    }

    private fun setSwitchChecked(sw: Switch?, checked: Boolean) {
        sw ?: return
        sw.setOnCheckedChangeListener(null)
        sw.isChecked = checked
        when (sw) {
            grabSwitch -> wireGrabSwitch()
            skipSwitch -> wireSkipSwitch()
        }
    }

    private fun gate(): Boolean {
        val config = MsSdk.getConfig()
        
        if (!config.masterSwitch) {
            MsToast.show(context, "请先打开总开关")
            return false
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: View) {
        var downX = 0f
        var downY = 0f
        var startLeft = 0
        var startTop = 0
        var moved = false
        view.isClickable = true
        view.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    moved = false
                    val lp = v.layoutParams as LayoutParams
                    startLeft = lp.leftMargin
                    startTop = lp.topMargin
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = e.rawX - downX
                    val deltaY = e.rawY - downY
                    if (!moved && kotlin.math.hypot(deltaX.toDouble(), deltaY.toDouble()) < dp(4)) {
                        return@setOnTouchListener false
                    }
                    moved = true
                    val parent = v.parent as? ViewGroup ?: return@setOnTouchListener false
                    val lp = v.layoutParams as LayoutParams
                    val (left, top) = DragClamp.clampMargins(
                        parent,
                        v,
                        (startLeft + deltaX).toInt(),
                        (startTop + deltaY).toInt()
                    )
                    lp.leftMargin = left
                    lp.topMargin = top
                    v.layoutParams = lp
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> moved
                else -> false
            }
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
}
