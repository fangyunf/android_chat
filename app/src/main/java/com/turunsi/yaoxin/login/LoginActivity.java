package com.turunsi.yaoxin.login;

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
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityLoginBinding;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.turunsi.yaoxin.fragment.OtherPlaceLoginFragment;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    private Handler handler;
    boolean isAgree = false;

    int _type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.setStatusBarLightMode(this, true, true);

        binding.activityLoginForgetTv.setOnClickListener(this);
        binding.activityLoginRegisterTv.setOnClickListener(this);
        binding.activityLoginLoginTv.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);

        // 设置协议文本的SpannableString
        setupAgreementText();
        binding.activityLoginTf1.viewTitleTfCountTitleTv.setText("手机号");
        binding.activityLoginTf2.viewTitleTfCountTitleTv.setText("验证码");
        binding.activityLoginTf3.viewTitleTfCountTitleTv.setText("密码");
        binding.activityLoginTf1.viewTitleTfCountEt.setHint("输入登录账号");
        binding.activityLoginTf2.viewTitleTfCountEt.setHint("输入验证码");
        binding.activityLoginTf3.viewTitleTfCountEt.setHint("输入登录密码");
        binding.activityLoginTf1.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityLoginTf2.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(true);
        binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());


        CountDownView mCountDownView = binding.activityLoginTf2.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.activityLoginTf1.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                CommonNetUtil.getPhoneCode(phone);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });
        changeTitleWithType(0);

        if (BuildConfig.DEBUG) {
//            binding.activityLoginTf1.viewTitleTfCountEt.setText("18616821287");
//            binding.activityLoginTf3.viewTitleTfCountEt.setText("a1234567");

            binding.activityLoginTf1.viewTitleTfCountEt.setText("13100000001");
            binding.activityLoginTf3.viewTitleTfCountEt.setText("shaoli111");
        }
    }

    void changeTitleWithType(int type) {
        _type = type;
        if (type == 0) {
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("登录");
            binding.activityLoginTitleTv.setText("登录");
            binding.activityLoginForgetTv.setText("忘记密码");
            binding.activityLoginForgetTv.setVisibility(View.VISIBLE);
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginTitleIv.setImageResource(R.mipmap.common_login_title_login);
        } else if (type == 1) {
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTv.setText("注册");
            binding.activityLoginTitleTv.setText("注册");
            binding.activityLoginRegisterTv.setText("已有账号，去登录");
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginForgetTv.setVisibility(View.GONE);
            binding.activityLoginTitleIv.setImageResource(R.mipmap.common_login_title_register);
        } else if (type == 2) {
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTv.setText("找回密码");
            binding.activityLoginTitleTv.setText("找回密码");
            binding.activityLoginForgetTv.setVisibility(View.GONE);
            binding.activityLoginRegisterTv.setText("已有账号，去登录");
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginForgetTv.setVisibility(View.GONE);
            binding.activityLoginTitleIv.setImageResource(R.mipmap.common_login_title_forget);
        }
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
                        .withContext(LoginActivity.this)
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
                        .withContext(LoginActivity.this)
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
        binding.activityLoginIsCheckedTxt.setText(spannableString);
        binding.activityLoginIsCheckedTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginIsAgreeLl) {
            binding.activityLoginIsCheckedIv.setSelected(!binding.activityLoginIsCheckedIv.isSelected());
            isAgree = binding.activityLoginIsCheckedIv.isSelected();
        } else if (v == binding.activityLoginTf3.viewTitleTfCountEyeRl) {
            binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(!binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected());
            if (binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected()) {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(null);

            }
        } else if (v == binding.activityLoginForgetTv) {
            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
//            if (_type == 0) {
//                changeTitleWithType(2);
//            } else if (_type == 1 || _type == 2) {
//                changeTitleWithType(0);
//            }
//            changeTitleWithType(_type == 0 ? 2 : 0);
        } else if (v == binding.activityLoginRegisterTv) {
            RegisterActivity.start(RegisterActivity.class, this, null);
//            if (_type == 0) {
//                changeTitleWithType(1);
//            } else {
//                changeTitleWithType(0);
//            }
        } else if (v == binding.activityLoginLoginTv) {

            if (_type == 0) {

                if (!binding.activityLoginIsCheckedIv.isSelected()) {
                    ToastUtils.toastMsg("请同意协议");
                    return;
                }
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
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
                HttpUtil.apiW().customer_login(bean)
                        .enqueue(new CommonCallback<NetData>() {
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
//                                        OtherPlaceLoginFragment fragment = new OtherPlaceLoginFragment();
//                                        fragment.showNow(getSupportFragmentManager(),"OtherPlaceLoginFragment");
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
                if (!binding.activityLoginIsCheckedIv.isSelected()) {
                    ToastUtils.toastMsg("请同意协议");
                    return;
                }
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String code = getTextStr(binding.activityLoginTf2.viewTitleTfCountEt);
                if (code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
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
                registerBean.captcha = code;
                registerBean.deviceId = DeviceUtils.getDeviceId(this);
                registerBean.clientType = Constant.clientType;

                Activity that = this;
                LoadingDialog.showDialog(getSupportFragmentManager(), "注册中");

                HttpUtil.apiW().customer_register(registerBean)
                        .enqueue(new CommonCallback<NetData>() {
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
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String code = getTextStr(binding.activityLoginTf2.viewTitleTfCountEt);
                if (code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
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
                bean.captcha = code;

                Activity that = this;
                HttpUtil.apiW().customer_updatePassword(bean)
                        .enqueue(new CommonCallback<NetData>() {
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

//            Intent intent = new Intent();
//            intent.setClass(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            this.startActivity(intent);
//            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.isRunningLoginView = false;
    }
}
