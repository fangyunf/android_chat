package com.machinesecond

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import com.machinesecond.config.MsConfig
import com.machinesecond.engine.CompensationEngine
import com.machinesecond.engine.GrabEngine
import com.machinesecond.engine.SendEngine
import com.machinesecond.fan.FanAddQueue
import com.machinesecond.fan.FanStore
import com.machinesecond.host.HostBridge
import com.machinesecond.host.RedInbound
import com.machinesecond.mass.MassSender
import com.machinesecond.ui.floating.ChatFloatingOverlay
import com.machinesecond.ui.floating.MemberProfilePanel
import com.machinesecond.ui.mass.AddressBookMassEntry
import com.machinesecond.ui.settings.SettingsActivity
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast
import com.machinesecond.util.MsVoiceAlert

object MsSdk {

    private lateinit var appContext: Context
    private lateinit var host: HostBridge
    private lateinit var config: MsConfig
    private lateinit var grabEngine: GrabEngine
    private lateinit var sendEngine: SendEngine
    private lateinit var compensationEngine: CompensationEngine
    private lateinit var fanStore: FanStore
    private lateinit var fanAddQueue: FanAddQueue
    private lateinit var massSender: MassSender

    private var chatFloating: ChatFloatingOverlay? = null
    private var memberPanel: MemberProfilePanel? = null
    private var massEntry: AddressBookMassEntry? = null

    @Volatile
    private var initialized = false

    fun setup(context: Context, bridge: HostBridge) {
        appContext = context.applicationContext
        (appContext as? android.app.Application)?.let { MsToast.register(it) }
        host = bridge
        config = MsConfig.getInstance(appContext, bridge)
        config.reloadFromDisk()
        // 冷启动不恢复上次的秒抢/赔付武装，避免大退后进来仍是开启
        config.disarmAllGrabSessions()
        config.disarmAllCompensationSessions()
        config.synchronize()
        MsVoiceAlert.warmUp(appContext)
        grabEngine = GrabEngine(appContext, host, config)
        sendEngine = SendEngine(appContext, host, config)
        compensationEngine = CompensationEngine(appContext, host, config)
        fanStore = FanStore(appContext)
        fanAddQueue = FanAddQueue(host, config)
        massSender = MassSender(appContext, host, config)
        grabEngine.onGrabSuccessForCompensation = { msg, personalYuan ->
            compensationEngine.afterGrab(msg, personalYuan)
        }

        host.syncBackgroundKeepAlive(config.backgroundGrab)
        initialized = true
        DiagLogStore.append(appContext, "SDK", "setup ok version=${com.machinesecond.BuildConfig.SDK_VERSION}")
    }

    fun onReceiveMessages(rawMessages: List<Any>, sessionId: String) {
        if (!initialized) return
        MainHandler.post {
            compensationEngine.pruneExpiredTracks()
            for (raw in rawMessages) {
                val inbound = host.parseInbound(raw) ?: continue
                val msg = if (inbound.sessionId.isBlank()) {
                    inbound.copy(sessionId = sessionId)
                } else {
                    inbound
                }
                onReceiveSingle(msg)
            }
        }
    }

    private fun onReceiveSingle(msg: RedInbound) {
        if (!config.masterSwitch) return
        handlePreciseFan(msg)
        if (msg.claimed || msg.redPacketId.isBlank()) return
        compensationEngine.ensureTrack(msg)
        grabEngine.onRecv(msg)
    }

    private fun handlePreciseFan(msg: RedInbound) {
        if (msg.customType != 23) return
        if (msg.fromUserId == host.currentUserId()) return
        if (!config.fanSwitch || !config.addPreciseCrowdEnabled) return
        val name = msg.senderDisplayName ?: msg.fromUserId
        fanStore.appendPrecise(msg.fromUserId, name, msg.senderAvatarUrl ?: "", msg.senderMemberCode ?: "")
    }

    fun setActiveGroupId(groupId: String?, chatVisible: Boolean) {
        if (!initialized) return
        sendEngine.setActiveGroupId(groupId, chatVisible)
        DiagLogStore.append(appContext, "Session", "activeGroup=${sendEngine.activeGroupId ?: ""} visible=$chatVisible input=${groupId ?: ""}")
    }

    /**
     * 离开群聊表面：真正退出时停发包并解除该群秒抢/赔付武装。
     * @param suppressDisarm true 时仅摘浮层（进设置页），不停自动发包、不关武装
     */
    fun onLeaveChat(groupId: String?, suppressDisarm: Boolean = false) {
        if (!initialized) return
        uninstallChatFloating()
        if (suppressDisarm) {
            // 进设置：保留发包状态，回群聊后浮钮会按 autoSendActive 恢复
            DiagLogStore.append(appContext, "Session", "leave soft gid=${groupId ?: ""}")
            return
        }
        sendEngine.stopAutoSend()
        sendEngine.setActiveGroupId(groupId, false)
        val gid = groupId?.takeIf { it.isNotBlank() }
        if (gid != null) {
            config.disarmGrabSession(gid)
            config.disarmCompensationSession(gid)
        } else {
            config.disarmAllGrabSessions()
        }
        config.synchronize()
        DiagLogStore.append(appContext, "Session", "leave disarm gid=${gid ?: "*"}")
    }

