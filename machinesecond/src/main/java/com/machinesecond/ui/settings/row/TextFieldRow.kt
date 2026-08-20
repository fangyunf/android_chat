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

object TextFieldRow {

    fun build(
        context: Context,
        row: SettingsRow,
        password: Boolean = false,
        editable: Boolean = true,
        onChanged: (SettingsRow) -> Unit,
        onClick: (() -> Unit)? = null
    ): View {
        val card = cardContainer(context, 60)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        root.addView(titleView(context, row.title, titleWidthFor(context, row.title)).apply {
            gravity = Gravity.CENTER_VERTICAL
        })
        val et = EditText(context).apply {
            setText(row.value as? String ?: "")
            hint = row.placeholder
            styleField(this, context)
            isSingleLine = true
            isEnabled = editable
            isFocusable = editable
            isFocusableInTouchMode = editable
            if (password) {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            } else {
                applyInputMode(row.inputMode)
            }
            if (!editable) {
                isClickable = true
                setOnClickListener { onClick?.invoke() }
            } else {
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        row.value = text?.toString() ?: ""
                        onChanged(row)
                    }
                }
            }
        }
        root.addView(
            et,
            LinearLayout.LayoutParams(0, dp(context, 36), 1f).apply {
                marginStart = dp(context, 8)
            }
        )
        card.addCentered(root)
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
