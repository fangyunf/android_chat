package com.machinesecond.ui.settings

enum class RowType {
    Switch,
    TextField,
    TextFieldWithSwitch,
    SegmentWithSwitch,
    Button,
    Action,
    Description,
    NumberPicker,
    CompensationGrid,
    DiscountGrid
}

data class SettingsRow(
    val key: String,
    val title: String,
    val type: RowType,
    var value: Any? = null,
    var placeholder: String? = null,
    var switchValue: Boolean = false,
    var segmentOptions: List<String> = emptyList(),
    var selectedIndex: Int = 0,
    var numberOptions: List<String> = emptyList(),
    var auxValue: String = "",
    var inputMode: InputMode = InputMode.Text
)

enum class InputMode {
    Text,
    Integer,
    Decimal
}
