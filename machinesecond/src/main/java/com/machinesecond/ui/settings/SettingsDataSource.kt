package com.machinesecond.ui.settings

import com.machinesecond.config.MsConfig

object SettingsDataSource {

    private val mineSegments = listOf("单雷", "双雷", "三雷", "四雷", "五雷")
    private val packetOptions = (5..11).map { it.toString() }

    private const val IOS14_TIP =
        "①设置-隐私-跟踪-允许APP请求跟踪-打开\n" +
                "②设置-隐私-拉到最下方-个性化广告-关闭\n" +
                "2⃣️14以下系统关闭限制广告追踪"

    private const val BASIC_DESC =
        "1.注意:本插件只做研究之用，请于下载测试完毕后删除，如有非法使用，后果自负!\n" +
                "2.任何功能必须在使用时打开总开关\n" +
                "3.红包提醒打开后有语音播报和文字提醒\n" +
                "4.后台抢包打开后可后台进行工作"

    private const val GRAB_DESC =
        "1.注意；本插件只做研究之用，请于下载测试完毕后删除，如有非法使用，后果自负！\n" +
                "2.只抢几雷或以上（比如选择2择会抢2345雷）\n" +
                "3.固定抢几雷-1-2-3-4-5（选择2就是只抢2雷）\n" +
                "4.尾包相关开关对齐星衍：仅设置项，收包链未接线（开了也不特抢/特跳）\n" +
                "5.抢定向=只抢专属(type21)；不抢赔付=不抢带中文的赔付红包；普通可抢非专属（含中文，除非开不抢赔付）"

    private const val SEND_DESC =
        "发包说明：\n" +
                "1 全雷值模式=标题不带金额（实发仍用「输入金额」）\n" +
                "2 云红包/零用钱与拼手气同走群发包接口\n" +
                "3 循环雷值请用/分割：比如567/789/912\n" +
                "4 设置好后手动发一次红包"

    private const val COMP_DESC =
        "注意:只赔付组合红包语【金额+有分隔符号+雷值比如100-123】或组合红包语【金额+雷值比如100123】"

    fun tabTitles(): List<String> =
        listOf("基本设置", "秒抢设置", "发包设置", "爆粉设置", "赔付设置")

    fun rowsForTab(config: MsConfig, tabIndex: Int): List<SettingsRow> = when (tabIndex) {
        0 -> basicRows(config)
        1 -> grabRows(config)
        2 -> sendRows(config)
        3 -> fanRows(config)
        4 -> compRows(config)
        else -> emptyList()
    }

    private fun basicRows(c: MsConfig): List<SettingsRow> = listOf(
        SettingsRow(
            "masterSwitch", "总开关", RowType.Switch,
            switchValue = c.masterSwitch, placeholder = "【打开之后功能生效】"
        ),
        SettingsRow(
            "redPacketReminder", "红包提醒", RowType.Switch,
            switchValue = c.redPacketReminder, placeholder = "【抢到红包会有文字+语音提醒】"
        ),
        SettingsRow(
            "backgroundGrab", "后台抢包", RowType.Switch,
            switchValue = c.backgroundGrab, placeholder = "【必须打开定位功能】"
        ),
        SettingsRow("grabLog", "抢包日志", RowType.Action, value = "查看"),
        SettingsRow(
            "diagLog", "诊断日志", RowType.Action,
            value = "复制排查",
            placeholder = "【秒抢/发包/赔付流程日志，可复制发给开发】"
        ),
        //SettingsRow("ios14AuthTip", "14系统以上授权的正常方法", RowType.Description, value = IOS14_TIP),
        SettingsRow("desc", "说明", RowType.Description, value = BASIC_DESC)
    )

