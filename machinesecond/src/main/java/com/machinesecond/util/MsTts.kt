package com.machinesecond.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class MsTts(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.language = Locale.SIMPLIFIED_CHINESE
                tts?.setSpeechRate(0.7f)
                tts?.setPitch(1.1f)
            }
        }
    }

    fun speak(text: String) {
        if (text.isBlank() || !ready) return
        MainHandler.post {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ms_tts_${System.currentTimeMillis()}")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
