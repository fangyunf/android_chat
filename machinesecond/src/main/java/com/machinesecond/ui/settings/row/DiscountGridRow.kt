package com.machinesecond.ui.settings.row

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.MsSdk
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors

/**
 * 对齐 iOS：6 行 × 2 组「A = B」，每格只能输入数字，宽度按权重等分。
 */
object DiscountGridRow {

    fun build(context: Context, row: SettingsRow, onChanged: (SettingsRow) -> Unit): View {
        val card = cardContainer(context, 250)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 6), dp(context, 5), dp(context, 6), dp(context, 5))
        }
        root.addView(TextView(context).apply {
            text = "打折"
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 15f))
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
            gravity = Gravity.CENTER
        })
        val config = MsSdk.getConfig()
        val cellH = dp(context, 30)

        for (r in 0 until 6) {
            val line = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            for (c in 0 until 2) {
                val index = r * 2 + c
                val (left, right) = config.getDiscountEntry(index)
                val group = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }
                lateinit var a: EditText
                lateinit var b: EditText
                a = numericCell(context, "A").apply {
                    setText(left)
                    setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            config.setDiscountEntry(
                                index,
                                text?.toString() ?: "",
                                b.text?.toString() ?: ""
                            )
                            onChanged(row)
                        }
                    }
                }
                b = numericCell(context, "B").apply {
                    setText(right)
                    setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            config.setDiscountEntry(
                                index,
                                a.text?.toString() ?: "",
                                text?.toString() ?: ""
                            )
                            onChanged(row)
                        }
                    }
                }
                val eq = TextView(context).apply {
                    text = "="
                    setTextColor(MsColors.rowTitle)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 14f))
                    setTypeface(typeface, Typeface.BOLD)
                    includeFontPadding = false
                    gravity = Gravity.CENTER
                }
                group.addView(a, LinearLayout.LayoutParams(0, cellH, 1f))
                group.addView(eq, LinearLayout.LayoutParams(dp(context, 18), cellH))
                group.addView(b, LinearLayout.LayoutParams(0, cellH, 1f))
                line.addView(
                    group,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = dp(context, 3)
                        marginEnd = dp(context, 3)
                    }
                )
            }
            root.addView(
                line,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(context, 2) }
            )
        }
        card.addCentered(root)
        return card
    }
}
