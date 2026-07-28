package com.machinesecond.ui.settings.row

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.ui.widget.MsSwitch

/**
 * 对齐 iOS：左列为「标题 / 分段」上下堆叠，右侧开关相对整行垂直居中。
 */
object SegmentWithSwitchRow {

    fun build(
        context: Context,
        row: SettingsRow,
        onChanged: (SettingsRow) -> Unit
    ): View {
        val card = cardContainer(context, 92)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 10), dp(context, 10))
        }
        val left = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        left.addView(
            titleView(context, row.title, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                maxLines = 2
            }
        )

        val scroll = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false }
        val segRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(MsColors.segmentBg)
                cornerRadius = dp(context, 6).toFloat()
            }
            setPadding(dp(context, 2), dp(context, 2), dp(context, 2), dp(context, 2))
        }
        val segButtons = mutableListOf<TextView>()
        fun paintSegments() {
            segButtons.forEachIndexed { index, btn ->
                val selected = index == row.selectedIndex
                if (selected) {
                    btn.setTextColor(MsColors.white)
                    btn.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    btn.background = GradientDrawable().apply {
                        setColor(MsColors.themeGold)
                        cornerRadius = dp(context, 4).toFloat()
                    }
                } else {
                    btn.setTextColor(0xEBEBEBEB.toInt())
                    btn.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    btn.background = null
                }
            }
        }
        row.segmentOptions.forEachIndexed { index, label ->
            val btn = TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                includeFontPadding = false
                minHeight = dp(context, 26)
                setPadding(dp(context, 8), 0, dp(context, 8), 0)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 12f))
                setOnClickListener {
                    row.selectedIndex = index
                    paintSegments()
                    onChanged(row)
                }
            }
            segButtons.add(btn)
            segRow.addView(
                btn,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(context, 26)
                ).apply { marginEnd = dp(context, 2) }
            )
        }
        paintSegments()
        scroll.addView(
            segRow,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        left.addView(
            scroll,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 32)).apply {
                topMargin = dp(context, 8)
            }
        )
        root.addView(
            left,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(context, 8)
                gravity = Gravity.CENTER_VERTICAL
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
