package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.netease.yunxin.kit.chatkit.ui.`fun`.redpacket.RedPacketAutoManager
import com.yaoxin.appbase.utils.ToastUtils

/** Java / UI 侧激活码入口 + 心跳。 */
object AuthHelper {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var monitor: AuthStatusMonitor? = null

    @Volatile
    private var lifecycleWired = false

    @JvmStatic
    fun canUse(): Boolean = AuthStore.get().canUseFeatures()

    @JvmStatic
    fun isAuthorized(): Boolean = AuthStore.get().isAuthorized()

    /**
     * 可使用功能时返回 true。
     * 未激活弹激活框；已激活但总开关关闭则提示打开总开关。
     */
    @JvmStatic
    fun requireAuth(activity: Activity): Boolean {
        if (canUse()) return true
        if (!AuthStore.get().isAuthorized()) {
            AuthDialog.showUnauthorized(activity) { showActivate(activity, null) }
            return false
        }
        ToastUtils.toastMsg("请先打开总开关")
        return false
    }

    @JvmStatic
    @JvmOverloads
    fun showActivate(activity: Activity, onSuccess: Runnable? = null) {
        AuthDialog.showActivate(activity, AuthStore.get().authCode) { code, finish ->
            val repo = AuthRepository(deviceIdProvider = { DeviceMac.forContext(activity) })
            repo.verifyAuthCode(code) { ok, _, msg ->
                activity.runOnUiThread {
                    if (ok) {
                        ToastUtils.toastMsg("授权成功")
                        onAuthSuccess()
                        onSuccess?.run()
                    }
                    finish(ok, msg)
                }
            }
        }
    }

    /** Application 启动：静默验卡 + 启心跳 + 前后台监听。 */
    @JvmStatic
    fun silentLoginOnStart(context: Context) {
        val app = context.applicationContext as? Application ?: return
        wireLifecycle(app)
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            if (AuthStore.get().isAuthorized()) onAuthSuccess()
            return
        }
        val store = AuthStore.get()
        if (store.authCode.isEmpty() || store.isRebindRequired) {
            AuthLog.w("silentLogin skip — no key or rebindRequired")
            return
        }
        val repo = AuthRepository(deviceIdProvider = { DeviceMac.forContext(app) })
        ensureMonitor(app, repo)
        repo.silentLogin { ok, msg ->
            mainHandler.post {
                val m = msg.orEmpty()
                AuthLog.i("silentLogin ok=$ok msg=$m")
                when {
                    ok -> {
                        if (store.isAuthorized()) monitor?.start()
                    }
                    m == AuthRepository.LEIDIAN_NEED_ACTIVATE ->
                        softNeedActivate("需要授权才能继续使用")
                    m == AuthRepository.LEIDIAN_NOT_FOUND ->
                        revokeAuth("")
                    m.contains("到期") || m.contains("冻结") || m.contains("作废") ->
                        revokeAuth(m)
                    else -> {
                        // 网络失败等：本地仍有效才启心跳
                        if (store.isAuthorized()) {
                            AuthLog.w("silentLogin soft-fail keep auth msg=$m")
                            monitor?.start()
                        }
                    }
                }
            }
        }
    }

    /** 验卡成功后启心跳。 */
    @JvmStatic
    fun onAuthSuccess() {
        val ctx = com.yaoxin.appbase.utils.AppProxy.getInstance().context ?: return
        val app = ctx.applicationContext as? Application ?: return
        val repo = AuthRepository(deviceIdProvider = { DeviceMac.forContext(app) })
        ensureMonitor(app, repo)
        AuthLog.i(
            "onAuthSuccess startMonitor " +
                AuthLog.snap(
                    AuthStore.get().authCode,
                    AuthStore.get().authExpireTime,
                    AuthStore.get().authStatusCode,
                    AuthStore.get().isAuthorized(),
                )
        )
        monitor?.start()
    }

    private fun ensureMonitor(app: Application, repo: AuthRepository) {
        if (monitor != null) return
        monitor = AuthStatusMonitor(repo) { reason ->
            if (reason.startsWith(AuthRepository.LEIDIAN_NEED_ACTIVATE)) {
                val toast = reason.substringAfter("|", "需要授权才能继续使用")
                    .ifBlank { "需要授权才能继续使用" }
                softNeedActivate(toast)
            } else {
                revokeAuth(reason)
            }
        }
        wireLifecycle(app)
    }

    private fun wireLifecycle(app: Application) {
        if (lifecycleWired) return
        lifecycleWired = true
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private var startedCount = 0

            override fun onActivityResumed(activity: Activity) {
                if (startedCount == 1) monitor?.onForeground()
            }

            override fun onActivityStarted(activity: Activity) {
                startedCount++
            }

            override fun onActivityStopped(activity: Activity) {
                startedCount = (startedCount - 1).coerceAtLeast(0)
                if (startedCount == 0) monitor?.onBackground()
            }

            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }

    private fun softNeedActivate(toast: String) {
        val store = AuthStore.get()
        val alreadyWaiting = store.isRebindRequired && !store.isAuthorized()
        AuthLog.w("softNeedActivate already=$alreadyWaiting toast=$toast")
        monitor?.stop()
        RedPacketAutoManager.get().stopAll(null)
        store.markNeedActivate()
        if (toast.isNotBlank() && !alreadyWaiting) {
            ToastUtils.toastMsg(toast)
        }
    }

    private fun revokeAuth(reason: String) {
        AuthLog.w("revokeAuth reason=$reason")
        monitor?.stop()
        RedPacketAutoManager.get().stopAll(null)
        val store = AuthStore.get()
        val clearReason = when {
            reason.isBlank() ||
                reason == AuthRepository.LEIDIAN_NOT_FOUND ||
                reason.contains("卡密不存在") -> AuthStore.AUTH_UNAUTH_DELETED
            reason.contains("到期") -> AuthStore.AUTH_UNAUTH_EXPIRED
            else -> reason.ifBlank { "授权已失效" }
        }
        store.clearAuth(clearReason)
        val tip = when {
            reason.isBlank() -> "授权已失效，请重新授权"
            reason.contains("到期") -> reason
            else -> reason
        }
        if (tip.isNotBlank()) ToastUtils.toastMsg(tip)
    }
}