    fun installChatFloating(overlayParent: ViewGroup, groupId: String) {
        if (!initialized) return
        uninstallChatFloating()
        chatFloating = ChatFloatingOverlay(overlayParent.context, groupId).also {
            overlayParent.addView(it, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
    }

    fun uninstallChatFloating() {
        chatFloating?.let { (it.parent as? ViewGroup)?.removeView(it) }
        chatFloating = null
    }

    fun installMemberProfile(parent: ViewGroup, userId: String, displayName: String) {
        if (!initialized) return
        uninstallMemberProfile()
        memberPanel = MemberProfilePanel(parent.context, userId, displayName).also {
            parent.addView(it, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
    }

    fun uninstallMemberProfile() {
        memberPanel?.let { (it.parent as? ViewGroup)?.removeView(it) }
        memberPanel = null
    }

    fun installMassEntry(parent: ViewGroup) {
        if (!initialized) return
        uninstallMassEntry()
        massEntry = AddressBookMassEntry(parent.context).also {
            parent.addView(it, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
    }

    fun uninstallMassEntry() {
        massEntry?.let { (it.parent as? ViewGroup)?.removeView(it) }
        massEntry = null
    }

    fun presentSettings(context: Context, title: String? = null) {
        if (!initialized) return
        DiagLogStore.append(appContext, "UI", "presentSettings activeGroup=${sendEngine.activeGroupId ?: ""}")
        val intent = Intent(context, SettingsActivity::class.java)
        title?.let { intent.putExtra(SettingsActivity.EXTRA_TITLE, it) }
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toggleGrabSession(sessionId: String): Boolean {
        ensureFloatingGate() ?: return false
        if (!config.secondSwitch) config.secondSwitch = true
        val armed = config.toggleGrabSession(sessionId)
        config.synchronize()
        if (armed && !hasAnyGrabTypeEnabled()) {
            MsToast.show(appContext, "已开启秒抢，请到「秒抢设置」打开「抢普通红包」等类型")
        }
        chatFloating?.refreshState()
        return armed
    }

    fun toggleCompensationSession(sessionId: String): Boolean {
        ensureFloatingGate() ?: return false
        if (!config.compensationSwitch) {
            config.compensationSwitch = true
            config.synchronize()
        }
        if (config.compensationPassword.isEmpty()) {
            MsToast.show(appContext, "请先到「赔付设置」设置赔付密码")
            return config.isCompensationArmed(sessionId)
        }
        val armed = config.toggleCompensationSession(sessionId)
        config.synchronize()
        if (armed && !config.autoCompensation) {
            MsToast.show(appContext, "已开启赔付，请到「赔付设置」打开「自动赔付」")
        }
        chatFloating?.refreshState()
        return armed
    }

    fun toggleAutoSend(groupId: String): Boolean {
        ensureFloatingGate() ?: return false
        if (!config.sendSwitch) {
            config.sendSwitch = true
            config.synchronize()
        }
        if (config.autoSendPassword.length != 6) {
            MsToast.show(appContext, "请先到「发包设置」设置支付密码")
            return sendEngine.autoSendActive
        }
        if (!sendEngine.isSendConfigured()) {
            MsToast.show(appContext, "请先到「发包设置」完善金额、个数等")
            return sendEngine.autoSendActive
        }
        val active = sendEngine.toggleAutoSend(groupId)
        chatFloating?.refreshState()
        return active
    }

    fun stopAutoSend() {
        if (!initialized) return
        sendEngine.stopAutoSend()
        chatFloating?.refreshState()
    }

    fun saveNormalFanCrowd(groupId: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (!config.masterSwitch) {
            onResult(false, "请先打开总开关")
            return
        }
        host.fetchGroupMembers(groupId) { members ->
            MainHandler.post {
                if (members.isEmpty()) {
                    onResult(false, "暂无群成员")
                } else {
                    fanStore.saveNormalFromMembers(members)
                    onResult(true, "已保存到本地,请到爆粉列表查看")
                }
            }
        }
    }

    fun stopAll() {
        if (!initialized) return
        sendEngine.stopAutoSend()
        fanAddQueue.stop()
        uninstallChatFloating()
        uninstallMemberProfile()
        uninstallMassEntry()
        clearState()
        config.disarmAllGrabSessions()
        config.disarmAllCompensationSessions()
        config.synchronize()
        DiagLogStore.append(appContext, "SDK", "stopAll disarm runtime")
    }

    /** 进程退到后台 / 大退：关掉发包与各群武装，设置项（金额密码等）保留 */
    fun onAppBackgrounded() {
        if (!initialized) return
        stopAll()
    }

    fun clearState() {
        if (!initialized) return
        grabEngine.clearState()
        sendEngine.clearState()
        compensationEngine.clearState()
    }

    fun getConfig(): MsConfig {
        check(initialized) { "MsSdk.setup() must be called first" }
        return config
    }

    fun getHost(): HostBridge {
        check(initialized) { "MsSdk.setup() must be called first" }
        return host
    }


    fun getFanStore(): FanStore {
        check(initialized) { "MsSdk.setup() must be called first" }
        return fanStore
    }

    fun getFanAddQueue(): FanAddQueue {
        check(initialized) { "MsSdk.setup() must be called first" }
        return fanAddQueue
    }

    fun getMassSender(): MassSender {
        check(initialized) { "MsSdk.setup() must be called first" }
        return massSender
    }

    fun getSendEngine(): SendEngine {
        check(initialized) { "MsSdk.setup() must be called first" }
        return sendEngine
    }

    private fun ensureFloatingGate(): Boolean? {
        if (!config.masterSwitch) {
            MsToast.show(appContext, "请先打开总开关")
            return null
        }
        return true
    }

    private fun hasAnyGrabTypeEnabled(): Boolean =
        config.grabNormalRedPacket ||
            config.grabTargetedRedPacket ||
            config.grabPrivateRedPacket ||
            config.autoReceiveTransfer
}
