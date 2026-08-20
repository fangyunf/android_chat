package com.machinesecond.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * 红包语音：内置 WAV 片段拼接播放（对齐 qiangbao，不依赖系统中文 TTS）。
 * 片段目录：assets/qb_voice/
 */
object MsVoiceAlert {
    private const val TAG = "MsVoice"
    private const val ASSET_DIR = "qb_voice"
    /** 资源包版本：换 wav 时 +1，避免覆盖安装后仍读旧 cache */
    private const val PACK = 3
    private const val SAMPLE_RATE = 22050
    private const val PLAYBACK_SPEED = 1.25f

    private val lock = Any()
    private val gen = AtomicInteger(0)
    private val io: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "ms-voice").apply { isDaemon = true }
    }

    @Volatile
    private var player: MediaPlayer? = null

    @Volatile
    private var warmed = false

    fun warmUp(context: Context) {
        val app = context.applicationContext
        io.execute {
            synchronized(lock) {
                if (warmed) return@execute
                ensureCache(app)
                warmed = true
                Log.i(TAG, "warmUp ok dir=${cacheDir(app).absolutePath}")
            }
        }
    }

    /**
     * @param kind 0=普通抢到 22=私聊 23=专属 28=转账
     * @param yuan 金额（元）
     */
    fun speakAmount(context: Context, kind: Int, yuan: Double) {
        val app = context.applicationContext
        maybeLogSilentVolume(app)
        val myGen = gen.incrementAndGet()
        io.execute {
            try {
                playLocked(app, kind, yuan, myGen)
            } catch (t: Throwable) {
                Log.w(TAG, "play fail: ${t.message}", t)
                DiagLogStore.append(app, "Voice", "play fail ${t.javaClass.simpleName}")
            }
        }
    }

    private fun playLocked(app: Context, kind: Int, yuan: Double, myGen: Int) {
        if (myGen != gen.get()) return
        ensureCache(app)
        val clips = buildClips(kind, yuan)
        if (clips.isEmpty()) return
        val pcm = ByteArrayOutputStream(64 * 1024)
        for (name in clips) {
            if (myGen != gen.get()) return
            val bytes = loadPcm(app, name) ?: run {
                Log.w(TAG, "missing clip=$name")
                return
            }
            pcm.write(bytes)
        }
        val outFile = File(cacheDir(app), "speak_${myGen}.wav")
        writeWav(outFile, pcm.toByteArray())
        if (myGen != gen.get()) {
            outFile.delete()
            return
        }
        playFile(outFile, myGen)
    }

    private fun buildClips(kind: Int, yuan: Double): List<String> {
        val prefix = when {
            kind == 22 -> "lingsiliao"
            kind == 23 -> "lingzhuanshu"
            kind == 28 -> "lingzhuanzhang"
            else -> "qiangdao"
        }
        val amount = "%.2f".format(Locale.US, yuan)
        val out = ArrayList<String>(12)
        out.add(prefix)
        for (ch in amount) {
            when (ch) {
                '.' -> out.add("dian")
                in '0'..'9' -> out.add("d$ch")
            }
        }
        out.add("yuan")
        return out
    }

    private fun playFile(file: File, myGen: Int) {
        synchronized(lock) {
            stopPlayerLocked()
            if (myGen != gen.get()) {
                file.delete()
                return
            }
            val mp = MediaPlayer()
            player = mp
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                mp.setAudioAttributes(attrs)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")
                    mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                }
                mp.setDataSource(file.absolutePath)
                mp.setOnCompletionListener {
                    synchronized(lock) {
                        if (player === mp) {
                            runCatching { mp.release() }
                            player = null
                        }
                    }
                    file.delete()
                }
                mp.setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "MediaPlayer error what=$what extra=$extra")
                    synchronized(lock) {
                        if (player === mp) {
                            runCatching { mp.release() }
                            player = null
                        }
                    }
                    file.delete()
                    true
                }
                mp.prepare()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    runCatching {
                        mp.playbackParams = PlaybackParams().setSpeed(PLAYBACK_SPEED)
                    }
                }
                if (myGen != gen.get()) {
                    stopPlayerLocked()
                    file.delete()
                    return
                }
                mp.start()
                Log.i(TAG, "playing ${file.name} gen=$myGen")
            } catch (t: Throwable) {
                Log.w(TAG, "start fail: ${t.message}")
                runCatching { mp.release() }
                if (player === mp) player = null
                file.delete()
            }
        }
    }

    private fun stopPlayerLocked() {
        val p = player ?: return
        player = null
        runCatching { if (p.isPlaying) p.stop() }
        runCatching { p.release() }
    }

    private fun cacheDir(app: Context): File =
        File(app.cacheDir, "ms_voice_p$PACK").also { it.mkdirs() }

    private fun ensureCache(app: Context) {
        val dir = cacheDir(app)
        val names = listOf(
            "qiangdao", "lingsiliao", "lingzhuanshu", "lingzhuanzhang", "dian", "yuan",
            "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9",
        )
        for (name in names) {
            val dest = File(dir, "$name.wav")
            if (dest.length() > 44) continue
            copyAsset(app, name, dest)
        }
        runCatching {
            app.cacheDir.listFiles()?.forEach { f ->
                if (f.isDirectory && f.name.startsWith("ms_voice") && f.name != dir.name) {
                    f.deleteRecursively()
                }
            }
        }
    }

    private fun copyAsset(app: Context, name: String, dest: File) {
        val assetPath = "$ASSET_DIR/$name.wav"
        try {
            app.assets.open(assetPath).use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            return
        } catch (_: Throwable) {
        }
        Log.w(TAG, "asset missing $assetPath")
    }

    private fun loadPcm(app: Context, name: String): ByteArray? {
        val f = File(cacheDir(app), "$name.wav")
        if (!f.isFile || f.length() < 44) {
            copyAsset(app, name, f)
        }
        if (!f.isFile || f.length() < 44) return null
        val all = f.readBytes()
        val dataOffset = findDataOffset(all) ?: return null
        return all.copyOfRange(dataOffset, all.size)
    }

    private fun findDataOffset(wav: ByteArray): Int? {
        if (wav.size < 44) return null
        var i = 12
        while (i + 8 <= wav.size) {
            val id = String(wav, i, 4, Charsets.US_ASCII)
            val size = ByteBuffer.wrap(wav, i + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (id == "data") return i + 8
            val next = i + 8 + size
            if (next <= i || next > wav.size) break
            i = next
        }
        return if (wav.size > 44) 44 else null
    }

    private fun writeWav(file: File, pcm: ByteArray) {
        val channels = 1
        val bits = 16
        val byteRate = SAMPLE_RATE * channels * bits / 8
        val blockAlign = channels * bits / 8
        val dataSize = pcm.size
        val total = 36 + dataSize
        FileOutputStream(file).use { out ->
            fun w32(v: Int) {
                out.write(
                    byteArrayOf(
                        (v and 0xff).toByte(),
                        ((v shr 8) and 0xff).toByte(),
                        ((v shr 16) and 0xff).toByte(),
                        ((v shr 24) and 0xff).toByte(),
                    )
                )
            }
            fun w16(v: Int) {
                out.write(byteArrayOf((v and 0xff).toByte(), ((v shr 8) and 0xff).toByte()))
            }
            out.write("RIFF".toByteArray(Charsets.US_ASCII))
            w32(total)
            out.write("WAVE".toByteArray(Charsets.US_ASCII))
            out.write("fmt ".toByteArray(Charsets.US_ASCII))
            w32(16)
            w16(1)
            w16(channels)
            w32(SAMPLE_RATE)
            w32(byteRate)
            w16(blockAlign)
            w16(bits)
            out.write("data".toByteArray(Charsets.US_ASCII))
            w32(dataSize)
            out.write(pcm)
        }
    }

    private fun maybeLogSilentVolume(app: Context) {
        try {
            val am = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val media = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val noti = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            if (media == 0 && noti == 0) {
                Log.w(TAG, "volume media=0 notification=0 — voice may be silent")
                DiagLogStore.append(app, "Voice", "volume all zero")
            }
        } catch (_: Throwable) {
        }
    }
}
