package com.machinesecond.ui.theme

import android.graphics.Color

object MsColors {
    /** 设置页主色（橄榄绿，对齐截图） */
    val primary = Color.parseColor("#A4C66F")
    val primaryDark = Color.parseColor("#8FB055")
    val pageBg = Color.parseColor("#FFFFFF")
    val fieldBg = Color.parseColor("#F2F2F2")
    val fieldText = Color.parseColor("#333333")
    val fieldHint = Color.parseColor("#B0B0B0")
    val labelText = Color.parseColor("#333333")
    val tabInactive = Color.parseColor("#999999")
    val divider = Color.parseColor("#EEEEEE")
    val disclaimer = Color.parseColor("#C0C0C0")
    val floatText = Color.parseColor("#9B7BB8")
    val floatTextOn = Color.WHITE
    val floatBgOff = Color.WHITE
    /** 开启态背景（橄榄绿，与设置页主色一致） */
    val floatBgOn = primary
    val floatIconOff = Color.parseColor("#8A847A")
    val sendIcon = Color.parseColor("#E74C3C")
    val white = Color.WHITE
    val black = Color.BLACK

    // 兼容旧引用
    val darkRow = Color.parseColor("#291F1A")
    val rowTitle = Color.parseColor("#E6C794")
    val descText = Color.parseColor("#FAD180")
    val themeGold = Color.parseColor("#D1A673")
    val actionOrange = Color.parseColor("#FF8C33")
    val xingyanTheme = primary
    val segmentBg = Color.parseColor("#4D3D33")
    val floatOn = floatBgOn
    val floatOff = Color.argb((0.92f * 255).toInt(), 0x94, 0x90, 0x8A)
    val stopRed = Color.parseColor("#D94040")
    val massEntryBg = Color.argb((0.55f * 255).toInt(), 0, 0, 0)
    val configPageBg = Color.parseColor("#F5F5F7")
    val tabStripBg = primary
    val tabIconOffBg = Color.parseColor("#E8E4DC")
    val tabIconOffFg = Color.parseColor("#8A847A")
    val tabPayOffBg = Color.parseColor("#EDE8E1")
    val tabPayOffFg = Color.parseColor("#6B645C")
}
