package com.machinesecond.mass

import android.content.Context
import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast

class MassSender(
    private val context: Context,
    private val host: HostBridge,
    private val config: MsConfig
) {

    companion object {
        private const val PREFS = "MachineSecond_mass"
        private const val KEY_TEXT = "MachineSecond_MassSendText"
        private const val KEY_IMAGE = "MachineSecond_MassSendImage"
        private const val INTERVAL_MS = 200L
    }

    @Volatile
    private var running = false

    fun isRunning(): Boolean = running

    fun saveText(text: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TEXT, text).apply()
    }

    fun loadText(): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TEXT, "") ?: ""

    fun saveImagePath(path: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_IMAGE, path).apply()
    }

    fun loadImagePath(): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_IMAGE, null)

    fun start(onFinished: () -> Unit = {}) {
        if (!config.masterSwitch || !config.isAuthorized()) {
            MsToast.show(context, "请先打开总开关")
            return
        }
        if (running) {
            MsToast.show(context, "请稍后再试")
            return
        }

        val text = loadText()
        if (text.isBlank()) {
            MsToast.show(context, "请先设置发送内容")
            return
        }

        val friends = host.friendIdsForMass()
        if (friends.isEmpty()) {
            MsToast.show(context, "群发已结束，到聊天列表去查看吧，有可能有延迟...")
            onFinished()
            return
        }

        running = true
        MsToast.show(context, "群发已开发，请稍等处理...")
        sendNext(friends, 0, text, loadImagePath(), onFinished)
    }

    private fun sendNext(
        friends: List<String>,
        index: Int,
        text: String,
        imagePath: String?,
        onFinished: () -> Unit
    ) {
        if (index >= friends.size) {
            running = false
            MsToast.show(context, "群发已结束，到聊天列表去查看吧，有可能有延迟...")
            onFinished()
            return
        }

        host.sendMass(friends[index], text, imagePath) {
            MainHandler.postDelayed(INTERVAL_MS) {
                sendNext(friends, index + 1, text, imagePath, onFinished)
            }
        }
    }
}
