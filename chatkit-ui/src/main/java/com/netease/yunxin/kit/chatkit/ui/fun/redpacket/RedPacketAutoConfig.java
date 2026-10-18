package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.NumberUtil;

import java.util.Random;

public class RedPacketAutoConfig {

    private static final String SP_NAME = "red_packet_auto_config";
    private static final String KEY_SEND_INTERVAL = "sendInterval";
    private static final String KEY_AMOUNTS = "amounts";
    private static final String KEY_COUNTS = "counts";
    private static final String KEY_GREETINGS = "greetings";
    private static final String KEY_PAY_PASSWORD = "payPassword";
    private static final String KEY_GRAB_DELAY = "grabDelay";
    private static final String KEY_GRAB_EXCLUSIVE = "grabExclusiveEnabled";
    private static final String KEY_GRAB_LUCKY = "grabLuckyEnabled";
    private static final String KEY_VOICE = "voiceAnnounceEnabled";

    private static final Random RANDOM = new Random();

    private final SharedPreferences sp;

    public RedPacketAutoConfig() {
        Context ctx = AppProxy.getInstance().getContext();
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public double getSendInterval() {
        float v = sp.getFloat(KEY_SEND_INTERVAL, 1f);
        return Math.max(0.5, v);
    }

    public void setSendInterval(double seconds) {
        sp.edit().putFloat(KEY_SEND_INTERVAL, (float) Math.max(0.5, seconds)).apply();
    }

    public String getAmounts() {
        return sp.getString(KEY_AMOUNTS, "");
    }

    public void setAmounts(String amounts) {
        sp.edit().putString(KEY_AMOUNTS, amounts == null ? "" : amounts).apply();
    }

    public String getCounts() {
        return sp.getString(KEY_COUNTS, "");
    }

    public void setCounts(String counts) {
        sp.edit().putString(KEY_COUNTS, counts == null ? "" : counts).apply();
    }

    public String getGreetings() {
        return sp.getString(KEY_GREETINGS, "");
    }

    public void setGreetings(String greetings) {
        sp.edit().putString(KEY_GREETINGS, greetings == null ? "" : greetings).apply();
    }

    public String getPayPassword() {
        return sp.getString(KEY_PAY_PASSWORD, "");
    }

    public void setPayPassword(String pwd) {
        sp.edit().putString(KEY_PAY_PASSWORD, pwd == null ? "" : pwd).apply();
    }

    public double getGrabDelay() {
        return sp.getFloat(KEY_GRAB_DELAY, 0f);
    }

    public void setGrabDelay(double seconds) {
        sp.edit().putFloat(KEY_GRAB_DELAY, (float) Math.max(0, seconds)).apply();
    }

    public boolean isGrabExclusiveEnabled() {
        return sp.getBoolean(KEY_GRAB_EXCLUSIVE, false);
    }

    public void setGrabExclusiveEnabled(boolean enabled) {
        sp.edit().putBoolean(KEY_GRAB_EXCLUSIVE, enabled).apply();
    }

    public boolean isGrabLuckyEnabled() {
        return sp.getBoolean(KEY_GRAB_LUCKY, false);
    }

    public void setGrabLuckyEnabled(boolean enabled) {
        sp.edit().putBoolean(KEY_GRAB_LUCKY, enabled).apply();
    }

    public boolean isVoiceAnnounceEnabled() {
        if (!sp.contains(KEY_VOICE)) {
            return true;
        }
        return sp.getBoolean(KEY_VOICE, true);
    }

    public void setVoiceAnnounceEnabled(boolean enabled) {
        sp.edit().putBoolean(KEY_VOICE, enabled).apply();
    }

    public boolean isSendConfigured() {
        String pwd = getPayPassword();
        return hasValidAmount()
                && hasValidCount()
                && pwd != null
                && pwd.length() == 6;
    }

    /** 金额配置里至少有一项能解析出 >0 分 */
    public boolean hasValidAmount() {
        for (String item : splitSlashItems(getAmounts())) {
            if (NumberUtil.formartUploadMoney(item) > 0) {
                return true;
            }
        }
        return false;
    }

    /** 个数配置里至少有一项能解析出 >0 */
    public boolean hasValidCount() {
        for (String item : splitSlashItems(getCounts())) {
            try {
                if (Integer.parseInt(item) > 0) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public int randomAmountCents() {
        String[] parts = splitSlashItems(getAmounts());
        if (parts.length == 0) {
            return 0;
        }
        // 最多尝试全部候选项，避免抽到空项直接停发
        int start = RANDOM.nextInt(parts.length);
        for (int i = 0; i < parts.length; i++) {
            int cents = NumberUtil.formartUploadMoney(parts[(start + i) % parts.length]);
            if (cents > 0) {
                return cents;
            }
        }
        return 0;
    }

    public int randomCount() {
        String[] parts = splitSlashItems(getCounts());
        if (parts.length == 0) {
            return 0;
        }
        int start = RANDOM.nextInt(parts.length);
        for (int i = 0; i < parts.length; i++) {
            try {
                int count = Integer.parseInt(parts[(start + i) % parts.length]);
                if (count > 0) {
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public String randomGreeting() {
        String[] parts = splitSlashItems(getGreetings());
        if (parts.length == 0) {
            return "恭喜发财,大吉大利";
        }
        return parts[RANDOM.nextInt(parts.length)];
    }

    /** 支持 / 、 ， , 全角斜杠 分隔，并去掉空项 */
    private static String[] splitSlashItems(String value) {
        if (TextUtils.isEmpty(value)) {
            return new String[0];
        }
        String normalized = value
                .replace('／', '/')
                .replace('、', '/')
                .replace('，', '/')
                .replace(',', '/');
        String[] raw = normalized.split("/");
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        for (String part : raw) {
            if (part == null) {
                continue;
            }
            String item = part.trim();
            if (!item.isEmpty()) {
                list.add(item);
            }
        }
        return list.toArray(new String[0]);
    }

    public static class SendSnapshot {
        public final double intervalSeconds;
        public final String payPassword;

        public SendSnapshot(double intervalSeconds, String payPassword) {
            this.intervalSeconds = intervalSeconds;
            this.payPassword = payPassword;
        }
    }

    public SendSnapshot createSendSnapshot() {
        return new SendSnapshot(getSendInterval(), getPayPassword());
    }
}
