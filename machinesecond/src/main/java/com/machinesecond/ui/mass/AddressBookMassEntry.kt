package com.machinesecond.ui.mass

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.machinesecond.util.DragClamp
import com.machinesecond.util.MsToast

/**
 * 对齐 iOS MSAddressBookMassEntryView：通讯录「设置群发信息 / 群发」浮动入口。
 * 空白区域点击穿透，按钮可独立拖动。
 */
@SuppressLint("ViewConstructor")
class AddressBookMassEntry(context: Context) : FrameLayout(context) {

    init {
        setWillNotDraw(true)
        isClickable = false
        isFocusable = false

        val configBtn = capsule("设置群发信息", dp(110)).apply {
            setOnClickListener {
                if (!gate()) return@setOnClickListener
                val intent = Intent(context, MassMessageConfigActivity::class.java)
                if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            enableDrag(this)
        }
        val sendBtn = capsule("群发", dp(56)).apply {
            setOnClickListener {
                if (!gate()) return@setOnClickListener
                MsSdk.getMassSender().start()
            }
            enableDrag(this)
        }

        addView(configBtn, LayoutParams(LayoutParams.WRAP_CONTENT, dp(36)).apply {
            gravity = Gravity.END or Gravity.TOP
            topMargin = (resources.displayMetrics.heightPixels * 0.35f).toInt()
            marginEnd = dp(16)
        })
        addView(sendBtn, LayoutParams(LayoutParams.WRAP_CONTENT, dp(36)).apply {
            gravity = Gravity.END or Gravity.TOP
            topMargin = (resources.displayMetrics.heightPixels * 0.35f).toInt() + dp(48)
            marginEnd = dp(16)
        })
    }

    /** 空白不拦截，仅子按钮可点（对齐 iOS hitTest）。 */
    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = false

    private fun capsule(text: String, minW: Int): TextView =
        TextView(context).apply {
            this.text = text
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            gravity = Gravity.CENTER
            minWidth = minW
            minHeight = dp(32)
            includeFontPadding = false
            setPadding(dp(8), dp(6), dp(8), dp(6))
            background = GradientDrawable().apply {
                setColor(MsColors.massEntryBg)
                cornerRadius = dp(4).toFloat()
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
    private fun enableDrag(view: TextView) {
        var downX = 0f
        var downY = 0f
        var startLeft = 0
        var startTop = 0
        var moved = false
        view.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    val lp = v.layoutParams as LayoutParams
                    // 首次拖动前把 gravity 布局转成绝对 margin
                    if (lp.gravity != Gravity.NO_GRAVITY) {
                        lp.leftMargin = v.left
                        lp.topMargin = v.top
                        lp.gravity = Gravity.NO_GRAVITY
                        v.layoutParams = lp
                    }
                    startLeft = lp.leftMargin
                    startTop = lp.topMargin
                    moved = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val parent = v.parent as? ViewGroup ?: return@setOnTouchListener false
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (!moved && dx * dx + dy * dy < dp(4) * dp(4)) {
                        return@setOnTouchListener false
                    }
                    moved = true
                    val lp = v.layoutParams as LayoutParams
                    val (left, top) = DragClamp.clampMargins(
                        parent, v,
                        (startLeft + dx).toInt(),
                        (startTop + dy).toInt()
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
