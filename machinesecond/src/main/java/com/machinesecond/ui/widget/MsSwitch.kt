package com.machinesecond.ui.widget

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.machinesecond.R
import com.machinesecond.ui.settings.row.dp

/**
 * 对齐 iOS [MSUIHelper customSwitchButton] 的自定义开关图。
 * 素材比例 108:57，统一按 [widthDp] 换算高度，所有行保持同一尺寸。
 */
object MsSwitch {

    private const val ASPECT = 108f / 57f

    fun create(
        context: Context,
        checked: Boolean,
        widthDp: Int = 40,
        onChanged: (Boolean) -> Unit
    ): ImageView {
        var isOn = checked
        val w = dp(context, widthDp)
        val h = (w / ASPECT).toInt()
        return ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(w, h).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            scaleType = ImageView.ScaleType.FIT_XY
            adjustViewBounds = false
            setImageResource(if (isOn) R.drawable.ms_icn_second_on else R.drawable.ms_icn_second_off)
            contentDescription = if (isOn) "on" else "off"
            isClickable = true
            isFocusable = true
            setOnClickListener {
                isOn = !isOn
                setImageResource(if (isOn) R.drawable.ms_icn_second_on else R.drawable.ms_icn_second_off)
                contentDescription = if (isOn) "on" else "off"
                onChanged(isOn)
            }
        }
    }
}
