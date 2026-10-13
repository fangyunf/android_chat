package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView


/**
 * 激活 / 未授权提示弹窗（纯代码，不依赖库主题资源）。
 * 对齐 QianBao / iOS：标题「提示」、说明「请输入激活码」、hint「请输入...」。
 */

private object AuthColors {
    const val FIELD_TEXT = 0xFF222222.toInt()
    const val FIELD_HINT = 0xFF999999.toInt()
    const val STOP_RED = 0xFFE54545.toInt()
    const val ACTION_ORANGE = 0xFFFF8A00.toInt()
    const val THEME_GOLD = 0xFF58BE6B.toInt()
}

object AuthDialog {

    fun showUnauthorized(activity: Activity, onConfirm: () -> Unit) {
        val dialog = baseDialog(activity)
        val card = cardRoot(activity)
        card.addView(titleView(activity, "提示"))
        card.addView(
            bodyView(activity, "暂无授权，不可使用").apply {
                (layoutParams as LinearLayout.LayoutParams).topMargin = dp(activity, 10)
                (layoutParams as LinearLayout.LayoutParams).bottomMargin = dp(activity, 18)
            }
        )
        card.addView(
            dualButtons(
                activity = activity,
                negative = "取消" to { dialog.dismiss() },
                positive = "去授权" to {
                    dialog.dismiss()
                    onConfirm()
                }
            )
        )
        dialog.setContentView(wrapDim(activity, card))
        dialog.show()
    }

    fun showActivate(
        activity: Activity,
        initialCode: String = "",
        onSubmit: (code: String, finish: (ok: Boolean, message: String?) -> Unit) -> Unit
    ) {
        val dialog = baseDialog(activity)
        val card = cardRoot(activity)

        card.addView(titleView(activity, "提示"))
        card.addView(
            bodyView(activity, "请输入激活码").apply {
                (layoutParams as LinearLayout.LayoutParams).topMargin = dp(activity, 8)
            }
        )

        val seeded = initialCode.trim()
        val input = EditText(activity).apply {
            hint = "请输入..."
            setText(seeded)
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine()
            setTextColor(AuthColors.FIELD_TEXT)
            setHintTextColor(AuthColors.FIELD_HINT)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12))
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 10).toFloat()
                setColor(Color.parseColor("#F2F7F8"))
                setStroke(dp(activity, 1), Color.parseColor("#D0E0E2"))
            }
            if (seeded.isNotEmpty()) setSelection(seeded.length)
            // 粘贴带左右空格时立刻 trim 回写到输入框
            var trimming = false
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    if (trimming || s == null) return
                    val cur = s.toString()
                    val t = cur.trim()
                    if (t == cur) return
                    trimming = true
                    s.replace(0, s.length, t)
                    trimming = false
                }
            })
        }
        card.addView(
            input,
            LinearLayout.LayoutParams(MATCH, WRAP).apply {
                topMargin = dp(activity, 14)
                bottomMargin = dp(activity, 6)
            }
        )

        val error = TextView(activity).apply {
            visibility = View.GONE
            setTextColor(AuthColors.STOP_RED)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            gravity = Gravity.START
        }
        card.addView(
            error,
            LinearLayout.LayoutParams(MATCH, WRAP).apply {
                bottomMargin = dp(activity, 10)
            }
        )

        val progress = ProgressBar(activity).apply {
            visibility = View.GONE
            isIndeterminate = true
        }
        card.addView(
            progress,
            LinearLayout.LayoutParams(dp(activity, 28), dp(activity, 28)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(activity, 8)
            }
        )

        lateinit var buttons: View
        fun setBusy(busy: Boolean) {
            progress.visibility = if (busy) View.VISIBLE else View.GONE
            input.isEnabled = !busy
            buttons.isEnabled = !busy
            buttons.alpha = if (busy) 0.55f else 1f
        }

        fun submit() {
            val code = input.text?.toString()?.trim().orEmpty()
            if (code.isEmpty()) {
                error.text = "授权码不能为空"
                error.visibility = View.VISIBLE
                return
            }
            error.visibility = View.GONE
            setBusy(true)
            onSubmit(code) { ok, message ->
                activity.runOnUiThread {
                    setBusy(false)
                    if (ok) {
                        dialog.dismiss()
                    } else {
                        error.text = message?.takeIf { it.isNotBlank() } ?: "授权失败"
                        error.visibility = View.VISIBLE
                    }
                }
            }
        }

        buttons = dualButtons(
            activity = activity,
            negative = "取消" to { dialog.dismiss() },
            positive = "确定" to { submit() }
        )
        card.addView(buttons)

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else false
        }

        dialog.setContentView(wrapDim(activity, card))
        dialog.setOnShowListener { input.requestFocus() }
        dialog.show()
    }

    private fun baseDialog(activity: Activity): Dialog =
        Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(WRAP, WRAP)
            window?.setGravity(Gravity.CENTER)
        }

    private fun wrapDim(activity: Activity, card: View): FrameLayout {
        val cardW = minOf(
            dp(activity, 280),
            (activity.resources.displayMetrics.widthPixels * 0.82f).toInt()
        )
        return FrameLayout(activity).apply {
            setPadding(dp(activity, 16), dp(activity, 24), dp(activity, 16), dp(activity, 24))
            addView(card, FrameLayout.LayoutParams(cardW, WRAP, Gravity.CENTER))
        }
    }

    private fun cardRoot(activity: Activity): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 22), dp(activity, 20), dp(activity, 18))
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat()
                setColor(Color.WHITE)
            }
            elevation = dp(activity, 8).toFloat()
        }

    private fun titleView(activity: Activity, text: String): TextView =
        TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(AuthColors.FIELD_TEXT)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }

    private fun bodyView(activity: Activity, text: String): TextView =
        TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#5A6B70"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }

    private fun dualButtons(
        activity: Activity,
        negative: Pair<String, () -> Unit>,
        positive: Pair<String, () -> Unit>
    ): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply {
                topMargin = dp(activity, 4)
            }
            addView(
                pillButton(activity, negative.first, outline = true, click = negative.second),
                LinearLayout.LayoutParams(0, dp(activity, 42), 1f).apply {
                    marginEnd = dp(activity, 8)
                }
            )
            addView(
                pillButton(activity, positive.first, outline = false, click = positive.second),
                LinearLayout.LayoutParams(0, dp(activity, 42), 1f).apply {
                    marginStart = dp(activity, 8)
                }
            )
        }

    private fun pillButton(
        activity: Activity,
        label: String,
        outline: Boolean,
        click: () -> Unit
    ): TextView =
        TextView(activity).apply {
            text = label
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(if (outline) AuthColors.ACTION_ORANGE else Color.WHITE)
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 21).toFloat()
                if (outline) {
                    setColor(Color.parseColor("#E8F6F4"))
                    setStroke(dp(activity, 1), AuthColors.THEME_GOLD)
                } else {
                    setColor(AuthColors.THEME_GOLD)
                }
            }
            setOnClickListener { click() }
        }

    private fun dp(activity: Activity, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            activity.resources.displayMetrics
        ).toInt()

    private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
}
