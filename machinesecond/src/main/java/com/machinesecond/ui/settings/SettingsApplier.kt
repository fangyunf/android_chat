package com.machinesecond.ui.settings

import com.machinesecond.MsSdk
import com.machinesecond.config.MsConfig

object SettingsApplier {

    fun applyRow(config: MsConfig, row: SettingsRow) {
        when (row.key) {
            "masterSwitch" -> {
                config.masterSwitch = row.switchValue
                if (row.switchValue) config.onMasterSwitchTurnedOn()
            }
            "secondSwitch" -> config.secondSwitch = row.switchValue
            "sendSwitch" -> config.sendSwitch = row.switchValue
            "compensationSwitch" -> config.compensationSwitch = row.switchValue
            "fanSwitch" -> config.fanSwitch = row.switchValue
            "redPacketReminder" -> config.redPacketReminder = row.switchValue
            "backgroundGrab" -> {
                config.backgroundGrab = row.switchValue
                MsSdk.getHost().syncBackgroundKeepAlive(row.switchValue)
            }
            "onlyPinnedChats" -> config.onlyPinnedChats = row.switchValue
            "grabNormalRedPacket" -> config.grabNormalRedPacket = row.switchValue
            "grabTargetedRedPacket" -> config.grabTargetedRedPacket = row.switchValue
            "grabPrivateRedPacket" -> config.grabPrivateRedPacket = row.switchValue
            "autoReceiveTransfer" -> config.autoReceiveTransfer = row.switchValue
            "skipCompensationPacket" -> config.skipCompensationPacket = row.switchValue
            "autoGrabTailIfNotMine" -> config.autoGrabTailIfNotMine = row.switchValue
            "skipTailIfMine" -> config.skipTailIfMine = row.switchValue
            "onlyGrabMineCount" -> {
                config.onlyGrabMineCountEnabled = row.switchValue
                config.onlyGrabMineCount =
                    (row.value as? String)?.toIntOrNull()?.coerceIn(0, 5) ?: 0
            }
            "fixedMineIndex" -> {
                config.fixedMineEnabled = row.switchValue
                config.fixedMineIndex = row.selectedIndex.coerceIn(0, 4)
            }
            "onlyMultiMine" -> config.onlyMultiMine = row.switchValue
            "grabSpecifiedUsers" -> {
                config.grabSpecifiedUsersEnabled = row.switchValue
                val display = row.value as? String ?: ""
                config.grabSpecifiedUsers = row.auxValue.ifBlank {
                    config.grabSpecifiedUsers.ifBlank { display }
                }
                config.grabSpecifiedUsersDisplay = display
            }
            "skipSpecifiedUsers" -> {
                config.skipSpecifiedUsersEnabled = row.switchValue
                val display = row.value as? String ?: ""
                config.skipSpecifiedUsers = row.auxValue.ifBlank {
                    config.skipSpecifiedUsers.ifBlank { display }
                }
                config.skipSpecifiedUsersDisplay = display
            }
            "grabDelayMs" -> config.grabDelayMs = (row.value as? String)?.toIntOrNull() ?: 0
            "skipSelf" -> config.skipSelf = row.switchValue
            "specifiedAmount" -> {
                config.specifiedAmountEnabled = row.switchValue
                config.specifiedAmount = row.value as? String ?: ""
            }
            "autoSendPassword" -> config.setAutoSendPassword(row.value as? String ?: "")
            "autoSendInterval" -> config.autoSendInterval =
                ((row.value as? String)?.toIntOrNull()?.takeIf { it > 0 } ?: 5).toDouble()
            "sendAmount" -> config.sendAmount = row.value as? String ?: ""
            "greetingAmount" -> config.greetingAmount = row.value as? String ?: ""
            "packetCount" -> {
                val v = (row.value as? String)?.toIntOrNull() ?: 0
                config.packetCount = maxOf(1, v) // 对齐 iOS MSSettingsDataSource：MAX(1,v)
            }
            "separator" -> config.separator = row.value as? String ?: ""
            "allMineMode" -> config.allMineMode = row.switchValue
            "cloudRedPacketSend" -> config.cloudRedPacketSend = row.switchValue
            "pocketMoneySend" -> config.pocketMoneySend = row.switchValue
            "fixedMineValue" -> {
                config.fixedMineValueEnabled = row.switchValue
                config.fixedMineValue = row.value as? String ?: ""
            }
            "loopMineValue" -> {
                config.loopMineValueEnabled = row.switchValue
                config.loopMineValue = row.value as? String ?: ""
            }
            "randomMineIndex" -> {
                config.randomMineEnabled = row.switchValue
                config.randomMineIndex = row.selectedIndex.coerceIn(0, 4)
            }
            "addPreciseCrowdEnabled" -> {
                config.addPreciseCrowdEnabled = row.switchValue
                if (row.switchValue) config.fanSwitch = true
            }
            "autoCompensation" -> config.autoCompensation = row.switchValue
            "compensationPassword" -> config.setCompensationPassword(row.value as? String ?: "")
            "hideCompensationBlessing" -> config.hideCompensationBlessing = row.switchValue
            "compensationDelaySec" -> config.compensationDelaySec =
                (row.value as? String)?.toIntOrNull() ?: 0
            "memoryTimeSec" -> config.memoryTimeSec = (row.value as? String)?.toIntOrNull() ?: 0
            "skipAmountAbove" -> config.skipAmountAbove = row.value as? String ?: ""
            "skipPacketCount" -> config.skipPacketCount = row.value as? String ?: ""
        }
    }
}
