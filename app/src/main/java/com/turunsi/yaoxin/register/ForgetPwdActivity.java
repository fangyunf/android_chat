package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
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
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityForgetPwdBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class ForgetPwdActivity extends BaseActivity implements View.OnClickListener {
    ActivityForgetPwdBinding binding;
    boolean isAgree = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPwdBinding.inflate(getLayoutInflater());
//        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        binding.tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.forgetPwdActivityIsAgreeLl.setOnClickListener(this);
        binding.registerActivityBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setContentView(binding.getRoot());

        // 设置协议文本的SpannableString
        setupAgreementText();

        _initTfText();
    }

    void _initTfText() {
        // 设置标题
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountTitleTv.setText("输入登录账号");
        binding.forgetPwdActivityCodeTf.viewTitleTfCountTitleTv.setText("输入验证码");
        binding.forgetPwdActivityPwdTf.viewTitleTfCountTitleTv.setText("请输入8-12位密码,数字+字母");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountTitleTv.setText("请再次输入8-12位密码,数字+字母");

        // 设置输入类型
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        // 设置提示文本（根据图1）
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setHint("");
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setHint("");
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setHint("");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt.setHint("");

        // 设置密码字段的可见性切换
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(true);
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // 设置验证码按钮
        binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        CountDownView mCountDownView = binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
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
                        .withContext(ForgetPwdActivity.this)
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
                        .withContext(ForgetPwdActivity.this)
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
        binding.forgetPwdActivityIsCheckedTxt.setText(spannableString);
        binding.forgetPwdActivityIsCheckedTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (v == binding.forgetPwdActivityIsAgreeLl) {
            binding.forgetPwdActivityIsCheckedIv.setSelected(!binding.forgetPwdActivityIsCheckedIv.isSelected());
            isAgree = binding.forgetPwdActivityIsCheckedIv.isSelected();
        } else if (v == binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl) {
            binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(!binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected());
            if (binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected()) {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeRl) {
            binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(!binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected());
            if (binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected()) {
                binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.forgetPwdActivityBtn) {
            if (!binding.forgetPwdActivityIsCheckedIv.isSelected()) {
                ToastUtils.toastMsg("请同意协议");
                return;
            }
            String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfCountEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.forgetPwdActivityPwdTf.viewTitleTfCountEt);
            String pwd2 = getTextStr(binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            RegisterBean bean = new RegisterBean();
            bean.password = pwd1;
            bean.phoneNo = phone;
            bean.captcha = code;

            Activity that = this;
            HttpUtil.apiW().customer_updatePassword(bean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg("修改成功");
                            finish();

                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
}
