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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

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
        setupTitleText();
        setupAgreementText();
        binding.activityRegisterIsCheckedIv.setSelected(true);

        binding.activityRegisterAgreeLl.setOnClickListener(this);
        binding.activityRegisterBtn.setOnClickListener(this);
    }

    private void styleInputField(View root, EditText et, String hint) {
        root.setBackgroundResource(R.drawable.bg_input_stroke_capsule);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
        if (lp != null) {
            lp.leftMargin = 0;
            lp.rightMargin = 0;
            root.setLayoutParams(lp);
        }
        root.setPadding(dp(16), 0, dp(12), 0);
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            if (vg.getChildCount() > 1) {
                vg.getChildAt(1).setVisibility(View.GONE);
            }
            View row = vg.getChildAt(0);
            if (row instanceof LinearLayout) {
                ((LinearLayout) row).setPadding(0, 0, 0, 0);
            }
        }
        ViewGroup.MarginLayoutParams etLp = (ViewGroup.MarginLayoutParams) et.getLayoutParams();
        if (etLp != null) {
            etLp.leftMargin = 0;
            et.setLayoutParams(etLp);
        }
        et.setHint(hint);
        et.setHintTextColor(Color.parseColor("#BBBBBB"));
        et.setTextSize(15);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    void _initTfText() {
        styleInputField(binding.registerActivityPhoneTf.getRoot(),
                binding.registerActivityPhoneTf.viewTitleTfCountEt, "请输入账号");
        styleInputField(binding.registerActivityCodeTf.getRoot(),
                binding.registerActivityCodeTf.viewTitleTfCountEt, "请输入密保");
        styleInputField(binding.registerActivityUsernameTf.getRoot(),
                binding.registerActivityUsernameTf.viewTitleTfCountEt, "请输入昵称");
        styleInputField(binding.registerActivityPwdTf.getRoot(),
                binding.registerActivityPwdTf.viewTitleTfCountEt, "请输入密码");
        styleInputField(binding.registerActivityPwd2Tf.getRoot(),
                binding.registerActivityPwd2Tf.viewTitleTfCountEt, "请确认密码");

        binding.registerActivityPhoneTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.registerActivityCodeTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.registerActivityUsernameTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.registerActivityPwdTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.registerActivityPwd2Tf.viewTitleTfCountIv.setVisibility(View.GONE);

        binding.registerActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.registerActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.registerActivityCodeTf.viewTitleTfCountEt.setTransformationMethod(null);
        binding.registerActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.registerActivityPwd2Tf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.registerActivityPwd2Tf.viewTitleTfCountEyeIv.setSelected(true);

        // 密保：隐藏获取验证码
        binding.registerActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.GONE);
    }

    private void setupTitleText() {
        String text = "欢迎进入昌盛";
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf("昌盛");
        if (start >= 0) {
            spannableString.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.app_theme_color)),
                    start, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.activityRegisterTitleTv.setText(spannableString);
    }

    private void setupAgreementText() {
        String text = "已阅读并同意《隐私政策》和《用户服务协议》";
        SpannableString spannableString = new SpannableString(text);

        bindAgreementLink(spannableString, text, "《隐私政策》", "1", "隐私政策");
        bindAgreementLink(spannableString, text, "《用户服务协议》", "2", "用户服务协议");

        binding.activityRegisterAgreeTv.setText(spannableString);
        binding.activityRegisterAgreeTv.setMovementMethod(LinkMovementMethod.getInstance());
        binding.activityRegisterAgreeTv.setHighlightColor(Color.TRANSPARENT);
    }

    private void bindAgreementLink(SpannableString spannableString, String text, String link,
                                   String type, String title) {
        int start = text.indexOf(link);
        if (start < 0) {
            return;
        }
        int end = start + link.length();
        spannableString.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.app_theme_color)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                        .withParam("type", type)
                        .withParam("title", title)
                        .withContext(RegisterActivity.this)
                        .navigate();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(RegisterActivity.this, com.yaoxin.appbase.R.color.app_theme_color));
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                ToastUtils.toastMsg("请先阅读并同意隐私政策和用户服务协议");
                return;
            }

            String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfCountEt);
            if (phone.isEmpty()) {
                ToastUtils.toastMsg("请输入账号");
                return;
            }
            String ans = getTextStr(binding.registerActivityCodeTf.viewTitleTfCountEt);
            if (ans.isEmpty()) {
                ToastUtils.toastMsg("请输入密保");
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
            registerBean.ans = ans;
            registerBean.deviceId = DeviceUtils.getDeviceId(this);
            registerBean.clientType = Constant.clientType;

            Activity that = this;
            HttpUtil.apiW().customer_registerZh(registerBean)
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
