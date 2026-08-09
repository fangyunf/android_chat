package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class OtherPlaceLoginActivity extends BaseActivity implements View.OnClickListener {
    ActivityOtherPlaceLoginBinding binding;

    int _type = 0;
    String _phone = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherPlaceLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityOtherPlaceLoginNav.addCloseImageButton().setOnClickListener(this);
        binding.activityOtherPlaceLoginDontVerifyTv.setOnClickListener(this);
        binding.activityOtherPlaceLoginVerifyTv.setOnClickListener(this);

        if (extras.get("type") != null) {
            _type = Integer.parseInt((String) extras.get("type"));
        }
        if (extras.get("phone") != null) {
            _phone = (String) extras.get("phone");
        }
        if (_type == 1) {
            binding.activityOtherPlaceLoginTv2.setVisibility(View.VISIBLE);
            binding.activityOtherPlaceLoginTv2.setTextSize(14);
            binding.activityOtherPlaceLoginGetCodeLl.setVisibility(View.VISIBLE);
            binding.activityOtherPlaceLoginTv1.setText("安全验证");
            binding.activityOtherPlaceLoginTv2.setText(
                    TextUtils.isEmpty(_phone) ? "请输入密保完成验证" : ("账号：" + _phone));
            binding.activityOtherPlaceLoginVerifyLl.setVisibility(View.GONE);
            binding.activityOtherPlaceLoginNav.getTitleView().setText("安全验证");
            binding.activityOtherPlaceLoginAnsConfirmTv.setOnClickListener(this);
        }
    }

    private void submitAns() {
        String ans = getTextStr(binding.activityOtherPlaceLoginAnsEt);
        if (TextUtils.isEmpty(ans)) {
            ToastUtils.toastMsg("请输入密保");
            return;
        }
        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = _phone;
        registerBean.ans = ans;
        Activity that = this;
        HttpUtil.apiW()
                .customer_ydCodeCheckZh(registerBean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg("验证成功");
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

    @Override
    public void onClick(View v) {
        if (v == binding.activityOtherPlaceLoginNav.addCloseImageButton()
                || binding.activityOtherPlaceLoginDontVerifyTv == v) {
            finish();
        } else if (v == binding.activityOtherPlaceLoginVerifyTv) {
            HashMap map = new HashMap<>();
            map.put("type", "1");
            map.put("phone", _phone);
            OtherPlaceLoginActivity.start(OtherPlaceLoginActivity.class, this, map);
        } else if (v == binding.activityOtherPlaceLoginAnsConfirmTv) {
            submitAns();
        }
    }
}
