package com.machinesecond.ui.settings.row

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors

object NumberPickerRow {

    fun build(context: Context, row: SettingsRow, onChanged: (SettingsRow) -> Unit): View {
        val card = cardContainer(context, 96)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        root.addView(titleView(context, row.title, LinearLayout.LayoutParams.MATCH_PARENT).apply {
            maxLines = 2
        })
        // 选项等分整行宽度，7 个数字在窄屏也能全部显示，无需横向滚动
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(MsColors.segmentBg)
                cornerRadius = dp(context, 6).toFloat()
            }
            setPadding(dp(context, 3), dp(context, 3), dp(context, 3), dp(context, 3))
        }
        val chips = mutableListOf<TextView>()
        fun paint() {
            val selected = row.value as? String ?: ""
            chips.forEach { btn ->
                val isSel = btn.text.toString() == selected
                btn.background = GradientDrawable().apply {
                    cornerRadius = dp(context, 4).toFloat()
                    setColor(if (isSel) MsColors.themeGold else MsColors.white)
                }
                btn.setTextColor(MsColors.black)
                btn.setTypeface(null, if (isSel) Typeface.BOLD else Typeface.NORMAL)
            }
        }
        row.numberOptions.forEach { opt ->
            val btn = TextView(context).apply {
                text = opt
                gravity = Gravity.CENTER
                includeFontPadding = false
                maxLines = 1
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 13f))
                setOnClickListener {
                    row.value = opt
                    paint()
                    onChanged(row)
                }
            }
            chips.add(btn)
            rowLayout.addView(
                btn,
                LinearLayout.LayoutParams(0, dp(context, 30), 1f).apply {
                    marginStart = dp(context, 2)
                    marginEnd = dp(context, 2)
                }
            )
        }
        paint()
        root.addView(
            rowLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(context, 8) }
        )
        card.addCentered(root)
        return card
    }
}
