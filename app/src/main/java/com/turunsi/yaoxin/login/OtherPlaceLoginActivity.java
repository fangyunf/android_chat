package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityOtherPlaceLoginBinding;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;
import com.yaoxin.appbase.view.splitedittextview.OnInputListener;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 异地登录验证：统一手机验证码（不再走密保入口页）。
 */
public class OtherPlaceLoginActivity extends BaseActivity implements View.OnClickListener {
    ActivityOtherPlaceLoginBinding binding;

    String _phone = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherPlaceLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityOtherPlaceLoginNav.addCloseImageButton().setOnClickListener(this);
        binding.activityOtherPlaceLoginDontVerifyTv.setOnClickListener(this);

        if (extras != null && extras.get("phone") != null) {
            _phone = (String) extras.get("phone");
        }
        showSmsVerifyUi();
    }

    /** 直接展示手机验证码界面 */
    private void showSmsVerifyUi() {
        binding.activityOtherPlaceLoginTv2.setTextSize(18);
        binding.activityOtherPlaceLoginGetCodeLl.setVisibility(View.VISIBLE);
        binding.activityOtherPlaceLoginTv1.setText("安全验证");
        binding.activityOtherPlaceLoginTv2.setVisibility(View.VISIBLE);
        binding.activityOtherPlaceLoginTv2.setText(_phone);
        binding.activityOtherPlaceLoginVerifyLl.setVisibility(View.GONE);
        binding.activityOtherPlaceLoginNav.getTitleView().setText("安全验证");

        final Activity that = this;
        binding.activityOtherPlaceLoginSplitEt.setOnInputListener(
                new OnInputListener() {
                    @Override
                    public void onInputFinished(String content) {
                        RegisterBean registerBean = new RegisterBean();
                        registerBean.phoneNo = _phone;
                        registerBean.captcha = content;
                        HttpUtil.apiW()
                                .customer_ydCodeCheck(registerBean)
                                .enqueue(
                                        new CommonCallback<NetData>() {
                                            @Override
                                            public void Successful(
                                                    Call<NetData> call,
                                                    Response<NetData> response,
                                                    NetData body) {
                                                ToastUtils.toastMsg("验证成功");
                                                UserBean userBean =
                                                        new Gson()
                                                                .fromJson(
                                                                        (String) body.data,
                                                                        UserBean.class);
                                                DataUtil.putUserInfo(userBean);
                                                DataUtil.putToken(userBean.token);
                                                DataUtil.addLoginUserInfoList(userBean);
                                                IMUtil.loginIM(
                                                        that, userBean.userId, userBean.imToken);
                                            }

                                            @Override
                                            public void Failure(
                                                    Call<NetData> call, Throwable t) {}
                                        });
                    }
                });

        CountDownView mCountDownView = binding.activityOtherPlaceLoginBtnCaptcha;
        mCountDownView.needVerify = false;
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(
                new LoginLoader.CaptchaListener() {
                    @Override
                    public void onPre() {
                        CommonNetUtil.getPhoneCode(_phone);
                    }

                    @Override
                    public void onComplete(String phoneOrEmail) {}
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityOtherPlaceLoginNav.addCloseImageButton()
                || v == binding.activityOtherPlaceLoginDontVerifyTv) {
            finish();
        }
    }
}
