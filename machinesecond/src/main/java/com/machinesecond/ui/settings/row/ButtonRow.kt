package com.machinesecond.ui.settings.row

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors

object ButtonRow {

    fun build(context: Context, row: SettingsRow, onClick: () -> Unit): View {
        val card = cardContainer(context, 62)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        root.addView(titleView(context, row.title, dp(context, 72)).apply {
            gravity = Gravity.CENTER_VERTICAL
        })
        val display = (row.value as? String)?.takeIf { it.isNotBlank() }
            ?: row.placeholder ?: "请授权"
        val action = TextView(context).apply {
            text = display
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setTextColor(MsColors.fieldText)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 13f))
            includeFontPadding = false
            background = whiteFieldBg(context)
            setPadding(dp(context, 8), dp(context, 4), dp(context, 8), dp(context, 4))
            minHeight = dp(context, 32)
            setOnClickListener { onClick() }
        }
        root.addView(
            action,
            LinearLayout.LayoutParams(0, dp(context, 38), 1f).apply {
                marginStart = dp(context, 8)
            }
        )
        card.addCentered(root)
        return card
    }
}
