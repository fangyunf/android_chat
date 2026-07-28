package com.machinesecond.ui.log

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.GrabLogStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GrabLogActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val timeFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar())
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setPadding(dp(8), dp(8), dp(8), dp(12))
            clipToPadding = false
        }
        container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(
            container,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        setContentView(root)
        reload()
    }

    private fun buildTopBar(): FrameLayout {
        val bar = FrameLayout(this).apply {
            minimumHeight = dp(44)
            setBackgroundColor(MsColors.pageBg)
        }
        val close = actionText("关闭") { finish() }
        val clear = actionText("清空") {
            AlertDialog.Builder(this)
                .setMessage("清空抢包日志？")
                .setPositiveButton("确定") { _, _ ->
                    GrabLogStore.clear(this)
                    reload()
                }
                .setNegativeButton("取消", null)
                .show()
        }
        val title = TextView(this).apply {
            text = "抢包日志"
            setTextColor(MsColors.black)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        bar.addView(close, FrameLayout.LayoutParams(dp(60), dp(44)).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            leftMargin = dp(12)
        })
        bar.addView(clear, FrameLayout.LayoutParams(dp(60), dp(44)).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            rightMargin = dp(12)
        })
        bar.addView(title, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dp(44)
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(72)
            rightMargin = dp(72)
        })
        return bar
    }

    private fun actionText(text: String, click: () -> Unit): TextView = TextView(this).apply {
        this.text = text
        setTextColor(MsColors.actionOrange)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        includeFontPadding = false
        gravity = Gravity.CENTER
        setOnClickListener { click() }
    }

    private fun reload() {
        container.removeAllViews()
        val logs = GrabLogStore.load(this)
        val successCount = logs.count { it.success }
        val failCount = logs.size - successCount
        container.addView(summaryCard("共 ${logs.size} 条抢包记录", "成功 $successCount 条，失败 $failCount 条"))
        if (logs.isEmpty()) {
            container.addView(emptyCard("暂无抢包记录", "抢到红包、转账或失败后会在这里显示记录。"))
            return
        }
        for (entry in logs) {
            container.addView(logRow(entry))
        }
    }

    private fun summaryCard(title: String, subtitle: String): TextView = TextView(this).apply {
        text = "$title\n$subtitle"
        setTextColor(MsColors.descText)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        includeFontPadding = false
        setLineSpacing(dp(4).toFloat(), 1f)
        setPadding(dp(12), dp(10), dp(12), dp(10))
        background = roundedBg(MsColors.darkRow, 12)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, dp(6)) }
    }

    private fun emptyCard(title: String, subtitle: String): TextView = TextView(this).apply {
        text = "$title\n$subtitle"
        gravity = Gravity.CENTER
        setTextColor(MsColors.descText)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        includeFontPadding = false
        setLineSpacing(dp(5).toFloat(), 1f)
        setPadding(dp(24), dp(70), dp(24), dp(70))
        background = roundedBg(MsColors.darkRow, 12)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun logRow(entry: GrabLogStore.Entry): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBg(MsColors.darkRow, 12)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dp(6)) }
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val statusText = if (entry.success) "成功" else "失败"
        val statusColor = if (entry.success) Color.parseColor("#68D391") else Color.parseColor("#FF7A7A")
        header.addView(TextView(this).apply {
            text = statusText
            setTextColor(MsColors.darkRow)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = roundedBg(statusColor, 6)
            setPadding(dp(8), dp(3), dp(8), dp(3))
        })
        header.addView(TextView(this).apply {
            text = timeFormat.format(Date(entry.time))
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            maxLines = 1
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(10)
        })
        card.addView(header)
        card.addView(TextView(this).apply {
            text = entry.message
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            includeFontPadding = false
            setLineSpacing(dp(4).toFloat(), 1f)
            setPadding(0, dp(8), 0, 0)
        })
        return card
    }

    private fun roundedBg(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
}
