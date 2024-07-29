package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.alipay.face.api.ZIMFacade;
import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerSetBinding;
import com.turunsi.yaoxin.databinding.ActivityMineRealNameSetBinding;
import com.turunsi.yaoxin.utils.IMUtil;
import com.turunsi.yaoxin.utils.RealNameAuthUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class RealNameSetActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineRealNameSetBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineRealNameSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineRealNameSetNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineRealNameSetSaveRl.setOnClickListener(this);


        binding.activityMineRealNameSetName.viewTitleTfWithoutBgTv.setText("真实姓名");
        binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt.setHint("请输入姓名");
        binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMineRealNameSetName.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));


        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgTv.setText("身份证号");
        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt.setHint("请输入身份证号");
        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清除Activity正在运行的标志
        Constant.isRunningRealName = false;
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMineRealNameSetNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineRealNameSetSaveRl) {
            String certName = getTextStr(binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt);
            if (certName.isEmpty()) {
                ToastUtils.toastMsg("请输入姓名");
                return;
            }
            String certNo = getTextStr(binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt);
            if (certNo.isEmpty()) {
                ToastUtils.toastMsg("请输入身份证");
                return;
            }
            ZIMFacade.install(this);
            String metaInfos = ZIMFacade.getMetaInfos(this);
            RegisterBean registerBean = new RegisterBean();
            registerBean.metaInfos = metaInfos;
            registerBean.certName = certName;
            registerBean.certNo = certNo;

            Activity that = this;
            HttpUtil.apiW().consumer_certify(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            RegisterBean dataBean = new Gson().fromJson(body.data.toString(),RegisterBean.class);

                            RealNameAuthUtil.start(that,dataBean.certifyId);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
}
