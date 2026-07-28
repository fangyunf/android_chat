package com.machinesecond.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.max

object MsToast {

    private var token = 0
    private var currentView: View? = null

    fun show(context: Context, message: String) {
        if (message.isBlank()) return
        MainHandler.post {
            val act = findActivity(context) ?: return@post
            showOnActivity(act, message)
        }
    }

    private fun showOnActivity(activity: Activity, message: String) {
        val decor = activity.window?.decorView as? ViewGroup ?: return
        currentView?.let { decor.removeView(it) }
        val myToken = ++token

        val dm = activity.resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels

        val tv = TextView(activity).apply {
            text = message
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            maxLines = 2
            gravity = Gravity.CENTER
            setPadding(dp(activity, 14), dp(activity, 8), dp(activity, 14), dp(activity, 8))
            background = GradientDrawable().apply {
                setColor(Color.argb((0.78f * 255).toInt(), 0, 0, 0))
                cornerRadius = dp(activity, 10).toFloat()
            }
        }

        tv.measure(
            View.MeasureSpec.makeMeasureSpec(screenW - dp(activity, 48), View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val textW = tv.measuredWidth
        val textH = tv.measuredHeight
        val w = minOf(screenW - dp(activity, 48), max(dp(activity, 120), textW + dp(activity, 28)))
        val h = max(dp(activity, 36), textH + dp(activity, 16))

        val lp = FrameLayout.LayoutParams(w, h).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = (screenH * 0.22f).toInt() - h / 2
        }

        decor.addView(tv, lp)
        currentView = tv
        tv.alpha = 0f

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 180
            fillAfter = true
        }
        tv.startAnimation(fadeIn)
        tv.animate().alpha(1f).setDuration(180).start()

        MainHandler.postDelayed(180 + 1600) {
            if (myToken != token) return@postDelayed
            val fadeOut = AlphaAnimation(1f, 0f).apply {
                duration = 250
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        if (myToken == token) {
                            decor.removeView(tv)
                            if (currentView === tv) currentView = null
                        }
                    }
                })
            }
            tv.startAnimation(fadeOut)
        }
    }

    private fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
}
