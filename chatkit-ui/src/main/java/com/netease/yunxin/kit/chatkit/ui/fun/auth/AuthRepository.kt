package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 激活码验卡（雷电自建总端，对齐 qiangbao AuthRepository.verifyLeidian）。
 */
class AuthRepository(
    private val deviceIdProvider: () -> String,
    private val client: OkHttpClient = defaultClient(),
) {
    companion object {
        const val LEIDIAN_STATUS = LeidianLicenseClient.STATUS
        const val LEIDIAN_NEED_ACTIVATE = LeidianLicenseClient.NEED_ACTIVATE
        const val LEIDIAN_NOT_FOUND = LeidianLicenseClient.NOT_FOUND
        const val LEIDIAN_OPEN_UNBIND = LeidianLicenseClient.OPEN_UNBIND

        private fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .writeTimeout(12, TimeUnit.SECONDS)
                .build()
    }

    /** 本地已有卡密时拉 /api/app/init；失败不影响授权。 */
    fun prefetchLeidianInit(authCode: String?) {
        if (authCode.isNullOrBlank()) return
        if (AuthProduct.LOCAL_DEBUG_AUTH) return
        Thread {
            LeidianLicenseClient(client).init()
        }.start()
    }

    /**
     * 心跳 query。
     * - 网络失败 / 无法识别：passed=true（不吊销）
     * - 明确业务失败：passed=false
     */
    fun checkUserStatus(
        authCode: String?,
        onResult: (passed: Boolean, message: String?) -> Unit,
    ) {
        val un = authCode?.trim().orEmpty()
        if (un.isEmpty()) {
            AuthLog.w("leidian status skip no authCode")
            onResult(true, null)
            return
        }
        val mac = DeviceMac.normalize(deviceIdProvider())
        AuthLog.i("leidian status query code=${AuthLog.maskCode(un)} mac=${AuthLog.maskMac(mac)}")
        Thread {
            val r = LeidianLicenseClient(client).query(un, mac)
            when {
                r.ok -> {
                    r.expire?.let { exp ->
                        AuthStore.get().setAuth(un, exp, LEIDIAN_STATUS)
                        AuthStore.get().applyServerClock(r.expireTs, r.serverTs)
                    }
                    AuthLog.i("leidian status OK exp=${r.expire}")
                    onResult(true, null)
                }
                r.needActivate -> {
                    AuthLog.w("leidian status -411 need activate (keep key)")
                    onResult(false, LEIDIAN_NEED_ACTIVATE)
                }
                r.silentClear -> {
                    AuthLog.w("leidian status -102 silent clear")
                    onResult(false, LEIDIAN_NOT_FOUND)
                }
                r.openUnbind -> {
                    AuthLog.w("leidian status mismatch code=${r.apiCode} (no open unbind)")
                    onResult(false, LEIDIAN_OPEN_UNBIND)
                }
                r.apiCode == -5 || r.message == "卡密已到期" -> {
                    AuthLog.w("leidian status expired")
                    onResult(false, r.message ?: "卡密已到期")
                }
                r.apiCode == -4 -> {
                    onResult(false, r.message ?: "卡密已冻结或作废，请联系客服")
                }
                r.message?.contains("网络") == true || r.message?.contains("请求失败") == true -> {
                    AuthLog.w("leidian status netFail=${r.message} (pass, no revoke)")
                    onResult(true, null)
                }
                else -> {
                    AuthLog.w("leidian status FAIL msg=${r.message}")
                    onResult(false, r.message)
                }
            }
        }.start()
    }

    fun verifyAuthCode(
        raw: String,
        onResult: (ok: Boolean, expire: String?, msg: String?) -> Unit,
    ) {
        val code = raw.trim()
        if (code.isEmpty()) {
            onResult(false, null, "授权码不能为空")
            return
        }
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            val expire = AuthStore.LOCAL_DEBUG_EXPIRE
            AuthStore.get().setAuth(code, expire, AuthStore.LOCAL_DEBUG_STATUS)
            onResult(true, expire, null)
            return
        }
        val mac = DeviceMac.normalize(deviceIdProvider())
        if (mac.length < 8) {
            onResult(false, null, "设备码异常，请重装后重试")
            return
        }
        AuthLog.i("verify leidian activate code=${AuthLog.maskCode(code)} mac=${AuthLog.maskMac(mac)}")
        Thread {
            val lic = LeidianLicenseClient(client)
            var r = lic.activate(code, mac)
            if (!r.ok && r.apiCode == -412) {
                AuthLog.i("leidian activate → login (-412)")
                r = lic.login(code, mac)
            }
            when {
                r.ok -> {
                    val exp = r.expire
                    if (!exp.isNullOrBlank()) {
                        AuthStore.get().setAuth(code, exp, LEIDIAN_STATUS)
                        AuthStore.get().applyServerClock(r.expireTs, r.serverTs)
                        onResult(true, exp, null)
                    } else {
                        onResult(false, null, "授权失败：无法解析到期时间")
                    }
                }
                r.openUnbind -> onResult(false, null, "授权失败")
                else -> onResult(false, null, r.message ?: "授权失败")
            }
        }.start()
    }

    /** 冷启动静默校验；网络失败不踢。 */
    fun silentLogin(onResult: (ok: Boolean, msg: String?) -> Unit) {
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            onResult(true, null)
            return
        }
        val store = AuthStore.get()
        val code = store.authCode.trim()
        if (code.isEmpty()) {
            onResult(false, "无卡密")
            return
        }
        val mac = DeviceMac.normalize(deviceIdProvider())
        Thread {
            val r = LeidianLicenseClient(client).login(code, mac)
            when {
                r.ok -> {
                    r.expire?.let {
                        store.setAuth(code, it, LEIDIAN_STATUS)
                        store.applyServerClock(r.expireTs, r.serverTs)
                    }
                    onResult(true, null)
                }
                r.needActivate -> {
                    store.markNeedActivate()
                    onResult(false, LEIDIAN_NEED_ACTIVATE)
                }
                r.silentClear -> {
                    store.clearAuth(AuthStore.AUTH_UNAUTH_DELETED)
                    onResult(false, LEIDIAN_NOT_FOUND)
                }
                r.message?.contains("网络") == true || r.message?.contains("请求失败") == true ->
                    onResult(true, null)
                r.apiCode == -5 || r.message == "卡密已到期" -> {
                    store.clearAuth(AuthStore.AUTH_UNAUTH_EXPIRED)
                    onResult(false, r.message)
                }
                else -> onResult(false, r.message)
            }
        }.start()
    }
}
