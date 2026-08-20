package com.machinesecond.ui.settings

import android.content.Context
import android.view.View
import com.machinesecond.MsSdk
import com.machinesecond.ui.settings.row.ActionRow
import com.machinesecond.ui.settings.row.ButtonRow
import com.machinesecond.ui.settings.row.CompensationGridRow
import com.machinesecond.ui.settings.row.DescriptionRow
import com.machinesecond.ui.settings.row.DiscountGridRow
import com.machinesecond.ui.settings.row.NumberPickerRow
import com.machinesecond.ui.settings.row.SegmentWithSwitchRow
import com.machinesecond.ui.settings.row.SwitchRow
import com.machinesecond.ui.settings.row.TextFieldRow
import com.machinesecond.ui.settings.row.TextFieldWithSwitchRow

object SettingsRowInflater {

    fun inflate(
        context: Context,
        row: SettingsRow,
        onChanged: (SettingsRow) -> Unit,
        onAction: (SettingsRow) -> Unit,
        onAuth: () -> Unit,
        onUnauthorized: () -> Unit,
        onPickUsers: (SettingsRow, Boolean) -> Unit
    ): View {
        val gatedChanged: (SettingsRow) -> Unit = onChanged

        return when (row.type) {
            RowType.Switch -> SwitchRow.build(context, row, gatedChanged)
            RowType.TextField -> {
                val isPwd = row.key.contains("password", ignoreCase = true)
                TextFieldRow.build(
                    context = context,
                    row = row,
                    password = isPwd,
                    editable = true,
                    onChanged = if (isPwd) gatedChanged else onChanged,
                    onClick = null
                )
            }
            RowType.TextFieldWithSwitch -> {
                val picker = row.key == "grabSpecifiedUsers" || row.key == "skipSpecifiedUsers"
                TextFieldWithSwitchRow.build(
                    context = context,
                    row = row,
                    editable = !picker,
                    onChanged = gatedChanged,
                    // 对齐 iOS：选人页未授权也能点开，授权只拦开关/保存
                    onClick = if (picker) {
                        { onPickUsers(row, row.key == "grabSpecifiedUsers") }
                    } else null
                )
            }
            RowType.SegmentWithSwitch -> SegmentWithSwitchRow.build(context, row, gatedChanged)
            RowType.Button -> ButtonRow.build(context, row, onAuth)
            RowType.Action -> ActionRow.build(context, row) { onAction(row) }
            RowType.Description -> DescriptionRow.build(context, row)
            RowType.NumberPicker -> NumberPickerRow.build(context, row, gatedChanged)
            // 九宫格/打折写的是本地矩阵，不走授权拦截，否则编辑时会被反复重建
            RowType.CompensationGrid -> CompensationGridRow.build(context, row, onChanged)
            RowType.DiscountGrid -> DiscountGridRow.build(context, row, onChanged)
        }
    }
}
