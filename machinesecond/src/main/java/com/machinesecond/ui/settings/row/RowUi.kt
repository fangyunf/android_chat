package com.machinesecond.ui.settings.row

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.machinesecond.ui.theme.MsColors

internal fun dp(context: Context, v: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), context.resources.displayMetrics).toInt()

internal fun sp(context: Context, v: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, context.resources.displayMetrics)

/** 用 dp 定字号，避免系统大字体把设置页撑爆（对齐 iOS pt）。 */
internal fun textPx(context: Context, dpSize: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, context.resources.displayMetrics)

/**
 * 行卡片：内容整体垂直居中，最小高度保证不过矮。
 */
internal fun cardContainer(context: Context, minHeightDp: Int): FrameLayout =
    FrameLayout(context).apply {
        val bg = GradientDrawable().apply {
            setColor(MsColors.darkRow)
            cornerRadius = dp(context, 8).toFloat()
        }
        background = bg
        minimumHeight = dp(context, minHeightDp)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(dp(context, 8), dp(context, 5), dp(context, 8), dp(context, 5))
        layoutParams = lp
    }

/** 把行内容塞进卡片并垂直居中。 */
internal fun FrameLayout.addCentered(child: android.view.View) {
    addView(
        child,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER_VERTICAL }
    )
}

internal fun titleView(
    context: Context,
    text: String,
    width: Int = LinearLayout.LayoutParams.WRAP_CONTENT
): TextView =
    TextView(context).apply {
        this.text = text
        setTextColor(MsColors.rowTitle)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 14f))
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        includeFontPadding = false
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

internal fun subtitleView(context: Context, text: String?): TextView? {
    if (text.isNullOrBlank()) return null
    return TextView(context).apply {
        this.text = text
        setTextColor(MsColors.descText)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 11f))
        includeFontPadding = false
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
        setLineSpacing(dp(context, 2).toFloat(), 1f)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(context, 3) }
    }
}

/**
 * 九宫格 / 打折格里的数字输入格：只能输入数字与小数点，居中且文字过长自动缩字号。
 */
internal fun numericCell(context: Context, hintText: String): android.widget.EditText =
    android.widget.EditText(context).apply {
        hint = hintText
        gravity = Gravity.CENTER
        isSingleLine = true
        includeFontPadding = false
        minHeight = 0
        minimumHeight = 0
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(context, 2), 0, dp(context, 2), 0)
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789.")
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 13f))
        setTextColor(MsColors.fieldText)
        setHintTextColor(MsColors.fieldHint)
        background = whiteFieldBg(context)
        shrinkHintToFit(this, context, maxDp = 13f, minDp = 8f)
    }

internal fun whiteFieldBg(context: Context): GradientDrawable =
    GradientDrawable().apply {
        setColor(MsColors.white)
        cornerRadius = dp(context, 4).toFloat()
    }

internal fun styleField(et: android.widget.EditText, context: Context) {
    et.setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 13f))
    et.setHintTextColor(MsColors.fieldHint)
    et.setTextColor(MsColors.fieldText)
    et.includeFontPadding = false
    et.gravity = Gravity.CENTER_VERTICAL
    et.setPadding(dp(context, 8), dp(context, 4), dp(context, 8), dp(context, 4))
    et.minHeight = dp(context, 36)
    et.background = whiteFieldBg(context)
}

/**
 * 对齐 iOS 的 adjustsFontSizeToFitWidth：文字/占位过长时自动缩字号（不小于 minDp），保证一行显示完整。
 */
internal fun shrinkHintToFit(
    et: android.widget.EditText,
    context: Context,
    maxDp: Float = 13f,
    minDp: Float = 9f
) {
    fun fit() {
        val avail = et.width - et.paddingLeft - et.paddingRight
        if (avail <= 0) return
        val body = et.text?.toString().takeUnless { it.isNullOrEmpty() } ?: (et.hint?.toString() ?: "")
        if (body.isEmpty()) return
        val paint = android.text.TextPaint(et.paint)
        var size = maxDp
        while (size > minDp) {
            paint.textSize = textPx(context, size)
            if (paint.measureText(body) <= avail) break
            size -= 0.5f
        }
        val target = textPx(context, size)
        if (kotlin.math.abs(et.textSize - target) > 0.5f) {
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX, target)
        }
    }
    et.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> fit() }
    et.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
        override fun afterTextChanged(s: android.text.Editable?) = fit()
    })
}

internal fun actionButton(context: Context, text: String): TextView =
    TextView(context).apply {
        this.text = text
        gravity = Gravity.CENTER
        minHeight = dp(context, 36)
        setTextColor(MsColors.themeGold)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 13f))
        includeFontPadding = false
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        background = whiteFieldBg(context)
        setPadding(dp(context, 6), dp(context, 4), dp(context, 6), dp(context, 4))
    }

/** 标题宽：按文字测宽，夹在 72…min(140, 45%屏宽) —— 对齐 iOS。 */
internal fun titleWidthFor(context: Context, title: String): Int {
    val tv = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx(context, 14f))
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        this.text = title
    }
    tv.measure(0, 0)
    val screen = context.resources.displayMetrics.widthPixels
    val maxW = minOf(dp(context, 140), (screen * 0.42f).toInt())
    return tv.measuredWidth.coerceIn(dp(context, 72), maxW)
}
