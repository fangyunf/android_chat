package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityLoginBinding;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    private Handler handler;
    boolean isAgree = true;

    int _type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityMineLoginNav);
        binding.activityMineLoginNav.addCloseImageButton().setOnClickListener(view -> finish());

        binding.activityLoginForgetTv.setOnClickListener(this);
        binding.activityLoginRegisterTv.setOnClickListener(this);
        binding.activityLoginLoginTv.setOnClickListener(this);
        binding.activityLoginAgreeLl.setOnClickListener(this);
        binding.activityLoginIsCheckedIv.setSelected(true);

        styleInputField(binding.activityLoginTf1.getRoot(),
                binding.activityLoginTf1.viewTitleTfCountEt, "请输入账号");
        styleInputField(binding.activityLoginTf2.getRoot(),
                binding.activityLoginTf2.viewTitleTfCountEt, "请输入密保");
        styleInputField(binding.activityLoginTf3.getRoot(),
                binding.activityLoginTf3.viewTitleTfCountEt, "请输入密码");

        binding.activityLoginTf1.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.activityLoginTf2.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.activityLoginTf3.viewTitleTfCountIv.setVisibility(View.GONE);

        binding.activityLoginTf1.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.activityLoginTf2.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(true);
        binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.GONE);
        setupRegisterText();
        setupAgreementText();
        changeTitleWithType(0);
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

    private void setupRegisterText() {
        String text = "没有账号？现在去注册账号";
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf("现在去注册账号");
        if (start >= 0) {
            int end = text.length();
            spannableString.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.app_theme_color)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.activityLoginRegisterTv.setText(spannableString);
    }

    private void setupAgreementText() {
        String text = "已阅读并同意《隐私政策》和《用户服务协议》";
        SpannableString spannableString = new SpannableString(text);
        bindAgreementLink(spannableString, text, "《隐私政策》", "1", "隐私政策");
        bindAgreementLink(spannableString, text, "《用户服务协议》", "2", "用户服务协议");
        binding.activityLoginAgreeTv.setText(spannableString);
        binding.activityLoginAgreeTv.setMovementMethod(LinkMovementMethod.getInstance());
        binding.activityLoginAgreeTv.setHighlightColor(Color.TRANSPARENT);
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
                        .withContext(LoginActivity.this)
                        .navigate();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(LoginActivity.this, com.yaoxin.appbase.R.color.app_theme_color));
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    void changeTitleWithType(int type) {
        _type = type;
        if (type == 0) {
            binding.activityLoginTf2.getRoot().setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("登录");
            binding.activityLoginTitleTv.setText("欢迎回来");
            binding.activityLoginForgetTv.setText("忘记密码？");
            binding.activityLoginForgetTv.setVisibility(View.VISIBLE);
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            setupRegisterText();
        } else if (type == 1) {
            binding.activityLoginTf2.getRoot().setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("注册");
            binding.activityLoginTitleTv.setText("注册");
            binding.activityLoginRegisterTv.setText("已有账号，去登录");
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginForgetTv.setVisibility(View.GONE);
        } else if (type == 2) {
            binding.activityLoginTf2.getRoot().setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("找回密码");
            binding.activityLoginTitleTv.setText("找回密码");
            binding.activityLoginRegisterTv.setText("已有账号，去登录");
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginForgetTv.setVisibility(View.GONE);
        }
        if (BuildConfig.DEBUG) {
            binding.activityLoginTf1.viewTitleTfCountEt.setText("13761543036");
            binding.activityLoginTf3.viewTitleTfCountEt.setText("a1234567");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginAgreeLl) {
            isAgree = !isAgree;
            binding.activityLoginIsCheckedIv.setSelected(isAgree);
        } else if (v == binding.activityLoginTf3.viewTitleTfCountEyeRl) {
            binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(!binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected());
            if (binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected()) {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.activityLoginForgetTv) {
            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
        } else if (v == binding.activityLoginRegisterTv) {
            if (_type == 0) {
                RegisterActivity.start(RegisterActivity.class, this, null);
            } else {
                changeTitleWithType(0);
            }
        } else if (v == binding.activityLoginLoginTv) {
            if (!isAgree) {
                ToastUtils.toastMsg("请先阅读并同意隐私政策和用户服务协议");
                return;
            }
            if (_type == 0) {
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.isEmpty()) {
                    ToastUtils.toastMsg("请输入账号");
                    return;
                }
                String pwd = getTextStr(binding.activityLoginTf3.viewTitleTfCountEt);
                if (pwd.isEmpty()) {
                    ToastUtils.toastMsg("密码输入有误");
                    return;
                }
                RegisterBean bean = new RegisterBean();
                bean.phoneNo = phone;
                bean.password = pwd;
                Activity that = this;
                LoadingDialog.showDialog(getSupportFragmentManager(), "登陆中");
                HttpUtil.apiW().customer_login(bean).enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                        DataUtil.putUserInfo(userBean);
                        DataUtil.putToken(userBean.token);
                        DataUtil.addLoginUserInfoList(userBean);
                        IMUtil.loginIM(that, userBean.userId, userBean.imToken);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        if (t instanceof NetServerException) {
                            NetServerException exception = (NetServerException) t;
                            if (exception.getErrCode() == 601) {
                                HashMap map = new HashMap<>();
                                map.put("type", "0");
                                map.put("phone", phone);
                                OtherPlaceLoginActivity.start(OtherPlaceLoginActivity.class, that, map);
                            }
                        }
                    }

                    @Override
                    public void end() {
                        super.end();
                        LoadingDialog.dismissDialog();
                    }
                });
            } else if (_type == 1) {
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.isEmpty()) {
                    ToastUtils.toastMsg("请输入账号");
                    return;
                }
                String ans = getTextStr(binding.activityLoginTf2.viewTitleTfCountEt);
                if (ans.isEmpty()) {
                    ToastUtils.toastMsg("请输入密保");
                    return;
                }
                String pwd1 = getTextStr(binding.activityLoginTf3.viewTitleTfCountEt);
                if (pwd1.isEmpty()) {
                    ToastUtils.toastMsg("请输入密码");
                    return;
                }
                RegisterBean registerBean = new RegisterBean();
                registerBean.phoneNo = phone;
                registerBean.password = pwd1;
                registerBean.ans = ans;
                registerBean.deviceId = DeviceUtils.getDeviceId(this);
                registerBean.clientType = Constant.clientType;

                Activity that = this;
                LoadingDialog.showDialog(getSupportFragmentManager(), "注册中");

                HttpUtil.apiW().customer_registerZh(registerBean).enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                        DataUtil.putUserInfo(userBean);
                        DataUtil.putToken(userBean.token);
                        IMUtil.loginIM(that, userBean.userId, userBean.imToken);
                        SPUtils.getInstance().put("isRegister", true);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                        LoadingDialog.dismissDialog();
                    }
                });
            } else if (_type == 2) {
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.isEmpty()) {
                    ToastUtils.toastMsg("请输入账号");
                    return;
                }
                String ans = getTextStr(binding.activityLoginTf2.viewTitleTfCountEt);
                if (ans.isEmpty()) {
                    ToastUtils.toastMsg("请输入密保");
                    return;
                }
                String pwd1 = getTextStr(binding.activityLoginTf3.viewTitleTfCountEt);
                if (pwd1.isEmpty()) {
                    ToastUtils.toastMsg("请输入密码");
                    return;
                }
                RegisterBean bean = new RegisterBean();
                bean.password = pwd1;
                bean.phoneNo = phone;
                bean.ans = ans;

                HttpUtil.apiW().customer_updatePasswordZh(bean).enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg("修改成功");
                        changeTitleWithType(0);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.isRunningLoginView = false;
    }
}
