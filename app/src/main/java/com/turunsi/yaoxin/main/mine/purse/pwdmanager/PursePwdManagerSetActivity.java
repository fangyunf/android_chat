package com.turunsi.yaoxin.main.mine.purse.pwdmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerSetBinding;
import com.turunsi.yaoxin.login.WelcomeLoginActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class PursePwdManagerSetActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePursePwdManagerSetBinding binding;
    //0:设置支付密码  1：修改支付密码  2：忘记支付密码 100：确认注销
    private int type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePursePwdManagerSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePursePwdManagerSetNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePursePwdManagerSetForgetPwdTv.setOnClickListener(this);
        binding.activityMineAddressAddSaveRl.setOnClickListener(this);
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        binding.activityMinePursePwdManagerSetNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityMinePursePwdManagerSetNav.setLayoutParams(params);
        binding.activityMinePursePwdManagerSetNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);

        if (extras != null) {
            String typeString = (String) extras.get("type");
            type = Integer.parseInt(typeString);
        }

        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgTv.setText("验证码");
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt.setHint("请输入验证码");
        binding.activityMinePursePwdManagerGetCode.btnCaptcha.setVisibility(View.VISIBLE);
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_NUMBER);

        CountDownView mCountDownView = binding.activityMinePursePwdManagerGetCode.btnCaptcha;
        mCountDownView.setUserEdit(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(
                new LoginLoader.CaptchaListener() {
                    @Override
                    public void onPre() {
                        String phone =
                                getTextStr(
                                        binding.activityMinePursePwdManagerSetPhone
                                                .viewTitleTfWithoutBgEt);
                        if (type == 1) {
                            // 修改支付密码：首行是原密码，用登录手机号取验证码
                            UserBean user = DataUtil.getUserInfo();
                            if (user != null) {
                                if (!TextUtils.isEmpty(user.phoneNo)) {
                                    phone = user.phoneNo;
                                } else if (!TextUtils.isEmpty(user.phone)) {
                                    phone = user.phone;
                                }
                            }
                            mCountDownView.needVerify = false;
                        }
                        CommonNetUtil.getPhoneCode(phone);
                    }

                    @Override
                    public void onComplete(String phoneOrEmail) {}
                });

        if (type == 0) {
            _initSetCell();
            binding.activityMinePursePwdManagerSetNav.setTitle("设置支付密码");
        } else if (type == 1) {
            binding.activityMinePursePwdManagerSetNav.setTitle("修改支付密码");
            _initModifyCell();
            binding.activityMinePursePwdManagerSetForgetPwdTv.setVisibility(View.VISIBLE);
        } else if (type == 2) {
            binding.activityMinePursePwdManagerSetNav.setTitle("忘记支付密码");
            _initForgetCell();
        } else if (type == 100) {
            binding.activityMinePursePwdManagerSetNav.setTitle("注销账号");
            binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgLl.setVisibility(
                    View.GONE);
            binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgLl.setVisibility(
                    View.GONE);
            binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("手机号");
            binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入手机号");
            binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setInputType(
                    InputType.TYPE_CLASS_NUMBER);
        }
    }

    private void _initSetCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_NUMBER);
    }

    private void _initModifyCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgLl.setVisibility(View.VISIBLE);
    }

    private void _initForgetCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请确认新密码");
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePursePwdManagerSetNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePursePwdManagerSetForgetPwdTv) {
            HashMap map = new HashMap();
            map.put("type", "2");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class, this, map);
        } else if (v == binding.activityMineAddressAddSaveRl) {
            String phone =
                    getTextStr(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
            String code =
                    getTextStr(binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt);

            if (type == 100) {
                if (phone.length() != 11) {
                    ToastUtils.toastMsg("手机格式错误");
                    return;
                }
                if (TextUtils.isEmpty(code) || code.length() > 6) {
                    ToastUtils.toastMsg("验证码错误");
                    return;
                }
                RegisterBean bean = new RegisterBean();
                bean.sms = code;
                HttpUtil.apiW()
                        .home_logout1(bean)
                        .enqueue(
                                new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(
                                            Call<NetData> call,
                                            Response<NetData> response,
                                            NetData body) {
                                        ToastUtils.toastMsg("注销成功");
                                        showLogin();
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {}
                                });
                return;
            }

            if (type != 1 && phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            if (type == 1 && TextUtils.isEmpty(phone)) {
                ToastUtils.toastMsg("请输入原密码");
                return;
            }
            if (TextUtils.isEmpty(code) || code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }

            String pwd1 =
                    getTextStr(binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt);
            String pwd2 =
                    getTextStr(
                            binding.activityMinePursePwdManagerSetConfirmPwd
                                    .viewTitleTfWithoutBgEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.password = pwd1;
            registerBean.captcha = code;
            if (type != 1) {
                registerBean.phoneNo = phone;
            }

            HttpUtil.apiW()
                    .home_updateFullPassword(registerBean)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call,
                                        Response<NetData> response,
                                        NetData body) {
                                    ToastUtils.toastMsg(body.msg);
                                    finish();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        PursePwdManagerSetActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(PursePwdManagerSetActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(
                                new Intent(
                                        PursePwdManagerSetActivity.this, WelcomeLoginActivity.class));
                        finish();
                    }
                });
    }
}