    private fun grabRows(c: MsConfig): List<SettingsRow> = listOf(
        SettingsRow(
            "onlyPinnedChats", "置顶群聊", RowType.Switch,
            switchValue = c.onlyPinnedChats, placeholder = "【打开后只抢置顶群聊】"
        ),
        SettingsRow(
            "grabNormalRedPacket", "抢普通红包", RowType.Switch,
            switchValue = c.grabNormalRedPacket, placeholder = "【打开后自动抢普通红包】"
        ),
        SettingsRow(
            "grabTargetedRedPacket", "抢定向红包", RowType.Switch,
            switchValue = c.grabTargetedRedPacket, placeholder = "【打开后只抢定向赔付的红包】"
        ),
        SettingsRow(
            "grabPrivateRedPacket", "抢私聊红包", RowType.Switch,
            switchValue = c.grabPrivateRedPacket, placeholder = "【自动抢私聊红包】"
        ),
        SettingsRow(
            "autoReceiveTransfer", "自动领转账", RowType.Switch,
            switchValue = c.autoReceiveTransfer, placeholder = "【自动领转账】"
        ),
        SettingsRow(
            "skipCompensationPacket", "不抢赔付包", RowType.Switch,
            switchValue = c.skipCompensationPacket, placeholder = "【不抢带中文的赔付红包】"
        ),
        SettingsRow(
            "autoGrabTailIfNotMine", "自动发包尾包不是雷自动抢", RowType.Switch,
            switchValue = c.autoGrabTailIfNotMine
        ),
        SettingsRow(
            "skipTailIfMine",
            "尾巴是雷不抢",
            RowType.Switch,
            switchValue = c.skipTailIfMine
        ),
        SettingsRow(
            "onlyGrabMineCount", "只抢几雷及以上", RowType.TextFieldWithSwitch,
            value = if (c.onlyGrabMineCount == 0) "" else c.onlyGrabMineCount.toString(),
            placeholder = "【比如选择2择会抢2345雷】", switchValue = c.onlyGrabMineCountEnabled
        ),
        SettingsRow(
            "fixedMineIndex", "抢固定雷", RowType.SegmentWithSwitch,
            segmentOptions = mineSegments, selectedIndex = c.fixedMineIndex,
            switchValue = c.fixedMineEnabled
        ),
        SettingsRow("onlyMultiMine", "只抢多雷", RowType.Switch, switchValue = c.onlyMultiMine),
        SettingsRow(
            "grabSpecifiedUsers",
            "抢指定人",
            RowType.TextFieldWithSwitch,
            value = c.grabSpecifiedUsersDisplay.ifBlank { c.grabSpecifiedUsers },
            auxValue = c.grabSpecifiedUsers,
            placeholder = "【点开指定人头像右上角选择只抢此人】",
            switchValue = c.grabSpecifiedUsersEnabled
        ),
        SettingsRow(
            "skipSpecifiedUsers",
            "不抢此人",
            RowType.TextFieldWithSwitch,
            value = c.skipSpecifiedUsersDisplay.ifBlank { c.skipSpecifiedUsers },
            auxValue = c.skipSpecifiedUsers,
            placeholder = "【点开指定人头像右上角选择不抢此人】",
            switchValue = c.skipSpecifiedUsersEnabled
        ),
        SettingsRow(
            "grabDelayMs", "抢包延迟", RowType.TextField,
            value = if (c.grabDelayMs == 0) "" else c.grabDelayMs.toString(),
            placeholder = "延迟秒抢，【单位1=1毫秒】"
        ),
        SettingsRow(
            "skipSelf", "不抢自己", RowType.Switch,
            switchValue = c.skipSelf, placeholder = "【不抢自己发的红包】"
        ),
        SettingsRow(
            "specifiedAmount", "指定金额", RowType.TextFieldWithSwitch,
            value = c.specifiedAmount, placeholder = "【只抢大于输入金额的红包】",
            switchValue = c.specifiedAmountEnabled,
            inputMode = InputMode.Decimal
        ),
        SettingsRow("desc", "说明", RowType.Description, value = GRAB_DESC)
    )

