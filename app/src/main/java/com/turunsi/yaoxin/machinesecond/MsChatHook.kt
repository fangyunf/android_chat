package com.turunsi.yaoxin.machinesecond

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.machinesecond.api.MachineSecond
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.yaoxin.appbase.utils.AppProxy

/**
 * 群聊进出 + 全局收消息。
 * 退出群聊界面时自动停发包、解除该群秒抢武装；进设置页不关武装。
 */
object MsChatHook {

    private const val TEAM_CHAT_ACTIVITY =
        "com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatTeamActivity"
    private const val SETTINGS_ACTIVITY =
        "com.machinesecond.ui.settings.SettingsActivity"

    private const val LEAVE_DELAY_MS = 450L
    private const val APP_EXIT_DELAY_MS = 800L

    private var msgObserverRegistered = false
    private var lifecycleRegistered = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private var leaveToken = 0
    private var pendingLeaveGid: String? = null
    private var settingsOpen = false
    private var activeTeamGid: String? = null

    private var startedActivityCount = 0
    private var appExitToken = 0

    private val receiveObserver = Observer<List<IMMessage>> { messages ->
        if (messages.isNullOrEmpty()) return@Observer
        val bySession = LinkedHashMap<String, MutableList<Any>>()
        for (msg in messages) {
            if (msg.sessionType != SessionTypeEnum.Team && msg.sessionType != SessionTypeEnum.P2P) {
                continue
            }
            val sid = msg.sessionId ?: continue
            bySession.getOrPut(sid) { ArrayList() }.add(msg)
        }
        for ((sid, list) in bySession) {
            MachineSecond.onReceiveMessages(list, sid)
        }
    }

    fun install(app: Application) {
        if (!lifecycleRegistered) {
            lifecycleRegistered = true
            app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

                override fun onActivityStarted(activity: Activity) {
                    startedActivityCount++
                    cancelAppExitReset()
                }

                override fun onActivityStopped(activity: Activity) {
                    startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                    if (startedActivityCount == 0) {
                        scheduleAppExitReset()
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityResumed(activity: Activity) {
                    if (isSettingsActivity(activity)) {
                        settingsOpen = true
                        cancelPendingLeave()
                        return
                    }
                    if (!isTeamChatActivity(activity)) return
                    cancelPendingLeave()
                    val sessionId = resolveSessionId(activity) ?: return
                    activeTeamGid = sessionId
                    try {
                        AppProxy.getInstance().setTeamId(sessionId)
                    } catch (_: Exception) {
                    }
                    MachineSecond.setActiveGroupId(sessionId, true)
                    val parent = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
                    MachineSecond.installChatFloating(parent, sessionId)
                    parent.post {
                        val last = parent.childCount - 1
                        if (last >= 0) {
                            parent.getChildAt(last)?.bringToFront()
                        }
                    }
                }

                override fun onActivityPaused(activity: Activity) {
                    if (isSettingsActivity(activity)) {
                        settingsOpen = false
                        return
                    }
                    if (!isTeamChatActivity(activity)) return
                    val sessionId = resolveSessionId(activity) ?: activeTeamGid
                    MachineSecond.uninstallChatFloating()
                    // 进设置：软离开（不停发包）；真正退出再停
                    if (settingsOpen) {
                        MachineSecond.onLeaveChat(sessionId, suppressDisarm = true)
                        return
                    }
                    // 延迟判定真正离开，避免短暂 pause 把连续发包掐断
                    MachineSecond.setActiveGroupId(sessionId, false)
                    scheduleLeave(sessionId)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (!isTeamChatActivity(activity)) return
                    val sessionId = resolveSessionId(activity) ?: activeTeamGid
                    cancelPendingLeave()
                    if (!settingsOpen) {
                        MachineSecond.onLeaveChat(sessionId, suppressDisarm = false)
                    } else {
                        MachineSecond.onLeaveChat(sessionId, suppressDisarm = true)
                    }
                    if (activeTeamGid == sessionId) activeTeamGid = null
                }
            })
        }
        registerMsgObserver()
    }

    private fun scheduleAppExitReset() {
        val token = ++appExitToken
        mainHandler.postDelayed({
            if (token != appExitToken) return@postDelayed
            if (startedActivityCount == 0) {
                MachineSecond.onAppBackgrounded()
            }
        }, APP_EXIT_DELAY_MS)
    }

    private fun cancelAppExitReset() {
        appExitToken++
    }

    private fun scheduleLeave(groupId: String?) {
        val gid = groupId?.takeIf { it.isNotBlank() } ?: return
        pendingLeaveGid = gid
        val token = ++leaveToken
        mainHandler.postDelayed({
            if (token != leaveToken) return@postDelayed
            if (settingsOpen) {
                MachineSecond.onLeaveChat(gid, suppressDisarm = true)
            } else {
                MachineSecond.onLeaveChat(gid, suppressDisarm = false)
            }
            pendingLeaveGid = null
        }, LEAVE_DELAY_MS)
    }

    private fun cancelPendingLeave() {
        leaveToken++
        pendingLeaveGid = null
    }

    private fun isTeamChatActivity(activity: Activity): Boolean {
        val name = activity.javaClass.name
        return name == TEAM_CHAT_ACTIVITY || name.endsWith(".FunChatTeamActivity")
    }

    private fun isSettingsActivity(activity: Activity): Boolean {
        val name = activity.javaClass.name
        return name == SETTINGS_ACTIVITY ||
            (name.contains("machinesecond") && name.endsWith(".SettingsActivity"))
    }

    private fun resolveSessionId(activity: Activity): String? {
        val intent = activity.intent
        val fromString = intent?.getStringExtra(RouterConstant.CHAT_ID_KRY)
        if (!fromString.isNullOrEmpty()) return fromString

        val serializable = intent?.getSerializableExtra(RouterConstant.CHAT_ID_KRY)
        if (serializable is String && serializable.isNotEmpty()) return serializable

        return try {
            AppProxy.getInstance().teamId?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    fun registerMsgObserver() {
        if (msgObserverRegistered) return
        try {
            NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage(receiveObserver, true)
            msgObserverRegistered = true
        } catch (_: Exception) {
        }
    }

    fun unregisterMsgObserver() {
        if (!msgObserverRegistered) return
        try {
            NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage(receiveObserver, false)
        } catch (_: Exception) {
        }
        msgObserverRegistered = false
    }
}
