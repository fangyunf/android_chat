package com.turunsi.yaoxin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class OtherPlaceLoginFragment extends BaseDialogFragment implements View.OnClickListener {
    FragmentOtherPlaceLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtherPlaceLoginBinding.inflate(inflater, container, false);
        binding.fragmentOtherPlaceLoginConfirmBtn.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.fragmentOtherPlaceLoginConfirmBtn) {
            String account = getTextStr(binding.fragmentOtherPlaceLoginPhoneEt).trim();
            if (TextUtils.isEmpty(account)) {
                ToastUtils.toastMsg("请输入账号");
                return;
            }
            String ans = getTextStr(binding.fragmentOtherPlaceLoginInputCodeEt).trim();
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg("请输入密保答案");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = account;
            registerBean.ans = ans;

            Activity that = getActivity();
            HttpUtil.apiW().customer_ydCodeCheck(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
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
