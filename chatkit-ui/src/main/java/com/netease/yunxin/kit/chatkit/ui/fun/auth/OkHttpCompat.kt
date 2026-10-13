package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.RequestBody
import java.nio.charset.StandardCharsets

/**
 * 兼容宿主自带 OkHttp 3.x 与 SDK 依赖的 4.x。
 * 禁止使用 Kotlin `toMediaType()` / `toRequestBody()`（会访问 4.x 的 Companion，
 * 宿主若是 3.x 会 NoSuchFieldError，导致注入 bootstrap 整段失败）。
 */
object OkHttpCompat {
    fun mediaType(s: String): MediaType {
        val clazz = Class.forName("okhttp3.MediaType")
        for (name in arrayOf("parse", "get")) {
            val m = runCatching { clazz.getMethod(name, String::class.java) }.getOrNull() ?: continue
            val v = m.invoke(null, s) ?: continue
            @Suppress("UNCHECKED_CAST")
            return v as MediaType
        }
        error("okhttp3.MediaType.parse/get missing")
    }

    fun requestBody(content: String, mediaType: MediaType?): RequestBody {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        return requestBody(bytes, mediaType)
    }

    fun requestBody(bytes: ByteArray, mediaType: MediaType?): RequestBody {
        val clazz = RequestBody::class.java
        // OkHttp3: create(MediaType, byte[])
        runCatching {
            val m = clazz.getMethod("create", MediaType::class.java, ByteArray::class.java)
            return m.invoke(null, mediaType, bytes) as RequestBody
        }
        // OkHttp4: create(byte[], MediaType)
        runCatching {
            val m = clazz.getMethod("create", ByteArray::class.java, MediaType::class.java)
            return m.invoke(null, bytes, mediaType) as RequestBody
        }
        // OkHttp3: create(MediaType, String)
        runCatching {
            val m = clazz.getMethod("create", MediaType::class.java, String::class.java)
            return m.invoke(null, mediaType, String(bytes, StandardCharsets.UTF_8)) as RequestBody
        }
        // OkHttp4: create(String, MediaType)
        val m = clazz.getMethod("create", String::class.java, MediaType::class.java)
        return m.invoke(null, String(bytes, StandardCharsets.UTF_8), mediaType) as RequestBody
    }

    fun emptyBody(): RequestBody = requestBody(ByteArray(0), null)

    fun parseHttpUrl(url: String): HttpUrl? {
        val clazz = Class.forName("okhttp3.HttpUrl")
        val m = runCatching { clazz.getMethod("parse", String::class.java) }.getOrNull()
            ?: return null
        @Suppress("UNCHECKED_CAST")
        return m.invoke(null, url) as? HttpUrl
    }
}
