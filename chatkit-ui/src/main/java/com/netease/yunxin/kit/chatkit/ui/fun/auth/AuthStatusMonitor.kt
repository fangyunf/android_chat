package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.os.Handler
import android.os.Looper

/**
 * 授权心跳（对齐 qiangbao AuthStatusMonitor，仅雷电自建总端）。
 *
 * - 每 [INTERVAL_MS] 调 query
 * - 网络失败不踢；连续 [FAIL_STREAK_TO_REVOKE] 次明确失败才吊销
 * - -102 / 到期 / -411 立即处理
 * - 回前台离开超过 [FOREGROUND_CHECK_MIN_GAP_MS] 再查一次
 */
class AuthStatusMonitor(
    private val authRepo: AuthRepository,
    private val onRevoked: (String) -> Unit,
) {
    companion object {
        private const val INTERVAL_MS = 300_000L
        private const val FAIL_STREAK_TO_REVOKE = 3
        private const val FOREGROUND_CHECK_MIN_GAP_MS = 60_000L
        private const val MIN_GAP_MS = 15_000L
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var inFlight = false

    @Volatile
    private var failStreak = 0

    @Volatile
    private var lastCheckAt = 0L

    @Volatile
    private var lastBackgroundAt = 0L

    private val tick = object : Runnable {
        override fun run() {
            checkNow(force = false)
            mainHandler.postDelayed(this, INTERVAL_MS)
        }
    }

    fun start() {
        val store = AuthStore.get()
        if (!store.isAuthorized()) {
            AuthLog.w(
                "monitor start SKIP notAuthorized " +
                    AuthLog.snap(store.authCode, store.authExpireTime, store.authStatusCode, false)
            )
            return
        }
        mainHandler.removeCallbacks(tick)
        failStreak = 0
        AuthLog.i(
            "monitor start " +
                AuthLog.snap(store.authCode, store.authExpireTime, store.authStatusCode, true)
        )
        authRepo.prefetchLeidianInit(store.authCode)
        checkNow(force = true)
        mainHandler.postDelayed(tick, INTERVAL_MS)
    }

    fun stop() {
        AuthLog.i("monitor stop")
        mainHandler.removeCallbacks(tick)
    }

    fun onBackground() {
        lastBackgroundAt = System.currentTimeMillis()
        AuthLog.i("monitor onBackground")
    }

    fun onForeground() {
        val bg = lastBackgroundAt
        if (bg <= 0L) return
        val away = System.currentTimeMillis() - bg
        lastBackgroundAt = 0L
        if (away < FOREGROUND_CHECK_MIN_GAP_MS) {
            AuthLog.i("monitor onForeground skip awayMs=$away")
            return
        }
        AuthLog.i("monitor onForeground check awayMs=$away")
        checkNow(force = false)
    }

    fun checkNow(force: Boolean = true) {
        val store = AuthStore.get()
        if (!store.isAuthorized()) {
            AuthLog.w("monitor check skip notAuthorized")
            return
        }
        if (AuthProduct.LOCAL_DEBUG_AUTH ||
            store.authStatusCode == AuthStore.LOCAL_DEBUG_STATUS
        ) {
            AuthLog.i("monitor check skip LOCAL_DEBUG")
            return
        }
        val code = store.authCode.trim()
        if (code.isEmpty()) {
            AuthLog.w("monitor status skip no authCode")
            return
        }
        val now = System.currentTimeMillis()
        if (!force && now - lastCheckAt < MIN_GAP_MS) return
        if (inFlight) {
            AuthLog.i("monitor check skip inFlight")
            return
        }
        inFlight = true
        lastCheckAt = now
        AuthLog.i("monitor checkNow force=$force streak=$failStreak")
        authRepo.checkUserStatus(code) { passed, message ->
            mainHandler.post {
                inFlight = false
                if (passed) {
                    if (failStreak != 0) AuthLog.i("monitor check pass reset streak was=$failStreak")
                    failStreak = 0
                    return@post
                }
                val msg = message.orEmpty()
                AuthLog.w("monitor status fail msg=$msg")

                when {
                    msg == AuthRepository.LEIDIAN_NOT_FOUND -> {
                        revoke("", "leidian-102")
                    }
                    msg == AuthRepository.LEIDIAN_NEED_ACTIVATE -> {
                        softNeedActivate("需要授权才能继续使用")
                    }
                    msg == AuthRepository.LEIDIAN_OPEN_UNBIND -> {
                        AuthLog.w("monitor mismatch tip only (no open unbind)")
                        com.yaoxin.appbase.utils.ToastUtils.toastMsg("授权失败")
                    }
                    msg.contains("到期") || msg.contains("冻结") || msg.contains("作废") -> {
                        revoke(msg, "leidian-terminal")
                    }
                    else -> {
                        failStreak++
                        AuthLog.w("monitor leidian-dead streak=$failStreak/$FAIL_STREAK_TO_REVOKE msg=$msg")
                        if (failStreak >= FAIL_STREAK_TO_REVOKE) {
                            failStreak = 0
                            revoke(msg.ifBlank { "授权已失效，请重新授权" }, "leidian-dead")
                        }
                    }
                }
            }
        }
    }

    private fun softNeedActivate(toast: String) {
        val store = AuthStore.get()
        AuthLog.w(
            "NEED_ACTIVATE keepKey " +
                AuthLog.snap(store.authCode, store.authExpireTime, store.authStatusCode, store.isAuthorized())
        )
        failStreak = 0
        stop()
        onRevoked(AuthRepository.LEIDIAN_NEED_ACTIVATE + "|" + toast)
    }

    private fun revoke(reason: String, source: String) {
        val store = AuthStore.get()
        AuthLog.w(
            "REVOKE source=$source reason=$reason " +
                AuthLog.snap(store.authCode, store.authExpireTime, store.authStatusCode, store.isAuthorized())
        )
        stop()
        onRevoked(reason)
    }
}
