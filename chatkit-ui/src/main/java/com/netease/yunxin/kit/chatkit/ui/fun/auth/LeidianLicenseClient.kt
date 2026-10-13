package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

// ProductRuntime -> AuthProduct
// OkHttpCompat same package
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * 自建总端卡密（APP-CLIENT-GUIDE 2026-08-28）：
 * - 用户点授权 → [activate]（唯一写机器码入口）
 * - 启动/回前台静默 → [login]（只校验，不写 mac）
 * - 心跳 → [query]（只校验）
 * - 解绑后 login/query → `-411`，须用户再点授权；勿清本地卡密
 *
 * 线路：主 [ProductRuntime.licenseBaseUrl]（通常 https://ios99.vip），
 * 失败再试备用（对齐 iOS 默认国内 http://8.136.47.17）。
 * 网络/网关失败与业务失败（卡密无效等）都会换备用再试；全部失败则返回最后一次结果。
 */
internal class LeidianLicenseClient(
    private val client: OkHttpClient,
) {
    companion object {
        const val STATUS = "LEIDIAN"
        const val NOT_FOUND = "LEIDIAN_-102"
        /** 需要用户点授权；保留本地 key */
        const val NEED_ACTIVATE = "LEIDIAN_-411"
        /** 机器码/IP 不匹配（-402/-403）；内部标记，对用户只提示「授权失败」、不打开解绑页 */
        const val OPEN_UNBIND = "LEIDIAN_UNBIND"
        /** 对齐 iOS kMSDefaultLicenseFallback：国内 HTTP，避开与主站同类 TLS/跨境问题 */
        const val DEFAULT_FALLBACK_BASE = "http://8.136.47.17"
        // 对齐 iOS：Content-Type 不带 charset，部分网关对 `application/json; charset=utf-8` 更挑
        private val JSON by lazy { OkHttpCompat.mediaType("application/json") }
    }

    data class Result(
        val ok: Boolean,
        val expire: String? = null,
        val message: String? = null,
        val silentClear: Boolean = false,
        val needActivate: Boolean = false,
        val openUnbind: Boolean = false,
        val apiCode: Int = 0,
        val expireTs: Long = 0L,
        val serverTs: Long = 0L,
    )

    /** 用户点「授权」：首次 / 解绑后重绑 */
    fun activate(key: String, mcode: String): Result =
        call("activate", key, mcode, loginUx = true)

    /** 静默登录：仅已绑定设备 */
    fun login(key: String, mcode: String): Result =
        call("login", key, mcode, loginUx = true)

    fun query(key: String, mcode: String): Result =
        call("query", key, mcode, loginUx = false)

    /** 仅 safecode；失败只打日志，不影响授权。 */
    fun init(): Result {
        val safecode = AuthProduct.LICENSE_SAFE_CODE
        if (safecode.isEmpty()) {
            return Result(false, message = "未配置安全码")
        }
        val body = JSONObject().put("safecode", safecode).toString()
        return postJsonPath("init", body) { text, http, base ->
            httpSpecial(http, text)?.let { return@postJsonPath it }
            val root = runCatching { JSONObject(text) }.getOrNull()
            if (root == null) {
                AuthLog.w("leidian init http=$http base=$base head=${text.take(40)}")
                return@postJsonPath Result(false, message = httpFallbackMsg(http, text))
            }
            val ok = root.optBoolean("ok", false)
            val code = root.optInt("code", 0)
            val data = root.optJSONObject("data")
            AuthLog.i(
                "leidian init ok=$ok code=$code http=$http base=$base " +
                    "name=${data?.optString("name").orEmpty()} " +
                    "bindMac=${data?.optBoolean("bind_mac")} bindIp=${data?.optBoolean("bind_ip")} " +
                    "noticeLen=${data?.optString("notice").orEmpty().length}"
            )
            if (ok || code == 1) Result(true) else {
                Result(
                    false,
                    message = root.optString("message").ifBlank { "init 失败" },
                    apiCode = code,
                )
            }
        }
    }

    fun unbindPageUrl(): String {
        val base = trimBase(AuthProduct.LICENSE_BASE_URL)
        return "$base/card-unbind/"
    }

    private fun call(path: String, key: String, mcode: String, loginUx: Boolean): Result {
        val safecode = AuthProduct.LICENSE_SAFE_CODE
        if (safecode.isEmpty()) {
            return Result(false, message = "未配置安全码")
        }
        val body = JSONObject()
            .put("safecode", safecode)
            .put("key", key.trim())
            .put("mcode", mcode)
            .put("platform", "android")
            .toString()
        return postJsonPath(path, body) { text, http, base ->
            httpSpecial(http, text)?.let { return@postJsonPath it }
            if (runCatching { JSONObject(text) }.getOrNull() == null) {
                AuthLog.w("leidian $path http=$http base=$base head=${text.take(40)}")
                return@postJsonPath Result(false, message = httpFallbackMsg(http, text))
            }
            val parsed = parse(text, loginUx, path)
            if (http !in 200..299) {
                AuthLog.w(
                    "leidian $path http=$http base=$base bizCode=${parsed.apiCode} msg=${parsed.message}"
                )
            }
            parsed
        }
    }

    /**
     * 主站 → 配置备用 → 内置国内默认。
     * 网络/网关失败、坏 JSON（0|≥500）、以及业务失败（ok=false）有备用时都会再试。
     */
    private fun postJsonPath(
        path: String,
        body: String,
        handle: (text: String, http: Int, base: String) -> Result,
    ): Result {
        val bases = baseCandidates()
        if (bases.isEmpty()) return Result(false, message = "授权地址无效")
        var lastMsg = "网络错误"
        var lastBiz: Result? = null
        for ((i, base) in bases.withIndex()) {
            val url = "$base/api/app/$path"
            val hasNext = i + 1 < bases.size
            AuthLog.i("leidian POST $path base=$base try=${i + 1}/${bases.size}")
            val attempt = try {
                val req = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .post(OkHttpCompat.requestBody(body, JSON))
                    .build()
                client.newCall(req).execute().use { resp ->
                    val text = resp.body?.string().orEmpty()
                    val http = resp.code
                    // 网关级失败：有备用则直接换线，不解析业务
                    if (shouldFailover(error = null, http = http) && hasNext) {
                        lastMsg = "HTTP $http"
                        AuthLog.w("leidian FAILOVER $path from=$base http=$http → next")
                        null
                    } else if (
                        runCatching { JSONObject(text) }.getOrNull() == null &&
                        shouldFailoverBadJson(http) &&
                        hasNext
                    ) {
                        lastMsg = "服务器响应异常"
                        AuthLog.w("leidian FAILOVER $path bad json http=$http → next")
                        null
                    } else {
                        handle(text, http, base)
                    }
                }
            } catch (t: Throwable) {
                lastMsg = friendlyNetMessage(t)
                if (hasNext) {
                    AuthLog.w("leidian FAILOVER $path from=$base err=${t.message} → next")
                    null
                } else {
                    AuthLog.w("leidian $path netFail base=$base ${t.message}")
                    Result(false, message = lastMsg)
                }
            }
            if (attempt == null) continue
            // 成功：立刻返回
            if (attempt.ok) return attempt
            // 业务失败（卡密无效/到期等）也试备用；两边都挂再回最后一次业务结果
            lastBiz = attempt
            lastMsg = attempt.message?.takeIf { it.isNotBlank() } ?: lastMsg
            if (hasNext) {
                AuthLog.w(
                    "leidian FAILOVER $path from=$base bizCode=${attempt.apiCode} " +
                        "msg=${attempt.message} → next"
                )
                continue
            }
            return attempt
        }
        return lastBiz ?: Result(false, message = lastMsg)
    }

    /** 主站 + 产品备用 + 内置国内默认（去重保序） */
    private fun baseCandidates(): List<String> {
        val out = linkedSetOf<String>()
        trimBase(AuthProduct.LICENSE_BASE_URL).takeIf { it.isNotEmpty() }?.let { out.add(it) }
        trimBase(AuthProduct.LICENSE_FALLBACK_BASE).takeIf { it.isNotEmpty() }?.let { out.add(it) }
        out.add(DEFAULT_FALLBACK_BASE)
        return out.toList()
    }

    /** 对齐 iOS ms_shouldFailoverAfterError */
    private fun shouldFailover(error: Throwable?, http: Int): Boolean {
        if (error != null) return true
        if (http == 0 || http == 502 || http == 503 || http == 504 || http >= 520) return true
        return false
    }

    private fun shouldFailoverBadJson(http: Int): Boolean =
        http == 0 || http >= 500

    private fun friendlyNetMessage(t: Throwable): String {
        val raw = t.message.orEmpty()
        if (raw.contains("Failed to connect", ignoreCase = true) ||
            raw.contains("Unable to resolve host", ignoreCase = true) ||
            raw.contains("timeout", ignoreCase = true) ||
            raw.contains("timed out", ignoreCase = true)
        ) {
            return "无法连接授权服务器，请检查网络后重试"
        }
        return raw.ifBlank { "网络失败" }
    }

    /** 对齐 iOS：429 / 410 单独提示；其余有 JSON 则交给 [parse] */
    private fun httpSpecial(http: Int, text: String): Result? = when (http) {
        429 -> Result(false, message = "请求过于频繁，请稍后重试", apiCode = 429)
        410 -> Result(false, message = "请打开网页解绑后再登录", apiCode = -410)
        else -> null
    }.also {
        if (it != null) AuthLog.w("leidian http=$http special msg=${it.message} head=${text.take(40)}")
    }

    private fun httpFallbackMsg(http: Int, text: String): String {
        val head = text.trim().take(40)
        return when {
            http == 400 && head.isNotEmpty() && !head.startsWith("{") -> head
            http in 200..299 -> "服务器响应异常"
            else -> "请求失败($http)"
        }
    }

    private fun trimBase(raw: String): String {
        var b = raw.trim()
        while (b.endsWith("/")) b = b.dropLast(1)
        return b
    }

    private fun parse(text: String, loginUx: Boolean, path: String): Result {
        val root = runCatching { JSONObject(text) }.getOrNull()
            ?: return Result(false, message = "授权回包无法解析")
        val ok = root.optBoolean("ok", false)
        val code = root.optInt("code", 0)
        val message = root.optString("message").ifBlank { root.optString("msg") }
        val data = root.optJSONObject("data")
        AuthLog.i("leidian $path ok=$ok code=$code msg=${message.take(40)}")

        when (code) {
            -102 -> return if (loginUx) {
                Result(false, message = "卡密无效，请检查后重试", apiCode = code)
            } else {
                Result(false, message = NOT_FOUND, silentClear = true, apiCode = code)
            }
            -411 -> return Result(
                false,
                message = NEED_ACTIVATE,
                needActivate = true,
                apiCode = code,
                expire = parseExpire(data),
            )
            -402, -403 -> return Result(
                false,
                message = OPEN_UNBIND,
                openUnbind = true,
                apiCode = code,
            )
            -412 -> {
                return Result(false, message = "LEIDIAN_-412", apiCode = code)
            }
            -5 -> return Result(false, message = "卡密已到期", apiCode = code)
            -4 -> return Result(false, message = "卡密已冻结或作废，请联系客服", apiCode = code)
            -406 -> return Result(false, message = "卡密与本软件不匹配", apiCode = code)
            -408 -> return Result(false, message = "卡密平台不匹配（请用安卓卡）", apiCode = code)
        }

        if (!ok && code != 1) {
            return Result(
                false,
                message = message.ifBlank { "授权失败" },
                apiCode = code,
            )
        }

        val expired = data?.optBoolean("expired", false) == true
        if (expired) {
            return Result(false, message = "卡密已到期", apiCode = code)
        }
        val clock = readServerClock(data)
        val expire = parseExpire(data)
        if (expire.isNullOrBlank()) {
            return Result(false, message = "授权失败：无法解析到期时间", apiCode = code)
        }
        return Result(
            true,
            expire = expire,
            apiCode = code,
            expireTs = clock.first,
            serverTs = clock.second,
        )
    }

    private fun readServerClock(data: JSONObject?): Pair<Long, Long> {
        if (data == null) return 0L to 0L
        return unixSeconds(data.optLong("expire_ts", 0L)) to unixSeconds(data.optLong("server_ts", 0L))
    }

    private fun unixSeconds(raw: Long): Long {
        if (raw <= 0L) return 0L
        return if (raw >= 10_000_000_000L) raw / 1000L else raw
    }

    private fun parseExpire(data: JSONObject?): String? {
        if (data == null) return null
        normalizedExpireString(data.optString("expire_at"))?.let { return it }
        val ts = data.optLong("expire_ts", 0L)
        if (ts <= 0L) return null
        val ms = if (ts < 10_000_000_000L) ts * 1000L else ts
        return normalizedExpireString(
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getDefault() }
                .format(java.util.Date(ms))
        )
    }

    private fun normalizedExpireString(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        val patterns = arrayOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
        )
        for (p in patterns) {
            val sdf = java.text.SimpleDateFormat(p, java.util.Locale.US)
            sdf.isLenient = false
            val d = runCatching { sdf.parse(t) }.getOrNull() ?: continue
            return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getDefault() }
                .format(d)
        }
        return null
    }
}
