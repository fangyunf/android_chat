package com.machinesecond.auth

import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.util.MainHandler
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 对齐 iOS MSActivationClient：表单登录 + 到期查询，网络/5xx 时按备用主机切换线路。
 */
class AuthRepository(
    private val host: HostBridge,
    private val config: MsConfig
) {

    private var lastLoginBody: String? = null

    companion object {
        private val expireFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        private val failKeywords = listOf("失败", "错误", "无效", "error", "不存在", "禁止")

        /** 对齐 iOS MSActivationFailoverHosts（XOR 0x5A 还原） */
        private val failoverHosts = listOf(
            "w.eydata.net",
            "w3.eydata.net",
            "w5.eydata.net",
            "w1.eydata.net",
            "w2.eydata.net",
            "w4.eydata.net",
            "vip1.eydata.net",
            "vip2.eydata.net",
            "vip3.eydata.net"
        )
    }

    fun login(code: String, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val body = buildString {
                    append("SingleCode=").append(urlEncode(code))
                    append("&Ver=1.0")
                    append("&Mac=").append(urlEncode(host.deviceUuid()))
                }
                val response = postFormTryingUrls(candidateUrls(host.authLoginUrl()), body)
                val ok = isLoginSuccess(response)
                MainHandler.post {
                    if (ok) {
                        lastLoginBody = response
                        config.setAuthCredentials(code, null)
                        fetchExpire(code, 0, callback)
                    } else {
                        callback(false, parseFailMessage(response))
                    }
                }
            } catch (e: Exception) {
                MainHandler.post { callback(false, e.message ?: "授权失败") }
            }
        }.start()
    }

    private fun fetchExpire(code: String, attempt: Int, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val body = "UserName=${urlEncode(code)}"
                val response = postFormTryingUrls(candidateUrls(host.authExpireUrl()), body)
                val normalized = normalizeExpireString(response)
                val parsed = parseExpire(normalized)
                if (parsed == null || is1900Expire(normalized) || parsed <= System.currentTimeMillis()) {
                    if (attempt < 3) {
                        MainHandler.postDelayed(600) {
                            fetchExpire(code, attempt + 1, callback)
                        }
                    } else {
                        MainHandler.post { callback(false, "授权失败") }
                    }
                    return@Thread
                }
                config.setAuthCredentials(code, normalized)
                config.synchronize()
                MainHandler.post { callback(true, normalized) }
            } catch (e: Exception) {
                val fallback = lastLoginBody?.let { normalizeExpireString(it) }
                val parsed = fallback?.let { parseExpire(it) }
                if (fallback != null && parsed != null && !is1900Expire(fallback) && parsed > System.currentTimeMillis()) {
                    config.setAuthCredentials(code, fallback)
                    config.synchronize()
                    MainHandler.post { callback(true, fallback) }
                } else {
                    MainHandler.post { callback(false, "授权失败：无法解析到期时间") }
                }
            }
        }.start()
    }

    private fun candidateUrls(preferred: String): List<String> {
        val out = linkedSetOf<String>()
        if (preferred.isNotBlank()) out.add(preferred)
        try {
            val u = URL(preferred)
            val path = u.path ?: ""
            if (path.isNotEmpty()) {
                val scheme = u.protocol ?: "https"
                for (h in failoverHosts) {
                    out.add("$scheme://$h$path")
                }
            }
        } catch (_: Exception) {
        }
        return out.toList()
    }

    /**
     * 仅网络/5xx 切下一条；业务回包（含 4xx 正文）不切线。
     */
    private fun postFormTryingUrls(urls: List<String>, body: String): String {
        if (urls.isEmpty()) throw IllegalStateException("授权地址无效")
        var lastError: Exception? = null
        for (url in urls) {
            try {
                val result = postFormOnce(url, body)
                if (result.lineError) {
                    lastError = Exception(result.text.ifBlank { "线路异常" })
                    continue
                }
                return result.text
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("全部线路请求失败")
    }

    private data class FormResult(val text: String, val lineError: Boolean)

    private fun postFormOnce(urlStr: String, body: String): FormResult {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.doOutput = true
        conn.connectTimeout = 12_000
        conn.readTimeout = 12_000
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        val code = conn.responseCode
        // 5xx/无响应：换线路
        if (code >= 500 || code == 0) {
            return FormResult("线路异常($code)", lineError = true)
        }
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = BufferedReader(stream.reader()).use { it.readText() }
        // 4xx：当业务失败文案返回，不换线
        return FormResult(text, lineError = false)
    }

    private fun isLoginSuccess(response: String): Boolean {
        val trimmed = response.trim()
        if (trimmed.length <= 10) return false
        val lower = trimmed.lowercase(Locale.US)
        return failKeywords.none { trimmed.contains(it) || lower.contains(it.lowercase(Locale.US)) }
    }

    private fun parseFailMessage(response: String): String {
        val t = response.trim()
        return if (t.isBlank()) "授权失败" else t
    }

    private fun parseExpire(text: String): Long? =
        try {
            expireFormat.parse(text)?.time
        } catch (_: Exception) {
            null
        }

    private fun normalizeExpireString(raw: String): String {
        var s = raw.trim().trim('"').trim('\'')
        if (parseExpire(s) == null && s.length >= 19) {
            s = s.take(19)
        }
        return s
    }

    private fun is1900Expire(text: String): Boolean =
        text.startsWith("1900-01-01")

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
}
