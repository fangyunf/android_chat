package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
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

import retrofit2.Call;
import retrofit2.Response;

/** 异地登录：输入密保完成验证 */
public class OtherPlaceLoginActivity extends BaseActivity {
    ActivityOtherPlaceLoginBinding binding;
    String account = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherPlaceLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityOtherPlaceLoginNav.addCloseImageButton().setOnClickListener(v -> finish());
        binding.activityOtherPlaceLoginDontVerifyTv.setOnClickListener(v -> finish());

        if (extras != null && extras.get("phone") != null) {
            account = (String) extras.get("phone");
        }

        binding.activityOtherPlaceLoginNav.getTitleView().setText("安全验证");
        binding.activityOtherPlaceLoginTv1.setText("您正在异地登录，须完成密保验证方可继续");
        binding.activityOtherPlaceLoginTv2.setVisibility(View.VISIBLE);
        binding.activityOtherPlaceLoginTv2.setTextSize(16);
        binding.activityOtherPlaceLoginTv2.setText(getString(R.string.label_account) + "：" + account);
        binding.activityOtherPlaceLoginGetCodeLl.setVisibility(View.VISIBLE);

        Activity that = this;
        binding.activityOtherPlaceLoginConfirmAnsTv.setOnClickListener(v -> submitSecurityAnswer(that));
    }

    private void submitSecurityAnswer(Activity activity) {
        if (TextUtils.isEmpty(account)) {
            ToastUtils.toastMsg(getString(R.string.toast_account_empty));
            return;
        }
        String ans = getTextStr(binding.activityOtherPlaceLoginAnsEt);
        if (TextUtils.isEmpty(ans)) {
            ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
            return;
        }
        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = account;
        registerBean.ans = ans;
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
                                IMUtil.loginIM(activity, userBean.userId, userBean.imToken);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }
}
