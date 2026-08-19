package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.yunxin.kit.alog.ALog;
import com.yaoxin.appbase.R;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class CommonNetUtil {
    public static void getPhoneCode(Activity activity, String phone) {
        getPhoneCode(activity, phone, null);
    }

    public static void getPhoneCode(Activity activity, String phone, CountDownView countDownView) {
        if (phone.length() != 11) {
            ToastUtils.toastMsg("手机格式错误");
            return;
        }
        requestPhoneCode(
                activity,
                phone,
                null,
                countDownView,
                new SmsCodeResultListener() {
                    @Override
                    public void onNeedCaptcha(String imageUrl, String msg) {
                        if (countDownView != null) {
                            countDownView.resetCountDown();
                        }
                        showImageCaptchaDialog(activity, phone, imageUrl, countDownView);
                    }

                    @Override
                    public void onSmsSent(String msg) {
                        if (countDownView != null && !countDownView.isCounting()) {
                            countDownView.startCountDown();
                        }
                        ToastUtils.toastMsg(TextUtils.isEmpty(msg) ? "发送成功" : msg);
                    }
                });
    }

    private static void requestPhoneCode(
            Activity activity,
            String phone,
            String validate,
            CountDownView countDownView,
            SmsCodeResultListener listener) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = phone;
        if (!TextUtils.isEmpty(validate)) {
            registerBean.validate = validate;
        }
        HttpUtil.apiW().customer_smsCode(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        CaptchaData captchaData = parseCaptchaData(body);
                        if (captchaData.type == 1 && !TextUtils.isEmpty(captchaData.image)) {
                            if (listener != null) {
                                listener.onNeedCaptcha(captchaData.image, body.msg);
                            }
                            return;
                        }
                        if (listener != null) {
                            listener.onSmsSent(body.msg);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                    }
                });
    }

    private static void showImageCaptchaDialog(
            Activity activity, String phone, String imageUrl, CountDownView countDownView) {
        if (activity == null || activity.isFinishing()) {
            ToastUtils.toastMsg("请重试");
            return;
        }
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, 16);
        root.setPadding(padding, padding, padding, padding / 2);

        ImageView imageView = new ImageView(activity);
        LinearLayout.LayoutParams imageLp =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 110));
        imageView.setLayoutParams(imageLp);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(imageView);

        EditText editText = new EditText(activity);
        editText.setHint("请输入图形验证码");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setSingleLine(true);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editText.setBackgroundResource(R.drawable.bg_gray_rounded_line_10);
        int inputPadding = dp(activity, 12);
        editText.setPadding(inputPadding, inputPadding, inputPadding, inputPadding);
        LinearLayout.LayoutParams inputLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputLp.topMargin = dp(activity, 12);
        editText.setLayoutParams(inputLp);
        root.addView(editText);

        Glide.with(activity).load(imageUrl).into(imageView);

        AlertDialog dialog =
                new AlertDialog.Builder(activity)
                        .setTitle("图形验证码")
                        .setView(root)
                        .setCancelable(false)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", null)
                        .create();
        dialog.setOnShowListener(
                d -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener(
                                    v -> {
                                        String code = editText.getText().toString().trim();
                                        if (TextUtils.isEmpty(code)) {
                                            ToastUtils.toastMsg("请输入图形验证码");
                                            return;
                                        }
                                        requestPhoneCode(
                                                activity,
                                                phone,
                                                code,
                                                countDownView,
                                                new SmsCodeResultListener() {
                                                    @Override
                                                    public void onNeedCaptcha(
                                                            String nextImageUrl, String msg) {
                                                        if (!TextUtils.isEmpty(msg)) {
                                                            ToastUtils.toastMsg(msg);
                                                        }
                                                        editText.setText("");
                                                        Glide.with(activity)
                                                                .load(nextImageUrl)
                                                                .into(imageView);
                                                    }

                                                    @Override
                                                    public void onSmsSent(String msg) {
                                                        dialog.dismiss();
                                                        if (countDownView != null
                                                                && !countDownView.isCounting()) {
                                                            countDownView.startCountDown();
                                                        }
                                                        ToastUtils.toastMsg(
                                                                TextUtils.isEmpty(msg)
                                                                        ? "发送成功"
                                                                        : msg);
                                                    }
                                                });
                                    });
                });

        imageView.setOnClickListener(
                v ->
                        requestPhoneCode(
                                activity,
                                phone,
                                null,
                                countDownView,
                                new SmsCodeResultListener() {
                                    @Override
                                    public void onNeedCaptcha(String nextImageUrl, String msg) {
                                        Glide.with(activity).load(nextImageUrl).into(imageView);
                                    }

                                    @Override
                                    public void onSmsSent(String msg) {
                                        dialog.dismiss();
                                        if (countDownView != null && !countDownView.isCounting()) {
                                            countDownView.startCountDown();
                                        }
                                        ToastUtils.toastMsg(
                                                TextUtils.isEmpty(msg) ? "发送成功" : msg);
                                    }
                                }));
        dialog.show();
    }

    private static CaptchaData parseCaptchaData(NetData body) {
        CaptchaData result = new CaptchaData();
        try {
            String dataStr = String.valueOf(body.data);
            JsonObject jsonObject = new JsonParser().parse(dataStr).getAsJsonObject();
            if (jsonObject.has("type") && !jsonObject.get("type").isJsonNull()) {
                result.type = jsonObject.get("type").getAsInt();
            }
            if (jsonObject.has("image") && !jsonObject.get("image").isJsonNull()) {
                result.image = jsonObject.get("image").getAsString();
            }
        } catch (Exception ignore) {
        }
        return result;
    }

    private static int dp(Activity activity, int value) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        activity.getResources().getDisplayMetrics());
    }

    private static class CaptchaData {
        int type;
        String image;
    }

    private interface SmsCodeResultListener {
        void onNeedCaptcha(String imageUrl, String msg);

        void onSmsSent(String msg);
    }
}
