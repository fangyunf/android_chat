package com.machinesecond.util

import android.view.View
import android.view.ViewGroup

/** 拖动夹紧到父视图可视区域（含 safeArea 由父布局自行扣 inset）。 */
object DragClamp {
    fun clampMargins(parent: ViewGroup, child: View, left: Int, top: Int): Pair<Int, Int> {
        val maxLeft = (parent.width - child.width).coerceAtLeast(0)
        val maxTop = (parent.height - child.height).coerceAtLeast(0)
        return left.coerceIn(0, maxLeft) to top.coerceIn(0, maxTop)
    }
}
