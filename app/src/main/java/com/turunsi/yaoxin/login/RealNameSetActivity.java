package com.turunsi.yaoxin.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.alipay.face.api.ZIMFacade;
import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityMineRealNameSetBinding;
import com.turunsi.yaoxin.utils.RealNameAuthUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class RealNameSetActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineRealNameSetBinding binding;
    /** 从设置等入口进入时可返回；强制实名流程不可返回 */
    private boolean canGoBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineRealNameSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineRealNameSetSaveRl.setOnClickListener(this);

        canGoBack = extras != null && "1".equals(extras.getString("showBack"));
        View backBtn = binding.activityMineRealNameSetNav.addCloseImageButton();
        if (canGoBack) {
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(this);
        } else {
            backBtn.setVisibility(View.GONE);
        }

        binding.activityMineRealNameSetName.viewTitleTfWithoutBgTv.setText("真实姓名");
        binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt.setHint("请输入姓名");

        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgTv.setText("身份证号");
        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt.setHint("请输入身份证号");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.isRunningRealName = false;
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineRealNameSetNav.addCloseImageButton()) {
            if (canGoBack) {
                finish();
            }
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
                            RegisterBean dataBean = new Gson().fromJson(body.data.toString(), RegisterBean.class);

                            RealNameAuthUtil.start(that, dataBean.certifyId, new RealNameAuthUtil.dispathBlockT() {
                                @Override
                                public void finishBlock() {
                                    ToastUtils.toastMsg("认证成功");
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        if (canGoBack) {
            super.onBackPressed();
        }
    }
}
