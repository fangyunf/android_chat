package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.utils.AppUtils;
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

//        binding.activityLoginLoginLl.setOnClickListener(this);
//        binding.activityLoginRegisterLl.setOnClickListener(this);
        binding.activityLoginForgetTv.setOnClickListener(this);
        binding.activityLoginRegisterTv.setOnClickListener(this);
        binding.activityLoginLoginTv.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        binding.activityLoginTf1.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_phone);
        binding.activityLoginTf2.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_code);
        binding.activityLoginTf3.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);
        binding.activityLoginTf4.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);
        binding.activityLoginTf1.viewTitleTfCountEt.setHint("输入手机号");
        binding.activityLoginTf2.viewTitleTfCountEt.setHint("输入验证码");
        binding.activityLoginTf3.viewTitleTfCountEt.setHint("输入密码");
        binding.activityLoginTf4.viewTitleTfCountEt.setHint("请再次输入密码");
        binding.activityLoginTf1.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityLoginTf2.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.activityLoginTf4.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);

        binding.activityLoginTf3.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.activityLoginTf4.viewTitleTfCountEyeRl.setOnClickListener(this);

        binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(true);
        binding.activityLoginTf4.viewTitleTfCountEyeIv.setSelected(true);

        binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.activityLoginTf4.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.layouLogin.setOnClickListener(this);
        binding.layouRegister.setOnClickListener(this);

        // 设置协议和隐私政策的 SpannableString
        setupAgreementText();

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
    }

    void changeTitleWithType(int type) {
        _type = type;
        if (type == 0) {

            binding.layoutBtnBg.setBackgroundResource(R.mipmap.bg_login_btn);

            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("登录");
            binding.activityLoginTitleTv.setText("登录");
            binding.activityLoginForgetTv.setText("忘记密码");
            binding.activityLoginForgetTv.setVisibility(View.VISIBLE);
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginTitleIv.setImageResource(R.mipmap.common_login_title_login);
            binding.viewLine.setVisibility(View.VISIBLE);
            binding.viewLine1.setVisibility(View.INVISIBLE);
            binding.tvVersion.setVisibility(View.GONE);
            binding.activityLoginTf4.viewRoundTfLl.setVisibility(View.GONE);
        } else if (type == 1) {
            binding.layoutBtnBg.setBackgroundResource(R.mipmap.bg_register_btn);
            binding.viewLine.setVisibility(View.INVISIBLE);
            binding.viewLine1.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTv.setText("注册");
            binding.activityLoginTitleTv.setText("注册");
            binding.activityLoginRegisterTv.setText("已有账号，去登录");
            binding.activityLoginRegisterTv.setVisibility(View.VISIBLE);
            binding.activityLoginForgetTv.setVisibility(View.GONE);
            binding.activityLoginTitleIv.setImageResource(R.mipmap.common_login_title_register);
            binding.tvVersion.setVisibility(View.GONE);
            binding.tvVersion.setText("版本号:" + AppUtils.getAppVersionName(this));
            binding.activityLoginTf4.viewRoundTfLl.setVisibility(View.VISIBLE);
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

        //我修改的
        binding.activityLoginRegisterTv.setVisibility(View.GONE);

        if (BuildConfig.DEBUG) {
            binding.activityLoginTf1.viewTitleTfCountEt.setText("13761543036");
            binding.activityLoginTf3.viewTitleTfCountEt.setText("a1234567");
        }
    }

    /**
     * 设置协议和隐私政策的 SpannableString
     */
    private void setupAgreementText() {
        String text = "登陆/注册即表示同意《服务协议》和《隐私政策》";
        SpannableString spannableString = new SpannableString(text);

        // 设置"《服务协议》"的样式和点击事件
        String serviceAgreement = "《服务协议》";
        int serviceStart = text.indexOf(serviceAgreement);
        int serviceEnd = serviceStart + serviceAgreement.length();

        if (serviceStart >= 0) {
            // 设置颜色和点击事件（无下划线）
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.color_8f55ff)),
                    serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                            .withParam("type", "2")
                            .withParam("title", "服务协议")
                            .withContext(LoginActivity.this)
                            .navigate();
                }

                @Override
                public void updateDrawState(@NonNull android.text.TextPaint ds) {
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
            // 设置颜色和点击事件（无下划线）
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, com.yaoxin.appbase.R.color.color_8f55ff)),
                    privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                            .withParam("type", "1")
                            .withParam("title", "隐私政策")
                            .withContext(LoginActivity.this)
                            .navigate();
                }

                @Override
                public void updateDrawState(@NonNull android.text.TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false); // 去掉下划线
                }
            }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 设置 TextView
        binding.activityLoginAgreeTv.setText(spannableString);
        binding.activityLoginAgreeTv.setMovementMethod(LinkMovementMethod.getInstance());
        binding.activityLoginAgreeTv.setHighlightColor(Color.TRANSPARENT); // 移除点击时的背景色
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginIsAgreeLl) {
            // 点击整个布局时切换复选框状态
            binding.activityLoginIsCheckedIv.setSelected(!binding.activityLoginIsCheckedIv.isSelected());
        } else if (v == binding.layouLogin) {
            changeTitleWithType(0);
        } else if (v == binding.layouRegister) {
            changeTitleWithType(1);
        } else if (v == binding.activityLoginTf3.viewTitleTfCountEyeRl) {
            binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(!binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected());
            if (binding.activityLoginTf3.viewTitleTfCountEyeIv.isSelected()) {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(null);

            }
        } else if (v == binding.activityLoginTf4.viewTitleTfCountEyeRl) {
            binding.activityLoginTf4.viewTitleTfCountEyeIv.setSelected(!binding.activityLoginTf4.viewTitleTfCountEyeIv.isSelected());
            if (binding.activityLoginTf4.viewTitleTfCountEyeIv.isSelected()) {
                binding.activityLoginTf4.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.activityLoginTf4.viewTitleTfCountEt.setTransformationMethod(null);
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
//            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
            if (_type == 0) {
                changeTitleWithType(1);
            } else {
                changeTitleWithType(0);
            }
        } else if (v == binding.activityLoginLoginTv) {

            if (_type == 0) {
//                if (!binding.activityLoginIsCheckedIv.isSelected()) {
//                    ToastUtils.toastMsg("请同意协议");
//                    return;
//                }
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String pwd = getTextStr(binding.activityLoginTf3.viewTitleTfCountEt);
                if (TextUtils.isEmpty(pwd)) {
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
//                if (!binding.activityLoginIsCheckedIv.isSelected()) {
//                    ToastUtils.toastMsg("请同意协议");
//                    return;
//                }
                String phone = getTextStr(binding.activityLoginTf1.viewTitleTfCountEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String code = getTextStr(binding.activityLoginTf2.viewTitleTfCountEt);
                if (TextUtils.isEmpty(code) || code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
                    return;
                }
                String pwd1 = getTextStr(binding.activityLoginTf3.viewTitleTfCountEt);
                if (pwd1.isEmpty()) {
                    ToastUtils.toastMsg("请输入密码");
                    return;
                }

                String newPwd = getTextStr(binding.activityLoginTf4.viewTitleTfCountEt);
                if (TextUtils.isEmpty(newPwd)) {
                    ToastUtils.toastMsg("确认密码输入有误");
                    return;
                }
                if (!(pwd1.equals(newPwd))) {
                    ToastUtils.toastMsg("两次密码不一致");
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

                HttpUtil.apiW().customer_register(registerBean).enqueue(new CommonCallback<NetData>() {
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
                HttpUtil.apiW().customer_updatePassword(bean).enqueue(new CommonCallback<NetData>() {
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
