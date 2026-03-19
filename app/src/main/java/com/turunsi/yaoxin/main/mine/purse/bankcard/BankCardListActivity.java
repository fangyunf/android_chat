package com.turunsi.yaoxin.main.mine.purse.bankcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindBankCardActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BankCardListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBankCardListBinding binding;
    BankCardListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBankCardListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseBankCardListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePurseBankCardListRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BankCardListAdapter();
        binding.activityMinePurseBankCardListRv.setAdapter(adapter);
        adapter.setItems(dataList);
        adapter.setOnUnbindClickListener((item, position) -> {
            DialogAlertUtil.showAlert("确定删除该银行卡？", type -> {
                if (type == 1) {
                    confirmAndUnbind(item);
                }
            }, getSupportFragmentManager());
        });
        Context that = this;
        adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            if (i == dataList.size()) {
                BindBankCardActivity.start(BindBankCardActivity.class, BankCardListActivity.this, null);
            } else {
                UserBean userBean = baseQuickAdapter.getItem(i);
                String type = getIntent().getStringExtra("type");
                if (!TextUtils.isEmpty(type) && type.equals("1")) {
                    Intent intent = new Intent();
                    intent.putExtra("bank", userBean);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void confirmAndUnbind(UserBean item) {
        // 简单确认，可替换为自定义弹窗
        // 直接调用解绑
        RegisterBean req = new RegisterBean();
        req.type = 3; // 3 表示支付宝解绑
        req.id = item.id;
        HttpUtil.apiW().deleteZFB(req).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Toast.makeText(BankCardListActivity.this, "解绑成功", Toast.LENGTH_SHORT).show();
                makeRequest();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                Toast.makeText(BankCardListActivity.this, "解绑失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeRequest();
    }

    private void makeRequest() {
        RegisterBean bankBean = new RegisterBean();
        bankBean.type = 3;
        HttpUtil.apiW().bindCard_userZFB(bankBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<UserBean>>() {
                }.getType();
                List<UserBean> tempList = new Gson().fromJson(body.data.toString(), type);
                dataList.clear();
                dataList.addAll(tempList);
                adapter.setItems(dataList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseBankCardListNav.addCloseImageButton()) {
            finish();
        }
    }




}