    private fun sendRows(c: MsConfig): List<SettingsRow> = listOf(
        SettingsRow(
            "autoSendPassword", "自动发包密码", RowType.TextField,
            value = c.autoSendPassword, placeholder = "请输入6位支付密码"
        ),
        SettingsRow(
            "autoSendInterval", "自动发包间隔", RowType.TextField,
            value = c.autoSendInterval.toInt().toString(), placeholder = "输入间隔(默认5秒)",
            inputMode = InputMode.Integer
        ),
        SettingsRow("sendAmount", "输入金额", RowType.TextField, value = c.sendAmount, inputMode = InputMode.Decimal),
        SettingsRow(
            "greetingAmount", "红包语金额", RowType.TextField,
            value = c.greetingAmount, placeholder = "输入红包语金额"
        ),
        SettingsRow(
            "packetCount", "红包个数", RowType.TextField,
            value = c.packetCount.toString(), placeholder = "输入红包个数",
            inputMode = InputMode.Integer
        ),
        SettingsRow(
            "separator", "分隔符", RowType.TextField,
            value = c.separator, placeholder = "设置分隔符（比如\\、-）"
        ),
        SettingsRow("allMineMode", "全雷值模式", RowType.Switch, switchValue = c.allMineMode),
        SettingsRow(
            "cloudRedPacketSend",
            "云红包发包",
            RowType.Switch,
            switchValue = c.cloudRedPacketSend
        ),
        SettingsRow(
            "pocketMoneySend",
            "零用钱发包",
            RowType.Switch,
            switchValue = c.pocketMoneySend
        ),
        SettingsRow(
            "fixedMineValue",
            "固定雷值",
            RowType.TextFieldWithSwitch,
            value = c.fixedMineValue,
            placeholder = "输入固定雷值",
            switchValue = c.fixedMineValueEnabled
        ),
        SettingsRow(
            "loopMineValue", "循环雷值", RowType.TextFieldWithSwitch,
            value = c.loopMineValue, placeholder = "输入雷值，多个用符号/",
            switchValue = c.loopMineValueEnabled
        ),
        SettingsRow(
            "randomMineIndex", "随机雷值", RowType.SegmentWithSwitch,
            segmentOptions = mineSegments, selectedIndex = c.randomMineIndex,
            switchValue = c.randomMineEnabled
        ),
        SettingsRow("desc", "发包说明", RowType.Description, value = SEND_DESC)
    )

    private fun fanRows(c: MsConfig): List<SettingsRow> {
        val interval = maxOf(5, c.fanIntervalSec)
        val fanDesc = String.format(
            "1⃣️普通人群=点一下群里的爆保存当前群成员\n" +
                    "2⃣️精准人群=打开开关后自动监控账号所在全部群，保存所有群发包人（不必盯着某一个群）\n" +
                    "3⃣️如果需要大小号切换-请先用大号添加好数据后：- 直接切换账号到小号在爆粉区进行爆粉\n" +
                    "4⃣️平台限制，每%d秒添加1个好友",
            interval
        )
        return listOf(
            SettingsRow("normalFan", "普通人群爆粉", RowType.Action, value = "本地爆粉区人群爆粉"),
            SettingsRow("preciseFan", "精准人群爆粉", RowType.Action, value = "精准人群区爆粉"),
            SettingsRow("customFan", "自选人群爆粉", RowType.Action, value = "自选人群区爆粉"),
            SettingsRow(
                "addPreciseCrowdEnabled", "添加精准人群", RowType.Switch,
                switchValue = c.addPreciseCrowdEnabled,
                placeholder = "【打开后监控账号所在全部群的发包人】"
            ),
            SettingsRow("desc", "爆粉说明", RowType.Description, value = fanDesc)
        )
    }

    private fun compRows(c: MsConfig): List<SettingsRow> = listOf(
        SettingsRow(
            "autoCompensation",
            "自动赔付",
            RowType.Switch,
            switchValue = c.autoCompensation
        ),
        SettingsRow(
            "compensationPassword", "密码", RowType.TextField,
            value = c.compensationPassword, placeholder = "请输入6位支付密码"
        ),
        SettingsRow(
            "hideCompensationBlessing", "赔付祝福语隐藏", RowType.Switch,
            switchValue = c.hideCompensationBlessing
        ),
        SettingsRow(
            "compensationDelaySec", "赔付延迟", RowType.TextField,
            value = if (c.compensationDelaySec == 0) "" else c.compensationDelaySec.toString(),
            placeholder = "延迟秒抢，单位1=1秒"
        ),
        SettingsRow(
            "memoryTimeSec", "红包记忆时间", RowType.TextField,
            value = if (c.memoryTimeSec == 0) "" else c.memoryTimeSec.toString(),
            placeholder = "记忆时间；0=不限"
        ),
        SettingsRow(
            "skipAmountAbove", "不赔多少以上的金额", RowType.TextField,
            value = c.skipAmountAbove, placeholder = "不赔多少以上的金额"
        ),
        SettingsRow(
            "skipPacketCount", "不赔付红包个数", RowType.NumberPicker,
            numberOptions = packetOptions, value = c.skipPacketCount
        ),
        SettingsRow("compensationMatrix", "九宫格", RowType.CompensationGrid),
        SettingsRow("discountMatrix", "打折", RowType.DiscountGrid),
        SettingsRow("desc", "特殊说明", RowType.Description, value = COMP_DESC)
    )
}
