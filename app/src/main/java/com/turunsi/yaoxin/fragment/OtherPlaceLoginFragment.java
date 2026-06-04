package com.turunsi.yaoxin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.FragmentOtherPlaceLoginBinding;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
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

import retrofit2.Call;
import retrofit2.Response;

public class OtherPlaceLoginFragment extends BaseDialogFragment implements View.OnClickListener {
    FragmentOtherPlaceLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOtherPlaceLoginBinding.inflate(inflater, container, false);

        binding.btnCaptcha.setVisibility(View.VISIBLE);
        CountDownView mCountDownView = binding.btnCaptcha;
        mCountDownView.setUserEdit(binding.fragmentOtherPlaceLoginPhoneEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(
                new LoginLoader.CaptchaListener() {
                    @Override
                    public void onPre() {
                        String phone = getTextStr(binding.fragmentOtherPlaceLoginPhoneEt);
                        CommonNetUtil.getPhoneCode(phone);
                    }

                    @Override
                    public void onComplete(String phoneOrEmail) {}
                });

        binding.fragmentOtherPlaceLoginConfirmBtn.setOnClickListener(this);
        binding.fragmentOtherPlaceLoginInputCodeEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.fragmentOtherPlaceLoginPhoneEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.fragmentOtherPlaceLoginConfirmBtn) {
            String phone = getTextStr(binding.fragmentOtherPlaceLoginPhoneEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.fragmentOtherPlaceLoginInputCodeEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = phone;
            registerBean.captcha = code;

            Activity that = getActivity();
            HttpUtil.apiW()
                    .customer_ydCodeCheck(registerBean)
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
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
    }
}
