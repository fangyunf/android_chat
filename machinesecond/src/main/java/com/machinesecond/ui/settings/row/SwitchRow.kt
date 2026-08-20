package com.machinesecond.ui.settings.row

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.widget.MsSwitch

object SwitchRow {

    fun build(
        context: Context,
        row: SettingsRow,
        onChanged: (SettingsRow) -> Unit
    ): View {
        val card = cardContainer(context, 56)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        val left = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        left.addView(
            titleView(context, row.title, LinearLayout.LayoutParams.MATCH_PARENT)
        )
        subtitleView(context, row.placeholder)?.let { left.addView(it) }
        root.addView(
            left,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(context, 8)
            }
        )
        root.addView(
            MsSwitch.create(context, row.switchValue) { checked ->
                row.switchValue = checked
                onChanged(row)
            }
        )
        card.addCentered(root)
        return card
    }
}
