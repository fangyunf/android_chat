package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityRegisterBinding;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    ActivityRegisterBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.layoutTitleBar);
        binding.layoutTitleBar.addCloseImageButton().setOnClickListener(v -> finish());
        binding.tvToAccount.setOnClickListener(v -> finish());
        binding.tvToLogin.setOnClickListener(v -> finish());
        binding.activityLoginIsCheckedTxt2.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt4.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        _initTfText();
    }

    void _initTfText() {
        binding.registerActivityPhoneTf.viewTitleTfTv.setText("手机号");
        binding.registerActivityCodeTf.viewTitleTfTv.setText("验证码");
        binding.registerActivityPwdTf.viewTitleTfTv.setText("密码");
        binding.registerActivityPwd2Tf.viewTitleTfTv.setText("确认密码");
        binding.registerActivityPhoneTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.registerActivityCodeTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.registerActivityPhoneTf.viewTitleTfEt.setHint("请输入11位手机号");
        binding.registerActivityCodeTf.viewTitleTfEt.setHint("请输入验证码");
        binding.registerActivityPwdTf.viewTitleTfEt.setHint("请输入密码");
        binding.registerActivityPwd2Tf.viewTitleTfEt.setHint("请再次输入密码");
        binding.registerActivityCodeTf.btnCaptcha.setVisibility(View.VISIBLE);
        CountDownView countDownView = binding.registerActivityCodeTf.btnCaptcha;
        countDownView.setUserEdit(binding.registerActivityPhoneTf.viewTitleTfEt);
        countDownView.setCountDownTime(60);
        countDownView.setCaptchaListener(
                new LoginLoader.CaptchaListener() {
                    @Override
                    public void onPre() {
                        String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfEt);
                        CommonNetUtil.getPhoneCode(phone);
                    }

                    @Override
                    public void onComplete(String phoneOrEmail) {}
                });
        binding.activityRegisterBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginIsAgreeLl) {
            binding.activityLoginIsCheckedIv.setSelected(!binding.activityLoginIsCheckedIv.isSelected());
        } else if (v == binding.activityLoginIsCheckedTxt2) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                    .withParam("type", "2")
                    .withParam("title", "服务协议")
                    .withContext(this)
                    .navigate();
        } else if (v == binding.activityLoginIsCheckedTxt4) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                    .withParam("type", "1")
                    .withParam("title", "隐私政策")
                    .withContext(this)
                    .navigate();
        } else if (v == binding.activityRegisterBtn) {
            String phone = getTextStr(binding.registerActivityPhoneTf.viewTitleTfEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.registerActivityCodeTf.viewTitleTfEt);
            if (TextUtils.isEmpty(code) || code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.registerActivityPwdTf.viewTitleTfEt);
            String pwd2 = getTextStr(binding.registerActivityPwd2Tf.viewTitleTfEt);
            if (TextUtils.isEmpty(pwd1)) {
                ToastUtils.toastMsg("请输入密码");
                return;
            }
            if (!pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            if (!binding.activityLoginIsCheckedIv.isSelected()) {
                ToastUtils.toastMsg("请同意协议");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = phone;
            registerBean.password = pwd1;
            registerBean.captcha = code;
            registerBean.deviceId = DeviceUtils.getDeviceId(this);
            registerBean.clientType = Constant.clientType;
            Activity that = this;
            HttpUtil.apiW()
                    .customer_register(registerBean)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call, Response<NetData> response, NetData body) {
                                    UserBean userBean =
                                            new Gson().fromJson((String) body.data, UserBean.class);
                                    DataUtil.putUserInfo(userBean);
                                    DataUtil.putToken(userBean.token);
                                    IMUtil.loginIM(that, userBean.userId, userBean.imToken);
                                    finish();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
    }
}
