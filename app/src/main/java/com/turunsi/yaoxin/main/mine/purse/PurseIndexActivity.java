package com.turunsi.yaoxin.main.mine.purse;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketRecordListActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseIndexBinding;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindAlipayActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.BankCardListActivity;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.PurseRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class PurseIndexActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseIndexBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseIndexNav.addCloseImageButton().setOnClickListener(this);
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) binding.activityMinePurseIndexNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityMinePurseIndexNav.setLayoutParams(params);
        binding.activityMinePurseIndexNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        android.widget.TextView billTv = binding.activityMinePurseIndexNav.setActionText("账单");
        if (billTv != null) {
            billTv.setTextColor(0xFF333333);
        }
        Context that = this;
        binding.activityMinePurseIndexNav.setActionClickListener(new ICallBack() {
            @Override
            public void callBack() {
                BillDetailListActivity.start(BillDetailListActivity.class, that, null);
            }
        });
        _initCell();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        if (body == null || body.data == null) {
                            return;
                        }
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        if (bean != null) {
                            binding.activityMinePurseIndexBalanceTv.setText(
                                    "¥" + NumberUtil.formartMoney(bean.balance));
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }

    private void _initCell() {
        binding.activityMinePurseIndexLingqian.setOnClickListener(this);
        binding.activityMinePurseIndexRechargeTv.setOnClickListener(this);
        binding.activityMinePurseIndexTixianTv.setOnClickListener(this);
        binding.activityMinePurseIndexHbjl.setOnClickListener(this);
        binding.activityMinePurseIndexWdkb.setOnClickListener(this);
        binding.activityMinePurseIndexBdwx.setOnClickListener(this);
        binding.activityMinePurseIndexBdzfb.setOnClickListener(this);
        binding.activityMinePurseIndexIdentityTv.setOnClickListener(this);
        binding.activityMinePurseIndexPaySettingTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseIndexNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseIndexLingqian
                || v == binding.activityMinePurseIndexBalanceTv) {
            BillDetailListActivity.start(BillDetailListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexRechargeTv) {
            PurseRechargeActivity.start(PurseRechargeActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexTixianTv) {
            PurseTiXianActivity.start(PurseTiXianActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexHbjl) {
            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdzfb) {
            BindAlipayActivity.start(BindAlipayActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdwx) {
            Map map = new HashMap();
            map.put("type", "1");
            BindAlipayActivity.start(BindAlipayActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexWdkb) {
            BankCardListActivity.start(BankCardListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexIdentityTv) {
            HashMap map = new HashMap();
            map.put("showBack", "1");
            RealNameSetActivity.start(RealNameSetActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexPaySettingTv) {
            PursePwdManagerActivity.start(PursePwdManagerActivity.class, this, null);
        }
    }
}
