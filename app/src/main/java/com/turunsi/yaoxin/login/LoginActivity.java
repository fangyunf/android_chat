package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
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

        binding.activityLoginLoginLl.setOnClickListener(this);
        binding.activityLoginRegisterLl.setOnClickListener(this);
        binding.activityLoginForgetTv.setOnClickListener(this);
        binding.activityLoginLoginTv.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt2.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt4.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        binding.activityLoginTf1.viewRoundTfEt.setHint("输入手机号");
        binding.activityLoginTf2.viewRoundTfEt.setHint("输入验证码");
        binding.activityLoginTf3.viewRoundTfEt.setHint("输入密码");
        binding.activityLoginTf1.viewRoundTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityLoginTf2.viewRoundTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        CountDownView mCountDownView = binding.activityLoginTf2.viewRoundTfBtnCaptcha;
        mCountDownView.setUserEdit(binding.activityLoginTf1.viewRoundTfEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.activityLoginTf1.viewRoundTfEt);
                CommonNetUtil.getPhoneCode(phone);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });
        changeTitleWithType(0);
        if (BuildConfig.DEBUG) {

        binding.activityLoginTf1.viewRoundTfEt.setText("18616821287");
        binding.activityLoginTf3.viewRoundTfEt.setText("12345678a");
        }
    }

    void changeTitleWithType(int type) {
        _type = type;

        if (type == 0) {
            ViewGroup.LayoutParams lp = binding.activityLoginSwitchLl.getLayoutParams();
            lp.height = SizeUtils.dp2px(60);
            binding.activityLoginSwitchLl.setLayoutParams(lp);
            binding.activityLoginSwitchLl.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTitleLineView.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTitleTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_color));
            binding.activityLoginRegisterTitleTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_aplha_color));
            binding.activityLoginLoginTitleTv.setTextSize(16);
            binding.activityLoginRegisterTitleTv.setTextSize(14);
            binding.activityLoginRegisterTitleLineView.setVisibility(View.GONE);
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.GONE);
            binding.activityLoginLoginTv.setText("登录");
            binding.activityLoginForgetTv.setText("忘记密码");
            binding.activityLoginForgetTv.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            ViewGroup.LayoutParams lp = binding.activityLoginSwitchLl.getLayoutParams();
            lp.height = SizeUtils.dp2px(60);
            binding.activityLoginSwitchLl.setLayoutParams(lp);
            binding.activityLoginSwitchLl.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTitleLineView.setVisibility(View.GONE);
            binding.activityLoginRegisterTitleTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_color));
            binding.activityLoginLoginTitleTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_aplha_color));
            binding.activityLoginLoginTitleTv.setTextSize(14);
            binding.activityLoginRegisterTitleTv.setTextSize(16);
            binding.activityLoginRegisterTitleLineView.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewRoundTfBtnCaptcha.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTv.setText("注册");
            binding.activityLoginForgetTv.setVisibility(View.GONE);
        } else if (type == 2) {
            ViewGroup.LayoutParams lp = binding.activityLoginSwitchLl.getLayoutParams();
            lp.height = 0;
            binding.activityLoginSwitchLl.setLayoutParams(lp);
            binding.activityLoginTf2.viewRoundTfLl.setVisibility(View.VISIBLE);
            binding.activityLoginTf2.viewRoundTfBtnCaptcha.setVisibility(View.VISIBLE);
            binding.activityLoginLoginTv.setText("找回密码");
            binding.activityLoginForgetTv.setVisibility(View.GONE);
            binding.activityLoginForgetTv.setText("返回登录");
            binding.activityLoginForgetTv.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginIsAgreeLl) {
            binding.activityLoginIsCheckedIv.setSelected(!binding.activityLoginIsCheckedIv.isSelected());
        }  else if (v == binding.activityLoginIsCheckedTxt2) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                .withParam("type","2")
                .withParam("title","服务协议")
                .withContext(this)
                .navigate();
        }  else if (v == binding.activityLoginIsCheckedTxt4) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                    .withParam("type","1")
                    .withParam("title","隐私政策")
                    .withContext(this)
                    .navigate();
        }  else if (v == binding.activityLoginForgetTv) {
//            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
            changeTitleWithType(_type == 0 ? 2 : 0);
        } else if (v == binding.activityLoginLoginLl) {
//            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
            changeTitleWithType(0);
        } else if (v == binding.activityLoginRegisterLl) {
//            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
            changeTitleWithType(1);
        } else if (v == binding.activityLoginLoginTv) {

            if (_type == 0) {

                if (!binding.activityLoginIsCheckedIv.isSelected()) {
                    ToastUtils.toastMsg("请同意协议");
                    return;
                }
                String phone = getTextStr(binding.activityLoginTf1.viewRoundTfEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String pwd = getTextStr(binding.activityLoginTf3.viewRoundTfEt);
                if (pwd.isEmpty()) {
                    ToastUtils.toastMsg("密码输入有误");
                    return;
                }
                RegisterBean bean = new RegisterBean();
                bean.phoneNo = phone;
                bean.password = pwd;
                Activity that = this;
                LoadingDialog.showDialog(getSupportFragmentManager(),"登陆中");
                HttpUtil.apiW().customer_login(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                UserBean userBean = new Gson().fromJson((String) body.data,UserBean.class);
                                DataUtil.putUserInfo(userBean);
                                DataUtil.putToken(userBean.token);
                                DataUtil.addLoginUserInfoList(userBean);
                                IMUtil.loginIM(that,userBean.userId,userBean.imToken);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                if (t instanceof NetServerException) {

                                    NetServerException exception = (NetServerException) t;
                                    if (exception.getErrCode() == 601) {

                                        HashMap map = new HashMap<>();
                                        map.put("type","0");
                                        map.put("phone",phone);
                                        OtherPlaceLoginActivity.start(OtherPlaceLoginActivity.class,that,map);
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
                String phone = getTextStr(binding.activityLoginTf1.viewRoundTfEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String code = getTextStr(binding.activityLoginTf2.viewRoundTfEt);
                if (code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
                    return;
                }
                String pwd1 = getTextStr(binding.activityLoginTf3.viewRoundTfEt);
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
                HttpUtil.apiW().customer_register(registerBean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                UserBean userBean = new Gson().fromJson((String) body.data,UserBean.class);
                                DataUtil.putUserInfo(userBean);
                                DataUtil.putToken(userBean.token);
                                IMUtil.loginIM(that,userBean.userId,userBean.imToken);

                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            } else if (_type == 2) {
                String phone = getTextStr(binding.activityLoginTf1.viewRoundTfEt);
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                String code = getTextStr(binding.activityLoginTf2.viewRoundTfEt);
                if (code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
                    return;
                }
                String pwd1 = getTextStr(binding.activityLoginTf3.viewRoundTfEt);
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
