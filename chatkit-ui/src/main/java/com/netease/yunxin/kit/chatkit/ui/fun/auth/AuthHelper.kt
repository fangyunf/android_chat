package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.app.Activity
import android.content.Context
import com.yaoxin.appbase.utils.ToastUtils

/** Java / UI 侧激活码入口。 */
object AuthHelper {

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
                        onSuccess?.run()
                    }
                    finish(ok, msg)
                }
            }
        }
    }

    @JvmStatic
    fun silentLoginOnStart(context: Context) {
        if (AuthProduct.LOCAL_DEBUG_AUTH) return
        if (AuthStore.get().authCode.isEmpty()) return
        val repo = AuthRepository(deviceIdProvider = { DeviceMac.forContext(context) })
        repo.silentLogin { ok, msg -> AuthLog.i("silentLogin ok=$ok msg=$msg") }
    }
}
