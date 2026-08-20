package com.machinesecond.ui.settings.row

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.text.method.DigitsKeyListener
import com.machinesecond.ui.settings.InputMode
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.widget.MsSwitch

/**
 * 对齐 iOS：title(120) | textField(placeholder 自动缩字号) | switch，单行水平布局。
 */
object TextFieldWithSwitchRow {

    fun build(
        context: Context,
        row: SettingsRow,
        editable: Boolean = true,
        onChanged: (SettingsRow) -> Unit,
        onClick: (() -> Unit)? = null
    ): View {
        val card = cardContainer(context, 60)
        val line = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 10), dp(context, 10))
        }
        line.addView(
            titleView(context, row.title, dp(context, 108)).apply {
                maxLines = 2
                gravity = Gravity.CENTER_VERTICAL
            }
        )

        val et = EditText(context).apply {
            setText(row.value as? String ?: "")
            hint = row.placeholder
            styleField(this, context)
            isSingleLine = true
            shrinkHintToFit(this, context)
            if (!editable) {
                // 保持 enabled 才能收到点击（对齐 iOS：点输入框直接进选人页）
                isFocusable = false
                isFocusableInTouchMode = false
                isCursorVisible = false
                inputType = InputType.TYPE_NULL
                keyListener = null
                isClickable = true
                setOnClickListener { onClick?.invoke() }
            } else {
                applyInputMode(row.inputMode)
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        row.value = text?.toString() ?: ""
                        onChanged(row)
                    }
                }
            }
        }
        line.addView(
            et,
            LinearLayout.LayoutParams(0, dp(context, 36), 1f).apply {
                marginStart = dp(context, 6)
                marginEnd = dp(context, 8)
            }
        )
        line.addView(
            MsSwitch.create(context, row.switchValue) { checked ->
                row.switchValue = checked
                onChanged(row)
            }
        )
        card.addCentered(line)
        return card
    }

    private fun EditText.applyInputMode(mode: InputMode) {
        when (mode) {
            InputMode.Integer -> {
                inputType = InputType.TYPE_CLASS_NUMBER
                keyListener = DigitsKeyListener.getInstance("0123456789")
            }
            InputMode.Decimal -> {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                keyListener = DigitsKeyListener.getInstance("0123456789.")
            }
            InputMode.Text -> inputType = InputType.TYPE_CLASS_TEXT
        }
    }
}
