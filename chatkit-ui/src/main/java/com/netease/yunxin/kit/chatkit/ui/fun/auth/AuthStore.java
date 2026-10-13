package com.netease.yunxin.kit.chatkit.ui.fun.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.yaoxin.appbase.utils.AppProxy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 激活码 / 总开关持久化（对齐 qiangbao QbConfig auth 段）。
 */
public final class AuthStore {

    public static final String AUTH_UNAUTH_EXPIRED = "expired";
    public static final String AUTH_UNAUTH_DELETED = "deleted";
    public static final String LEIDIAN_NEED_ACTIVATE_STATUS = "LEIDIAN_NEED_ACTIVATE";
    public static final String LOCAL_DEBUG_EXPIRE = "2099-12-31 23:59:59";
    public static final String LOCAL_DEBUG_STATUS = "LOCAL_DEBUG";

    private static final String SP = "yx_red_packet_auth";
    private static final String KEY_AUTH_CODE = "authCode";
    private static final String KEY_AUTH_EXPIRE = "authExpireTime";
    private static final String KEY_AUTH_STATUS = "authStatusCode";
    private static final String KEY_AUTH_EXPIRE_TS = "authExpireTs";
    private static final String KEY_AUTH_SERVER_TS = "authServerTs";
    private static final String KEY_AUTH_SERVER_SYNC = "authServerSyncLocalTs";
    private static final String KEY_REBIND = "authRebindRequired";
    private static final String KEY_UNAUTH_REASON = "authUnauthorizedReason";
    private static final String KEY_MASTER = "masterSwitch";

    private static AuthStore instance;
    private final SharedPreferences sp;

    private AuthStore(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(SP, Context.MODE_PRIVATE);
    }

    public static AuthStore get() {
        if (instance == null) {
            synchronized (AuthStore.class) {
                if (instance == null) {
                    Context ctx = AppProxy.getInstance().getContext();
                    instance = new AuthStore(ctx);
                }
            }
        }
        return instance;
    }

    public boolean isMasterSwitchOn() {
        return sp.getBoolean(KEY_MASTER, true);
    }

    public void setMasterSwitch(boolean on) {
        sp.edit().putBoolean(KEY_MASTER, on).apply();
    }

    public String getAuthCode() {
        return sp.getString(KEY_AUTH_CODE, "");
    }

    public String getAuthExpireTime() {
        return sp.getString(KEY_AUTH_EXPIRE, null);
    }

    public String getAuthStatusCode() {
        return sp.getString(KEY_AUTH_STATUS, null);
    }

    public boolean isRebindRequired() {
        return sp.getBoolean(KEY_REBIND, false);
    }

    public String getUnauthorizedReason() {
        return sp.getString(KEY_UNAUTH_REASON, null);
    }

    public void setAuth(String authCode, String expireTime, String statusCode) {
        SharedPreferences.Editor e = sp.edit();
        e.putString(KEY_AUTH_CODE, authCode == null ? "" : authCode.trim());
        e.putString(KEY_AUTH_EXPIRE, expireTime);
        e.putString(KEY_AUTH_STATUS, statusCode);
        e.putBoolean(KEY_REBIND, false);
        e.remove(KEY_UNAUTH_REASON);
        e.apply();
    }

    public void applyServerClock(long expireTs, long serverTs) {
        SharedPreferences.Editor e = sp.edit();
        e.putLong(KEY_AUTH_EXPIRE_TS, Math.max(0, expireTs));
        e.putLong(KEY_AUTH_SERVER_TS, Math.max(0, serverTs));
        e.putLong(KEY_AUTH_SERVER_SYNC, System.currentTimeMillis() / 1000L);
        e.apply();
    }

