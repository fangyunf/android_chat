package com.machinesecond.ui.settings.row

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.MsSdk
import com.machinesecond.ui.settings.SettingsRow
import com.machinesecond.ui.theme.MsColors

object CompensationGridRow {

    private val colHeaders = listOf("单雷", "双雷", "三雷", "四雷", "五雷")
    private val rowHeaders = listOf("5包", "6包", "7包", "8包", "9包", "10包", "11包")

    fun build(context: Context, row: SettingsRow, onChanged: (SettingsRow) -> Unit): View {
        val card = cardContainer(context, 316)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 6), dp(context, 6), dp(context, 6), dp(context, 6))
        }
        root.addView(TextView(context).apply {
            text = "九宫格"
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 15f))
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
        })
        val config = MsSdk.getConfig()
        val leftW = dp(context, 32)
        val cellH = dp(context, 32)

        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 22)
            ).apply { topMargin = dp(context, 6) }
        }
        headerRow.addView(TextView(context), LinearLayout.LayoutParams(leftW, dp(context, 22)))
        colHeaders.forEach { h ->
            headerRow.addView(TextView(context).apply {
                text = h
                setTextColor(MsColors.rowTitle)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 12f))
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                includeFontPadding = false
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(0, dp(context, 22), 1f).apply {
                marginStart = dp(context, 2)
                marginEnd = dp(context, 2)
            })
        }
        root.addView(headerRow)

        rowHeaders.forEachIndexed { ri, rh ->
            val line = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            line.addView(TextView(context).apply {
                text = rh
                setTextColor(MsColors.rowTitle)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 12f))
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                includeFontPadding = false
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(leftW, cellH))
            val pkt = ri + 5
            for (mi in 1..5) {
                val et = numericCell(context, "").apply {
                    val matrix = config.getCompensationMatrix()
                    setText(matrix["${pkt - 4}-$mi"] ?: matrix["${pkt}_$mi"] ?: "")
                    setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            config.setCompensationFactor(pkt, mi, text?.toString() ?: "")
                            onChanged(row)
                        }
                    }
                }
                line.addView(et, LinearLayout.LayoutParams(0, cellH, 1f).apply {
                    marginStart = dp(context, 2)
                    marginEnd = dp(context, 2)
                })
            }
            root.addView(
                line,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(context, 3) }
            )
        }
        card.addCentered(root)
        return card
    }
}
