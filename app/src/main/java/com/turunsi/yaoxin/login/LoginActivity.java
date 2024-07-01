package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.BuildConfig;
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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    private Handler handler;
    boolean isAgree = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.activityLoginRegTv.setOnClickListener(this);
        binding.activityLoginForgetTv.setOnClickListener(this);
        binding.activityLoginBtn.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt2.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt4.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        binding.activityLoginTf1.viewTitleTfTv.setText("手机号");
        binding.activityLoginTf2.viewTitleTfTv.setText("密码");
        binding.activityLoginTf1.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (BuildConfig.DEBUG) {

        binding.activityLoginTf1.viewTitleTfEt.setText("13761543036");
        binding.activityLoginTf2.viewTitleTfEt.setText("fyf825811");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginRegTv) {
            RegisterActivity.start(RegisterActivity.class, this, null);
        }  else if (v == binding.activityLoginIsAgreeLl) {
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
            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
        } else if (v == binding.activityLoginBtn) {

            if (!binding.activityLoginIsCheckedIv.isSelected()) {
                ToastUtils.toastMsg("请同意协议");
                return;
            }
            String phone = getTextStr(binding.activityLoginTf1.viewTitleTfEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String pwd = getTextStr(binding.activityLoginTf2.viewTitleTfEt);
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

                                    OtherPlaceLoginFragment fragment = new OtherPlaceLoginFragment();
                                    fragment.showNow(getSupportFragmentManager(),"OtherPlaceLoginFragment");
                                }
                            }
                        }

                        @Override
                        public void end() {
                            super.end();
                            LoadingDialog.dismissDialog();
                        }
                    });

//            Intent intent = new Intent();
//            intent.setClass(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            this.startActivity(intent);
//            finish();
        }
    }
}