    public void clearAuth(String reason) {
        SharedPreferences.Editor e = sp.edit();
        e.remove(KEY_AUTH_CODE);
        e.remove(KEY_AUTH_EXPIRE);
        e.remove(KEY_AUTH_STATUS);
        e.remove(KEY_AUTH_EXPIRE_TS);
        e.remove(KEY_AUTH_SERVER_TS);
        e.remove(KEY_AUTH_SERVER_SYNC);
        e.putBoolean(KEY_REBIND, false);
        if (!TextUtils.isEmpty(reason)) {
            e.putString(KEY_UNAUTH_REASON, reason);
        } else {
            e.remove(KEY_UNAUTH_REASON);
        }
        e.apply();
    }

    public void markNeedActivate() {
        SharedPreferences.Editor e = sp.edit();
        e.putString(KEY_AUTH_STATUS, LEIDIAN_NEED_ACTIVATE_STATUS);
        e.putBoolean(KEY_REBIND, true);
        e.apply();
    }

    /** 功能可用：总开关开 + 已授权未到期。 */
    public boolean canUseFeatures() {
        return isMasterSwitchOn() && isAuthorized();
    }

    public boolean isAuthorized() {
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            return true;
        }
        if (isRebindRequired()) {
            return false;
        }
        if (LEIDIAN_NEED_ACTIVATE_STATUS.equals(getAuthStatusCode())) {
            return false;
        }
        String code = getAuthCode();
        if (TextUtils.isEmpty(code)) {
            return false;
        }
        if (code.startsWith("DEBUG_") || "LOCAL_DEBUG".equals(code)
                || LOCAL_DEBUG_STATUS.equals(getAuthStatusCode())) {
            return false;
        }
        long expireTs = sp.getLong(KEY_AUTH_EXPIRE_TS, 0);
        if (expireTs > 0) {
            long serverTs = sp.getLong(KEY_AUTH_SERVER_TS, 0);
            long synced = sp.getLong(KEY_AUTH_SERVER_SYNC, 0);
            long localNow = System.currentTimeMillis() / 1000L;
            long calibrated =
                    (serverTs > 0 && synced > 0) ? localNow + (serverTs - synced) : localNow;
            return expireTs > calibrated;
        }
        String exp = getAuthExpireTime();
        if (TextUtils.isEmpty(exp)) {
            return false;
        }
        Long t = parseExpireMs(exp);
        return t != null && System.currentTimeMillis() < t;
    }

    public String authDisplayValue() {
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            String exp = getAuthExpireTime();
            return TextUtils.isEmpty(exp) ? LOCAL_DEBUG_EXPIRE : exp;
        }
        if (isAuthorized()) {
            String exp = getAuthExpireTime();
            return TextUtils.isEmpty(exp) ? "" : exp;
        }
        if (isRebindRequired() || LEIDIAN_NEED_ACTIVATE_STATUS.equals(getAuthStatusCode())) {
            return "需要授权";
        }
        if (AUTH_UNAUTH_EXPIRED.equals(getUnauthorizedReason())) {
            return "卡密到期";
        }
        if (AUTH_UNAUTH_DELETED.equals(getUnauthorizedReason())) {
            return "卡密被删除";
        }
        return "";
    }

    public String authPlaceholder() {
        if (AuthProduct.LOCAL_DEBUG_AUTH) {
            return "本地调试已自动授权";
        }
        if (isAuthorized()) {
            return "请授权";
        }
        if (isRebindRequired() || LEIDIAN_NEED_ACTIVATE_STATUS.equals(getAuthStatusCode())) {
            return "需要授权";
        }
        if (AUTH_UNAUTH_EXPIRED.equals(getUnauthorizedReason())) {
            return "卡密到期";
        }
        if (AUTH_UNAUTH_DELETED.equals(getUnauthorizedReason())) {
            return "卡密被删除";
        }
        return "请授权";
    }

    private static Long parseExpireMs(String exp) {
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setTimeZone(TimeZone.getDefault());
                Date d = sdf.parse(exp.trim());
                if (d != null) {
                    return d.getTime();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
