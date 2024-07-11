package com.turunsi.yaoxin.main.mine.purse.alipay;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBindAlipayBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.PurseBankListAddActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;

import retrofit2.Call;
import retrofit2.Response;

public class BindAlipayActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBindAlipayBinding binding;

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
        HttpUtil.apiW().bindCard_userZFB(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

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
            RegisterBean registerBean = new RegisterBean();
            registerBean.phone = "18616821287";
            registerBean.name = "万运浩";
            registerBean.type = 2;
            HttpUtil.apiW().bindCard_createUptadeZFB(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
            _type = 3;
            updateUI();
        } else if (v == binding.activityMineBindAlipayBindSuccessTv) {
            _type = 0;
            updateUI();
        }
    }

}
