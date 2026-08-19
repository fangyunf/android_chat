package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
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
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    ActivityRegisterBinding binding;
    private boolean isAgree = true; // 默认选中

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityOtherPlaceLoginNav);
        binding.activityOtherPlaceLoginNav.addCloseImageButton().setOnClickListener(view -> finish());

        _initTfText();
        setupAgreementText();

        binding.activityRegisterAgreeLl.setOnClickListener(this);
        binding.activityRegisterBtn.setOnClickListener(this);
    }

    void _initTfText() {
        // 设置图标和提示文本
        binding.registerActivityPhoneTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_phone);
        binding.registerActivityCodeTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_code);
        binding.registerActivityUsernameTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password); // 使用人物图标，如果没有可以用其他图标
        binding.registerActivityPwdTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);
        binding.registerActivityPwd2Tf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);

        // 设置输入类型和提示文本
        binding.registerActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.registerActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.registerActivityPhoneTf.viewTitleTfCountEt.setHint("输入11位中国大陆手机*");
        binding.registerActivityCodeTf.viewTitleTfCountEt.setHint("输入验证码*");
        binding.registerActivityUsernameTf.viewTitleTfCountEt.setHint("输入用户名");
        binding.registerActivityPwdTf.viewTitleTfCountEt.setHint("输入您的密码*");
        binding.registerActivityPwd2Tf.viewTitleTfCountEt.setHint("再次输入您的密码*");

        // 显示密码可见性切换按钮
        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(true);

        // 显示验证码按钮
        binding.registerActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        CountDownView mCountDownView = binding.registerActivityCodeTf.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.registerActivityPhoneTf.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfCountEt);
                CommonNetUtil.getPhoneCode(RegisterActivity.this, phone, mCountDownView);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });
    }

    private void setupAgreementText() {
        String text = "我已阅读并接受《服务条款》《隐私政策》";
        SpannableString spannableString = new SpannableString(text);

        // 设置"《服务条款》"的样式和点击事件
        String serviceAgreement = "《服务条款》";
        int serviceStart = text.indexOf(serviceAgreement);
        int serviceEnd = serviceStart + serviceAgreement.length();

        if (serviceStart >= 0) {
            // 设置颜色（绿色）
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.app_theme_color)),
                    serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 设置点击事件
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                            .withParam("type", "2")
                            .withParam("title", "服务协议")
                            .withContext(RegisterActivity.this)
                            .navigate();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false); // 去掉下划线
                }
            }, serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 设置"《隐私政策》"的样式和点击事件
        String privacyPolicy = "《隐私政策》";
        int privacyStart = text.indexOf(privacyPolicy);
        int privacyEnd = privacyStart + privacyPolicy.length();

        if (privacyStart >= 0) {
            // 设置颜色（绿色）
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.app_theme_color)),
                    privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 设置点击事件
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                            .withParam("type", "1")
                            .withParam("title", "隐私政策")
                            .withContext(RegisterActivity.this)
                            .navigate();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false); // 去掉下划线
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 设置 TextView
        binding.activityRegisterAgreeTv.setText(spannableString);
        binding.activityRegisterAgreeTv.setMovementMethod(LinkMovementMethod.getInstance());
        binding.activityRegisterAgreeTv.setHighlightColor(Color.TRANSPARENT); // 移除点击时的背景色
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityRegisterAgreeLl) {
            // 点击整个布局时切换复选框状态
            isAgree = !isAgree;
            binding.activityRegisterIsCheckedIv.setSelected(isAgree);
        } else if (v == binding.registerActivityPwdTf.viewTitleTfCountEyeRl) {
            // 切换密码可见性
            binding.registerActivityPwdTf.viewTitleTfCountEyeIv.setSelected(!binding.registerActivityPwdTf.viewTitleTfCountEyeIv.isSelected());
            if (binding.registerActivityPwdTf.viewTitleTfCountEyeIv.isSelected()) {
                binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl) {
            // 切换确认密码可见性
            binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(!binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected());
            if (binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.isSelected()) {
                binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.activityRegisterBtn) {
            // 验证协议是否同意
            if (!isAgree) {
                ToastUtils.toastMsg("请先阅读并同意服务协议和隐私政策");
                return;
            }

            String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfCountEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.registerActivityCodeTf.viewTitleTfCountEt);
            if (code.length() > 6 || code.isEmpty()) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String username = getTextStr(binding.registerActivityUsernameTf.viewTitleTfCountEt);
            String pwd1 = getTextStr(binding.registerActivityPwdTf.viewTitleTfCountEt);
            String pwd2 = getTextStr(binding.registerActivityPwd2Tf.viewTitleTfCountEt);
            if (pwd1.isEmpty() || pwd2.isEmpty()) {
                ToastUtils.toastMsg("请输入密码");
                return;
            }
            if (!pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }

            // 获取性别（1=男，2=女）
            int gender = binding.rbMale.isChecked() ? 1 : 2;

            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = phone;
            registerBean.password = pwd1;
            registerBean.captcha = code;
            registerBean.deviceId = DeviceUtils.getDeviceId(this);
            registerBean.clientType = Constant.clientType;
            // 如果有用户名和性别字段，可以在这里设置
            // registerBean.username = username;
            // registerBean.gender = gender;

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
