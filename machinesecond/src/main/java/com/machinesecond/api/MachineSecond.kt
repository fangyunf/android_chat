package com.machinesecond.api

import android.content.Context
import android.view.ViewGroup
import com.machinesecond.MsSdk
import com.machinesecond.host.HostBridge

/**
 * MachineSecond SDK 对外入口（宿主 App 只依赖本包 + [HostBridge]）。
 *
 * 宿主职责：实现 [HostBridge]（网络 / IM / 好友），在 Application 调 [init]。
 * SDK 职责：秒抢 / 发包 / 赔付 / 爆粉 / 群发 / 设置页 / 浮动钮。
 *
 * ```
 * // Application
 * MachineSecond.init(this, MyHostBridge())
 *
 * // 进群聊
 * MachineSecond.setActiveGroupId(gid, true)
 * MachineSecond.installChatFloating(parent, gid)
 *
 * // 收消息
 * MachineSecond.onReceiveMessages(list, sessionId)
 *
 * // 打开设置
 * MachineSecond.presentSettings(activity)
 * ```
 */
object MachineSecond {

    const val VERSION = com.machinesecond.BuildConfig.SDK_VERSION

    /** 初始化 SDK，注入宿主桥接。必须最先调用。 */
    @JvmStatic
    fun init(context: Context, bridge: HostBridge) {
        MsSdk.setup(context, bridge)
    }

    @JvmStatic
    fun onReceiveMessages(rawMessages: List<Any>, sessionId: String) {
        MsSdk.onReceiveMessages(rawMessages, sessionId)
    }

    @JvmStatic
    fun setActiveGroupId(groupId: String?, chatVisible: Boolean) {
        MsSdk.setActiveGroupId(groupId, chatVisible)
    }

    /** 离开群聊：默认停发包并解除该群秒抢武装；进设置页传 suppressDisarm=true */
    @JvmStatic
    @JvmOverloads
    fun onLeaveChat(groupId: String?, suppressDisarm: Boolean = false) {
        MsSdk.onLeaveChat(groupId, suppressDisarm)
    }

    @JvmStatic
    fun installChatFloating(overlayParent: ViewGroup, groupId: String) {
        MsSdk.installChatFloating(overlayParent, groupId)
    }

    @JvmStatic
    fun uninstallChatFloating() {
        MsSdk.uninstallChatFloating()
    }

    @JvmStatic
    fun installMemberProfile(parent: ViewGroup, userId: String, displayName: String) {
        MsSdk.installMemberProfile(parent, userId, displayName)
    }

    @JvmStatic
    fun uninstallMemberProfile() {
        MsSdk.uninstallMemberProfile()
    }

    @JvmStatic
    fun installMassEntry(parent: ViewGroup) {
        MsSdk.installMassEntry(parent)
    }

    @JvmStatic
    fun uninstallMassEntry() {
        MsSdk.uninstallMassEntry()
    }

    @JvmStatic
    @JvmOverloads
    fun presentSettings(context: Context, title: String? = null) {
        MsSdk.presentSettings(context, title)
    }

    @JvmStatic
    fun toggleGrabSession(sessionId: String): Boolean = MsSdk.toggleGrabSession(sessionId)

    @JvmStatic
    fun toggleCompensationSession(sessionId: String): Boolean =
        MsSdk.toggleCompensationSession(sessionId)

    @JvmStatic
    fun toggleAutoSend(groupId: String): Boolean = MsSdk.toggleAutoSend(groupId)

    @JvmStatic
    fun stopAutoSend() {
        MsSdk.stopAutoSend()
    }

    @JvmStatic
    fun saveNormalFanCrowd(groupId: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        MsSdk.saveNormalFanCrowd(groupId, onResult)
    }

    @JvmStatic
    fun stopAll() {
        MsSdk.stopAll()
    }

    /** 应用退到后台 / 大退时调用，关闭发包与秒抢武装 */
    @JvmStatic
    fun onAppBackgrounded() {
        MsSdk.onAppBackgrounded()
    }

    @JvmStatic
    fun clearState() {
        MsSdk.clearState()
    }

    @JvmStatic
    fun addGrabSpecifiedUserId(userId: String, displayName: String = userId) {
        MsSdk.getConfig().addGrabSpecifiedUser(userId, displayName)
        MsSdk.getConfig().synchronize()
    }

    @JvmStatic
    fun removeGrabSpecifiedUserId(userId: String) {
        MsSdk.getConfig().removeGrabSpecifiedUser(userId)
        MsSdk.getConfig().synchronize()
    }

    @JvmStatic
    fun addSkipSpecifiedUserId(userId: String, displayName: String = userId) {
        MsSdk.getConfig().addSkipSpecifiedUser(userId, displayName)
        MsSdk.getConfig().synchronize()
    }

    @JvmStatic
    fun removeSkipSpecifiedUserId(userId: String) {
        MsSdk.getConfig().removeSkipSpecifiedUser(userId)
        MsSdk.getConfig().synchronize()
    }

    @JvmStatic
    fun appendCustomFanUser(userId: String, name: String) {
        MsSdk.getFanStore().appendCustom(userId, name)
    }
}
