package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.yaoxin.appbase.utils.AppProxy;

import java.util.Locale;

public class VoiceAnnouncer implements TextToSpeech.OnInitListener {

    private static VoiceAnnouncer instance;
    private TextToSpeech tts;
    private boolean ready;

    private VoiceAnnouncer() {
        Context ctx = AppProxy.getInstance().getContext();
        if (ctx != null) {
            tts = new TextToSpeech(ctx.getApplicationContext(), this);
        }
    }

    public static synchronized VoiceAnnouncer get() {
        if (instance == null) {
            instance = new VoiceAnnouncer();
        }
        return instance;
    }

    @Override
    public void onInit(int status) {
        ready = status == TextToSpeech.SUCCESS;
        if (ready && tts != null) {
            tts.setLanguage(Locale.CHINA);
        }
    }

    public void announceExclusive(long amountCents) {
        RedPacketAutoConfig config = new RedPacketAutoConfig();
        if (!config.isVoiceAnnounceEnabled() || !ready || tts == null) {
            return;
        }
        String text;
        if (amountCents > 0) {
            text = "专属红包，" + String.format("%.2f", amountCents / 100.0) + "元，领取成功";
        } else {
            text = "专属红包领取成功";
        }
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "redpacket");
    }
}
