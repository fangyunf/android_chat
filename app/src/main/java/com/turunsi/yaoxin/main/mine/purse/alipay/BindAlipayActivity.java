package com.turunsi.yaoxin.main.mine.purse.alipay;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBindAlipayBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.PurseBankListAddActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BindAlipayActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBindAlipayBinding binding;

    List<UserBean> bindList;
    int _type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBindAlipayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineBindAlipayNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineBindAlipayGotoBindTv.setOnClickListener(this);
        binding.activityMineBindAlipayBindTv.setOnClickListener(this);
        binding.activityMineBindAlipayRebindTv.setOnClickListener(this);
        binding.activityMineBindAlipayBindSuccessTv.setOnClickListener(this);

        binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgTv.setText("支付宝账号");
        binding.activityMineBindAlipayName.viewTitleTfWithoutBgTv.setText("真实姓名");
        binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt.setHint("请输入支付宝账号");
        binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt.setHint("请输入您的真实姓名");
    }

    @Override
    protected void _requestData() {
        RegisterBean bean = new RegisterBean();
        bean.type = 2;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<UserBean>>() {}.getType();
                        bindList = new Gson().fromJson(body.data.toString(), type);
                        if (bindList.isEmpty()) {
                            _type = 0;
                        } else {
                            _type = 1;
                        }
                        updateUI();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    public void updateUI() {

        binding.activityMineBindAlipayGotoBindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayRebindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayBindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayBindSuccessLl.setVisibility(View.GONE);
        if (_type == 0) {
            binding.activityMineBindAlipayGotoBindLl.setVisibility(View.VISIBLE);
        } else if (_type == 1) {
            if (!bindList.isEmpty()) {
                binding.activityMineBindAlipayRebindContent.viewTitleDetailTemplateLeftTv.setText("已绑定支付宝");
                binding.activityMineBindAlipayRebindContent.viewTitleDetailTemplateRightTv.setText(bindList.get(0).phone);
            }
            binding.activityMineBindAlipayRebindLl.setVisibility(View.VISIBLE);

        } else if (_type == 2) {

            binding.activityMineBindAlipayBindLl.setVisibility(View.VISIBLE);
        } else if (_type == 3) {

            binding.activityMineBindAlipayBindSuccessLl.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMineBindAlipayNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBindAlipayGotoBindTv) {
            _type = 2;
            updateUI();
        } else if (v == binding.activityMineBindAlipayRebindTv) {
            _type = 2;
            updateUI();
        } else if (v == binding.activityMineBindAlipayBindTv) {
            String phone = getTextStr(binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt);
            String name = getTextStr(binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt);
            if (phone.isEmpty()) {
                ToastUtils.toastMsg("请输入手机号");
                return;
            }
            if (name.isEmpty()) {
                ToastUtils.toastMsg("请输入姓名");
                return;
            }
            RequestParamsBean registerBean = new RequestParamsBean(phone,name,"2");

            if (!bindList.isEmpty()) {
                registerBean.id = bindList.get(0).id + "";
            }
            HttpUtil.apiW().bindCard_createUptadeZFB1(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg(body.msg);
                            _type = 3;
                            updateUI();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        } else if (v == binding.activityMineBindAlipayBindSuccessTv) {
            finish();
        }
    }

}
