package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityRegisterBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private Handler handler;
    ActivityRegisterBinding binding;
    boolean isAgree = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerActivityBackIv.setOnClickListener(this);
        binding.registerActivityIsAgreeLl.setOnClickListener(this);

        // 设置协议文本的SpannableString
        setupAgreementText();
        // 初始化协议复选框为选中状态
        _initTfText();
    }

    void _initTfText() {
        // 设置标题
        binding.registerActivityPhoneTf.viewTitleTfCountTitleTv.setText("输入登录账号");
        binding.registerActivityPwdTf.viewTitleTfCountTitleTv.setText("输入登录密码");
        binding.registerActivityPwd2Tf.viewTitleTfCountTitleTv.setText("再次输入密码");
        binding.registerActivityCodeTf.viewTitleTfCountTitleTv.setText("输入验证码");

        // 设置输入类型
        binding.registerActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.registerActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        // 设置提示文本（根据图2）
        binding.registerActivityPhoneTf.viewTitleTfCountEt.setHint("");
        binding.registerActivityCodeTf.viewTitleTfCountEt.setHint("");
        binding.registerActivityPwdTf.viewTitleTfCountEt.setHint("");
        binding.registerActivityPwd2Tf.viewTitleTfCountEt.setHint("");

        binding.activityRegisterBtn.setOnClickListener(this);

        // 设置密码字段的可见性切换
        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);
        binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(true);
        binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // 设置验证码按钮
        binding.registerActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        CountDownView mCountDownView = binding.registerActivityCodeTf.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.registerActivityPhoneTf.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfCountEt);
                CommonNetUtil.getPhoneCode(phone);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });
    }

    /**
     * 设置协议文本的SpannableString，使《用户协议》和《隐私政策》可点击并显示为绿色
     */
    private void setupAgreementText() {
        String text = "我已阅读并同意《用户协议》、《隐私政策》";
        SpannableString spannableString = new SpannableString(text);

        // 获取主题绿色
        int greenColor = getResources().getColor(com.yaoxin.appbase.R.color.app_theme_color);

        // 设置《用户协议》的颜色和点击事件
        int userAgreementStart = text.indexOf("《用户协议》");
        int userAgreementEnd = userAgreementStart + "《用户协议》".length();
        spannableString.setSpan(new ForegroundColorSpan(greenColor),
                userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                        .withParam("type", "2")
                        .withParam("title", "用户协议")
                        .withContext(RegisterActivity.this)
                        .navigate();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(greenColor);
                ds.setUnderlineText(false);
            }
        }, userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置《隐私政策》的颜色和点击事件
        int privacyPolicyStart = text.indexOf("《隐私政策》");
        int privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length();
        spannableString.setSpan(new ForegroundColorSpan(greenColor),
                privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                        .withParam("type", "1")
                        .withParam("title", "隐私政策")
                        .withContext(RegisterActivity.this)
                        .navigate();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(greenColor);
                ds.setUnderlineText(false);
            }
        }, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置TextView
        binding.registerActivityIsCheckedTxt.setText(spannableString);
        binding.registerActivityIsCheckedTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (v == binding.registerActivityBackIv) {
            finish();
        } else if (v == binding.registerActivityIsAgreeLl) {
            binding.registerActivityIsCheckedIv.setSelected(!binding.registerActivityIsCheckedIv.isSelected());
            isAgree = binding.registerActivityIsCheckedIv.isSelected();
        } else if (v == binding.registerActivityPwdTf.viewTitleTfCountEyeRl) {
            binding.registerActivityPwdTf.viewTitleTfCountEyeIv.setSelected(!binding.registerActivityPwdTf.viewTitleTfCountEyeIv.isSelected());
            if (binding.registerActivityPwdTf.viewTitleTfCountEyeIv.isSelected()) {
                binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl) {
            binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(!binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected());
            if (binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected()) {
                binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.activityRegisterBtn) {
            if (!binding.registerActivityIsCheckedIv.isSelected()) {
                ToastUtils.toastMsg("请同意协议");
                return;
            }

            String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfCountEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.registerActivityCodeTf.viewTitleTfCountEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.registerActivityPwdTf.viewTitleTfCountEt);
            String pwd2 = getTextStr(binding.registerActivityPwd2Tf.viewTitleTfCountEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = phone;
            registerBean.password = pwd1;
            registerBean.captcha = code;
            registerBean.deviceId = DeviceUtils.getDeviceId(this);
            registerBean.clientType = Constant.clientType;

            Activity that = this;
            HttpUtil.apiW().customer_register(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                            DataUtil.putUserInfo(userBean);
                            DataUtil.putToken(userBean.token);
                            IMUtil.loginIM(that, userBean.userId, userBean.imToken);

                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
}
