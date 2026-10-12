package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.nis.captcha.Captcha;
import com.netease.nis.captcha.CaptchaConfiguration;
import com.netease.nis.captcha.CaptchaListener;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.SmsCodeResponse;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.view.ImageCaptchaDialog;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 短信 /customer/smsCode：
 * type 0 直接发短信；1 图形验证；2 网易易盾行为验证。
 * validate：首次不传；图形验证传图上文字；易盾传 SDK 返回的 validate。
 */
public class CommonNetUtil {

    /** 与短信接口 data 中 type 一致 */
    public static final int SMS_DATA_TYPE_SENT = 0;
    public static final int SMS_DATA_TYPE_IMAGE_CAPTCHA = 1;
    public static final int SMS_DATA_TYPE_YIDUN = 2;

    /** 巴黎 / 网易易盾业务 ID */
    public static final String YIDUN_CAPTCHA_ID = "a04d5c4d4db243f68fb37cbf157ed82c";

    public interface GraphicCaptchaContinuation {
        void onSmsOk();

        void onReplaceCaptchaImage(@Nullable String newImageUrl);
    }

    /**
     * 704 data 解密后有时是裸 URL，有时是 JSON（如附带 image/url/data 字段）。
     */
    public static String unwrapGraphicCaptchaUrl(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String s = raw.trim();
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            try {
                String unquoted = new Gson().fromJson(s, String.class);
                return TextUtils.isEmpty(unquoted) ? null : unquoted.trim();
            } catch (Exception e) {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        if (!s.startsWith("{")) {
            return s;
        }
        try {
            JsonObject o = new JsonParser().parse(s).getAsJsonObject();
            if (o.has("image") && !o.get("image").isJsonNull()) {
                return o.get("image").getAsString();
            }
            if (o.has("url") && !o.get("url").isJsonNull()) {
                return o.get("url").getAsString();
            }
            if (o.has("data") && !o.get("data").isJsonNull() && o.get("data").isJsonPrimitive()) {
                return o.get("data").getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 兼容旧调用：无 Activity / 无倒计时控件时仅发请求 */
    public static void getPhoneCode(String phone) {
        requestSmsGraphicFlow(null, phone, null, null, true, null);
    }

    /** 首次点「获取验证码」：validate 不传；type=1 弹图形验证；type=2 弹易盾 */
    public static void getPhoneCode(
            @Nullable FragmentActivity activity, String phone, @Nullable CountDownView countDownView) {
        requestSmsGraphicFlow(activity, phone, null, countDownView, true, null);
    }

    /** 用户在图形验证框内提交后调用：validate 为图中文字 */
    public static void submitSmsGraphicCaptcha(
            FragmentActivity activity,
            String phone,
            String graphicValidate,
            @Nullable CountDownView countDownView,
            GraphicCaptchaContinuation continuation) {
        if (continuation == null) {
            throw new IllegalArgumentException("continuation required");
        }
        requestSmsGraphicFlow(
                activity, phone, graphicValidate, countDownView, false, continuation);
    }

    private static boolean activityDead(@Nullable FragmentActivity activity) {
        if (activity == null) {
            return true;
        }
        if (activity.isFinishing()) {
            return true;
        }
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity.isDestroyed();
    }

    @Nullable
    private static FragmentActivity asFragmentActivity(@Nullable Context context) {
        Context c = context;
        while (c instanceof ContextWrapper) {
            if (c instanceof FragmentActivity) {
                return (FragmentActivity) c;
            }
            if (c instanceof Activity) {
                return null;
            }
            c = ((ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    private static void requestSmsGraphicFlow(
            @Nullable FragmentActivity activity,
            String phone,
            @Nullable String validate,
            @Nullable CountDownView countDownView,
            boolean releaseCountDownOnAnyFailure,
            @Nullable GraphicCaptchaContinuation continuation) {
        if (phone == null || phone.length() != 11) {
            ToastUtils.toastMsg("手机格式错误");
            if (countDownView != null) {
                countDownView.notifySmsRequestFailed();
            }
            return;
        }

        if (activity == null && countDownView != null) {
            activity = asFragmentActivity(countDownView.getContext());
        }

        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = phone;
        registerBean.validate = validate;

        final FragmentActivity act = activity;
        HttpUtil.apiW()
                .customer_smsCode(registerBean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call,
                                    Response<NetData> response,
                                    NetData body) {
                                SmsCodeResponse parsed = parseSmsCodeData(body.data);
                                String toast =
                                        !TextUtils.isEmpty(body.msg) ? body.msg : "发送成功";

                                if (parsed.type == SMS_DATA_TYPE_SENT) {
                                    ToastUtils.toastMsg(toast);
                                    if (countDownView != null) {
                                        countDownView.notifySmsSentSuccess();
                                    }
                                    if (continuation != null) {
                                        continuation.onSmsOk();
                                    }
                                    return;
                                }
                                if (parsed.type == SMS_DATA_TYPE_IMAGE_CAPTCHA) {
                                    if (TextUtils.isEmpty(parsed.image)) {
                                        ToastUtils.toastMsg("获取验证码图片失败");
                                        if (continuation == null && countDownView != null) {
                                            countDownView.notifySmsRequestFailed();
                                        }
                                        return;
                                    }
                                    if (continuation != null) {
                                        continuation.onReplaceCaptchaImage(parsed.image);
                                        return;
                                    }
                                    if (!activityDead(act)) {
                                        ImageCaptchaDialog.show(
                                                act.getSupportFragmentManager(),
                                                phone,
                                                parsed.image,
                                                countDownView);
                                    } else if (countDownView != null) {
                                        countDownView.notifySmsRequestFailed();
                                        ToastUtils.toastMsg("无法显示图形验证");
                                    }
                                    return;
                                }
                                if (parsed.type == SMS_DATA_TYPE_YIDUN) {
                                    if (activityDead(act)) {
                                        ToastUtils.toastMsg("无法显示行为验证");
                                        if (countDownView != null) {
                                            countDownView.notifySmsRequestFailed();
                                        }
                                        return;
                                    }
                                    showYiDunCaptcha(act, phone, countDownView);
                                    return;
                                }
                                ToastUtils.toastMsg(
                                        !TextUtils.isEmpty(body.msg)
                                                ? body.msg
                                                : "发送失败，请重试");
                                if (countDownView != null) {
                                    countDownView.notifySmsRequestFailed();
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                if (releaseCountDownOnAnyFailure && countDownView != null) {
                                    countDownView.notifySmsRequestFailed();
                                }
                                if (!releaseCountDownOnAnyFailure
                                        && continuation != null
                                        && t instanceof NetServerException
                                        && ((NetServerException) t).getErrCode() == 704) {
                                    String u =
                                            unwrapGraphicCaptchaUrl(
                                                    ((NetServerException) t).getExtraPayload());
                                    continuation.onReplaceCaptchaImage(u);
                                    if (TextUtils.isEmpty(u)) {
                                        ToastUtils.toastMsg("未获取到新验证码，请重试");
                                    }
                                }
                            }

                            @Override
                            public void end() {
                                super.end();
                            }
                        });
    }

    /** 弹出网易易盾行为验证；通过后把 validate 再提交给短信接口 */
    private static void showYiDunCaptcha(
            FragmentActivity activity, String phone, @Nullable CountDownView countDownView) {
        final boolean[] passed = {false};
        CaptchaConfiguration configuration =
                new CaptchaConfiguration.Builder()
                        .captchaId(YIDUN_CAPTCHA_ID)
                        .listener(
                                new CaptchaListener() {
                                    @Override
                                    public void onValidate(
                                            String result,
                                            String validate,
                                            String msg,
                                            String captchaType,
                                            String errorValidate) {
                                        if (!TextUtils.isEmpty(validate)) {
                                            passed[0] = true;
                                            // 易盾通过：validate 回传短信接口
                                            requestSmsGraphicFlow(
                                                    activity,
                                                    phone,
                                                    validate,
                                                    countDownView,
                                                    true,
                                                    null);
                                        }
                                    }

                                    @Override
                                    public void onError(int code, String msg) {
                                        if (passed[0]) {
                                            return;
                                        }
                                        ToastUtils.toastMsg(
                                                !TextUtils.isEmpty(msg) ? msg : "行为验证失败");
                                        if (countDownView != null) {
                                            countDownView.notifySmsRequestFailed();
                                        }
                                    }

                                    @Override
                                    public void onClose(Captcha.CloseType closeType) {
                                        // 验证成功关闭时不要重置按钮；用户主动关闭才恢复
                                        if (passed[0]
                                                || closeType == Captcha.CloseType.VERIFY_SUCCESS_CLOSE) {
                                            return;
                                        }
                                        if (closeType == Captcha.CloseType.USER_CLOSE
                                                && countDownView != null) {
                                            countDownView.notifySmsRequestFailed();
                                        }
                                    }

                                    @Override
                                    public void onCaptchaShow() {}
                                })
                        .build(activity);
        Captcha captcha = Captcha.getInstance();
        captcha.init(configuration);
        captcha.validate();
    }

    private static SmsCodeResponse parseSmsCodeData(Object data) {
        SmsCodeResponse r = new SmsCodeResponse();
        r.type = SMS_DATA_TYPE_SENT;
        if (data == null) {
            return r;
        }
        String s = data.toString().trim();
        if (s.isEmpty() || "{}".equals(s)) {
            return r;
        }
        try {
            JsonObject o = new JsonParser().parse(s).getAsJsonObject();
            SmsCodeResponse fromJson = new Gson().fromJson(o, SmsCodeResponse.class);
            if (fromJson == null) {
                return r;
            }
            if (!o.has("type")
                    && o.has("image")
                    && !o.get("image").isJsonNull()
                    && !TextUtils.isEmpty(o.get("image").getAsString())) {
                fromJson.type = SMS_DATA_TYPE_IMAGE_CAPTCHA;
            }
            return fromJson;
        } catch (Exception e) {
            return r;
        }
    }
}
