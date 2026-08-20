package com.machinesecond.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max

object MsToast {

    private var token = 0
    private var currentView: View? = null
    private var topActivity: Activity? = null
    private var lifecycleRegistered = false
    private var hideRunnable: Runnable? = null

    fun register(application: Application) {
        if (lifecycleRegistered) return
        lifecycleRegistered = true
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) {
                topActivity = activity
            }
            override fun onActivityResumed(activity: Activity) {
                topActivity = activity
            }
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) {
                if (topActivity === activity) topActivity = null
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) {
                if (topActivity === activity) topActivity = null
                // 页面销毁时清掉挂在该页上的 toast，避免第二次 add 失败
                val v = currentView
                if (v != null && v.context === activity) {
                    dismissCurrent()
                }
            }
        })
    }

    fun show(context: Context, message: String) {
        if (message.isBlank()) return
        MainHandler.post {
            val act = findActivity(context) ?: topActivity
            if (act != null && !act.isFinishing && !act.isDestroyed) {
                showOnActivity(act, message)
            } else {
                Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dismissCurrent() {
        hideRunnable?.let { MainHandler.removeCallbacks(it) }
        hideRunnable = null
        currentView?.let { old ->
            old.animate().cancel()
            (old.parent as? ViewGroup)?.removeView(old)
        }
        currentView = null
    }

    private fun showOnActivity(activity: Activity, message: String) {
        val decor = activity.window?.decorView as? ViewGroup ?: run {
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
            return
        }

        // 先清掉上一条，保证连续弹都能显示
        dismissCurrent()
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
            alpha = 0f
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
            topMargin = (screenH * 0.22f).toInt()
        }

        try {
            decor.addView(tv, lp)
        } catch (_: Exception) {
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT).show()
            return
        }
        currentView = tv
        tv.animate().alpha(1f).setDuration(160).start()

        val hide = Runnable {
            if (myToken != token || currentView !== tv) return@Runnable
            tv.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    if (myToken == token && currentView === tv) {
                        (tv.parent as? ViewGroup)?.removeView(tv)
                        if (currentView === tv) currentView = null
                    }
                }
                .start()
        }
        hideRunnable = hide
        MainHandler.postDelayed(1800, hide)
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
