package com.yaoxin.appbase.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class CommonNetUtil {
    /** 与短信接口 data 中 type 一致：0 已发短信；1 图形验证；2 网易盾（暂未接入） */
    public static final int SMS_DATA_TYPE_SENT = 0;
    public static final int SMS_DATA_TYPE_IMAGE_CAPTCHA = 1;
    public static final int SMS_DATA_TYPE_YIDUN = 2;

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
            JsonObject o = JsonParser.parseString(s).getAsJsonObject();
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

    /** 首次点「获取验证码」：validate 不传；服务端 type=1 时弹图形验证框 */
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

        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = phone;
        registerBean.validate = validate;

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
                                    if (!activityDead(activity)) {
                                        ImageCaptchaDialog.show(
                                                activity.getSupportFragmentManager(),
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
                                    ToastUtils.toastMsg("当前需行为验证，请稍后再试或使用新版客户端");
                                    if (countDownView != null) {
                                        countDownView.notifySmsRequestFailed();
                                    }
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
            JsonObject o = JsonParser.parseString(s).getAsJsonObject();
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
