package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityForgetPwdBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class ForgetPwdActivity extends BaseActivity implements View.OnClickListener {
    ActivityForgetPwdBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPwdBinding.inflate(getLayoutInflater());
        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.forgetPwdActivityNav);
        _initTfText();
    }

    void _initTfText() {
//        binding.forgetPwdActivityPhoneTf.viewTitleTfTv.setText("手机号");
//        binding.forgetPwdActivityCodeTf.viewTitleTfTv.setText("验证码");
//        binding.forgetPwdActivityPwdTf.viewTitleTfTv.setText("密码");
//        binding.forgetPwdActivityPwd2Tf.viewTitleTfTv.setText("确认密码");
//
//        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
//        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
//
//        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setHint("请输入手机号");
//        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setHint("请输入验证码");
//        binding.forgetPwdActivityPwdTf.viewTitleTfEt.setHint("请输入密码");
//        binding.forgetPwdActivityPwd2Tf.viewTitleTfEt.setHint("请输入密码");
//
//        binding.forgetPwdActivityCodeTf.btnCaptcha.setVisibility(View.VISIBLE);
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
        binding.activityLoginTf2.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.activityLoginTf4.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.activityLoginTf3.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.activityLoginTf4.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.activityLoginTf3.viewTitleTfCountEyeIv.setSelected(true);
        binding.activityLoginTf4.viewTitleTfCountEyeIv.setSelected(true);
        binding.activityLoginTf3.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.activityLoginTf4.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.forgetPwdActivityBtn.setOnClickListener(this);
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
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginTf3.viewTitleTfCountEyeRl) {
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
        } else if (v == binding.forgetPwdActivityNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.forgetPwdActivityBtn) {
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
            RegisterBean bean = new RegisterBean();
            bean.password = pwd1;
            bean.phoneNo = phone;
            bean.captcha = code;

            Activity that = this;
            HttpUtil.apiW().customer_updatePassword(bean).enqueue(new CommonCallback<NetData>() {
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
