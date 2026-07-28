package com.turunsi.yaoxin.machinesecond

import android.app.Activity
import android.app.Application
import android.os.Bundle
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
 * 单聊/群聊进出 + 全局收消息，喂给 MachineSecond。
 * 注意：chatkit 包名含 `fun`（Kotlin 关键字），此处用类名字符串避免 import 问题。
 */
object MsChatHook {

    private const val TEAM_CHAT_ACTIVITY =
        "com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatTeamActivity"
    private const val P2P_CHAT_ACTIVITY =
        "com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatP2PActivity"

    private var msgObserverRegistered = false
    private var lifecycleRegistered = false

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
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityResumed(activity: Activity) {
                    if (!isChatActivity(activity)) return
                    val sessionId = resolveSessionId(activity) ?: return
                    if (isTeamChatActivity(activity)) {
                        try {
                            AppProxy.getInstance().setTeamId(sessionId)
                        } catch (_: Exception) {
                        }
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
                    if (!isChatActivity(activity)) return
                    MachineSecond.uninstallChatFloating()
                    MachineSecond.setActiveGroupId(null, false)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (!isChatActivity(activity)) return
                    MachineSecond.uninstallChatFloating()
                    MachineSecond.setActiveGroupId(null, false)
                }
            })
        }
        registerMsgObserver()
    }

    private fun isChatActivity(activity: Activity): Boolean {
        return isTeamChatActivity(activity) || isP2pChatActivity(activity)
    }

    private fun isTeamChatActivity(activity: Activity): Boolean {
        val name = activity.javaClass.name
        return name == TEAM_CHAT_ACTIVITY || name.endsWith(".FunChatTeamActivity")
    }

    private fun isP2pChatActivity(activity: Activity): Boolean {
        val name = activity.javaClass.name
        return name == P2P_CHAT_ACTIVITY || name.endsWith(".FunChatP2PActivity")
    }

    /**
     * 群聊=teamId，单聊=对方 accId；优先 Intent。
     * AppProxy.setTeamId 曾因参数名遮蔽未写入字段，不能只依赖它。
     */
    private fun resolveSessionId(activity: Activity): String? {
        val intent = activity.intent
        val fromString = intent?.getStringExtra(RouterConstant.CHAT_ID_KRY)
        if (!fromString.isNullOrEmpty()) return fromString

        val serializable = intent?.getSerializableExtra(RouterConstant.CHAT_ID_KRY)
        if (serializable is String && serializable.isNotEmpty()) return serializable

        if (isTeamChatActivity(activity)) {
            return try {
                AppProxy.getInstance().teamId?.takeIf { it.isNotEmpty() }
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    fun registerMsgObserver() {
        if (msgObserverRegistered) return
        try {
            NIMClient.getService(MsgServiceObserve::class.java)
                .observeReceiveMessage(receiveObserver, true)
            msgObserverRegistered = true
        } catch (_: Exception) {
            // NIM 未初始化时稍后由 login 成功后再调
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
