package com.turunsi.yaoxin.login;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityMineRealNameSetBinding;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineRealNameSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineRealNameSetSaveRl.setOnClickListener(this);

        binding.activityMineRealNameSetNav.addCloseImageButton().setVisibility(View.GONE);

        binding.activityMineRealNameSetName.viewTitleTfWithoutBgTv.setText("真实姓名");
        binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt.setHint("请输入姓名");
//        binding.activityMineRealNameSetName.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMineRealNameSetName.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));


        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgTv.setText("身份证号");
        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt.setHint("请输入身份证号");
//        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMineRealNameSetIdentityNum.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));


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

            /* ========== 原阿里云实人认证流程（保留备用）==========
             * 恢复时请把下面整段取消注释，并增加 import：
             * android.app.Activity; com.alipay.face.api.ZIMFacade; com.google.gson.Gson;
             * com.turunsi.yaoxin.utils.RealNameAuthUtil;
             * 同时注释或删掉当前仅姓名/身份证 + consumer_certified 的分支，避免重复提交。
             *
            ZIMFacade.install(this);
            String metaInfos = ZIMFacade.getMetaInfos(this);
            RegisterBean registerBeanAli = new RegisterBean();
            registerBeanAli.metaInfos = metaInfos;
            registerBeanAli.certName = certName;
            registerBeanAli.certNo = certNo;

            Activity that = this;
            HttpUtil.apiW().consumer_certify(registerBeanAli)
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
             * ================================================== */

            RegisterBean registerBean = new RegisterBean();
            registerBean.certName = certName;
            registerBean.certNo = certNo;

            HttpUtil.apiW().consumer_certify(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            HttpUtil.apiW().consumer_certified()
                                    .enqueue(new CommonCallback<NetData>() {
                                        @Override
                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                            ToastUtils.toastMsg("认证成功");
                                            finish();
                                        }

                                        @Override
                                        public void Failure(Call<NetData> call, Throwable t) {
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
        // 留空或者添加你希望的代码
        // super.onBackPressed(); // 这行代码将会执行默认的返回操作，注释掉即可屏蔽返回键
    }

}
