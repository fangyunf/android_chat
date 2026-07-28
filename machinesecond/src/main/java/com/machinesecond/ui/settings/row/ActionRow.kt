package com.machinesecond.ui.settings.row

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.machinesecond.ui.settings.SettingsRow

/** 对齐 iOS：左标题 + 右白底金色按钮（文案取 value）。 */
object ActionRow {

    fun build(context: Context, row: SettingsRow, onClick: () -> Unit): View {
        val card = cardContainer(context, 62)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
        }
        root.addView(
            titleView(context, row.title, dp(context, 100)).apply {
                maxLines = 2
                gravity = Gravity.CENTER_VERTICAL
            }
        )
        val btnText = (row.value as? String)?.takeIf { it.isNotBlank() }
            ?: row.placeholder
            ?: row.title
        val btn = actionButton(context, btnText)
        btn.setOnClickListener { onClick() }
        root.addView(
            btn,
            LinearLayout.LayoutParams(0, dp(context, 38), 1f).apply {
                marginStart = dp(context, 8)
            }
        )
        card.addCentered(root)
        return card
    }
}
