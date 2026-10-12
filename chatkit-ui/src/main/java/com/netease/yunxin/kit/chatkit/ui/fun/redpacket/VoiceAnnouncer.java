package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.yaoxin.appbase.utils.AppProxy;

import java.util.Locale;

/**
 * 专属红包领取成功语音播报，对齐 iOS AVSpeechSynthesizer（zh-CN，静音模式也可出声）。
 */
public class VoiceAnnouncer implements TextToSpeech.OnInitListener {

    private static VoiceAnnouncer instance;
    private TextToSpeech tts;
    private boolean ready;
    private String pendingText;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private VoiceAnnouncer() {
        Context ctx = AppProxy.getInstance().getContext();
        if (ctx != null) {
            Context appCtx = ctx.getApplicationContext();
            audioManager = (AudioManager) appCtx.getSystemService(Context.AUDIO_SERVICE);
            tts = new TextToSpeech(appCtx, this);
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
        if (!ready || tts == null) {
            return;
        }
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (tts.isLanguageAvailable(locale) < TextToSpeech.LANG_AVAILABLE) {
            locale = Locale.CHINA;
        }
        tts.setLanguage(locale);
        tts.setAudioAttributes(buildPlaybackAttributes());
        if (pendingText != null) {
            speakInternal(pendingText);
            pendingText = null;
        }
    }

    public void announceExclusive(long amountCents) {
        RedPacketAutoConfig config = new RedPacketAutoConfig();
        if (!config.isVoiceAnnounceEnabled()) {
            return;
        }
        String text = buildAnnounceText(amountCents);
        if (!ready || tts == null) {
            pendingText = text;
            return;
        }
        speakInternal(text);
    }

    private void speakInternal(String text) {
        requestPlaybackFocus();
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "redpacket_exclusive");
    }

    private static String buildAnnounceText(long amountCents) {
        if (amountCents > 0) {
            return "领取" + formatYuan(amountCents);
        }
        return "领取成功";
    }

    /** 整元无小数，有小数保留两位。 */
    private static String formatYuan(long amountCents) {
        if (amountCents % 100 == 0) {
            return (amountCents / 100) + "元";
        }
        return String.format(Locale.CHINA, "%.2f元", amountCents / 100.0);
    }

    /**
     * 对齐 iOS：category .playback + mixWithOthers，走媒体流且可与其它音频混播，静音模式尽量出声。
     */
    private static AudioAttributes buildPlaybackAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build();
    }

    private void requestPlaybackFocus() {
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(buildPlaybackAttributes())
                        .setAcceptsDelayedFocusGain(false)
                        .setWillPauseWhenDucked(false)
                        .build();
            }
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
    }
}
