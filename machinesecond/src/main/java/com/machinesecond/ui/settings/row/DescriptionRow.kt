package com.machinesecond.ui.settings.row

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors

object DescriptionRow {

    fun build(context: Context, row: SettingsRow): View {
        val card = cardContainer(context, 96)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 12), dp(context, 12), dp(context, 12), dp(context, 12))
        }
        root.addView(titleView(context, row.title, LinearLayout.LayoutParams.MATCH_PARENT))
        root.addView(TextView(context).apply {
            text = row.value as? String ?: ""
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 12f))
            includeFontPadding = false
            setLineSpacing(dp(context, 3).toFloat(), 1f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(context, 6) }
        })
        card.addCentered(root)
        return card
    }
}
