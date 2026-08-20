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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max

object MsToast {

    private var token = 0
    private var currentView: View? = null
    private var topActivity: Activity? = null
    private var lifecycleRegistered = false

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
                showSystemTopToast(context.applicationContext, message)
            }
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
            topMargin = (screenH * 0.22f).toInt()
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

    private fun showSystemTopToast(context: Context, message: String) {
        val tv = TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            maxLines = 2
            gravity = Gravity.CENTER
            setPadding(dp(context, 14), dp(context, 8), dp(context, 14), dp(context, 8))
            background = GradientDrawable().apply {
                setColor(Color.argb((0.78f * 255).toInt(), 0, 0, 0))
                cornerRadius = dp(context, 10).toFloat()
            }
        }
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = tv
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, (context.resources.displayMetrics.heightPixels * 0.22f).toInt())
            show()
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
