package com.machinesecond.ui.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.settings.row.dp
import com.machinesecond.ui.theme.MsColors

/**
 * 深色主题输入弹窗，风格对齐设置页卡片（暗棕底 + 金色标题 + 白色输入框）。
 */
object MsInputDialog {

    fun show(
        context: Context,
        title: String,
        message: String? = null,
        hint: String = "",
        prefill: String = "",
        numeric: Boolean = false,
        onConfirm: (String) -> Unit
    ) {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(context, 14).toFloat()
            }
            setPadding(dp(context, 20), dp(context, 20), dp(context, 20), dp(context, 12))
        }
        root.addView(TextView(context).apply {
            text = title
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        if (!message.isNullOrBlank()) {
            root.addView(TextView(context).apply {
                text = message
                setTextColor(MsColors.descText)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(context, 8) }
            })
        }
        val field = EditText(context).apply {
            this.hint = hint
            setText(prefill)
            setSelection(text?.length ?: 0)
            isSingleLine = true
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = 0
            minimumHeight = 0
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            setTextColor(MsColors.fieldText)
            setHintTextColor(MsColors.fieldHint)
            setPadding(dp(context, 12), 0, dp(context, 12), 0)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER
            background = GradientDrawable().apply {
                setColor(MsColors.white)
                cornerRadius = dp(context, 8).toFloat()
            }
        }
        root.addView(
            field,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 44))
                .apply { topMargin = dp(context, 16) }
        )
        val cancel = dialogButton(context, "取消", filled = false)
        val confirm = dialogButton(context, "确定", filled = true)
        val bar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(context, 18) }
        }
        bar.addView(cancel, LinearLayout.LayoutParams(0, dp(context, 42), 1f).apply {
            marginEnd = dp(context, 6)
        })
        bar.addView(confirm, LinearLayout.LayoutParams(0, dp(context, 42), 1f).apply {
            marginStart = dp(context, 6)
        })
        root.addView(bar)

        val dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(
                root,
                ViewGroup.LayoutParams(
                    (context.resources.displayMetrics.widthPixels * 0.78f).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            window?.setBackgroundDrawable(GradientDrawable().apply { setColor(Color.TRANSPARENT) })
        }
        cancel.setOnClickListener { dialog.dismiss() }
        confirm.setOnClickListener {
            val value = field.text?.toString()?.trim() ?: ""
            dialog.dismiss()
            onConfirm(value)
        }
        dialog.show()
    }

    private fun dialogButton(context: Context, text: String, filled: Boolean): TextView =
        TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            isClickable = true
            includeFontPadding = false
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            setTextColor(if (filled) MsColors.white else MsColors.rowTitle)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = GradientDrawable().apply {
                cornerRadius = dp(context, 8).toFloat()
                if (filled) {
                    setColor(MsColors.themeGold)
                } else {
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(context, 1), MsColors.themeGold)
                }
            }
        }
}
