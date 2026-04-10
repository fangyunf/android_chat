package com.turunsi.yaoxin.main.mine.purse;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketRecordListActivity;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindAlipayActivity;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianAddAccountActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseIndexBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.BankCardListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.PurseRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.UsdtRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.usdt.BindUsdtEntryActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.UsdtWithdrawActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

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
        _initCell();
        binding.activityMinePurseIndexRechargeTv.setOnClickListener(this);
        binding.activityMinePurseIndexRechargeTv2.setOnClickListener(this);
        binding.activityMinePurseIndexTixianTv.setOnClickListener(this);
        Context that = this;
        binding.activityMinePurseIndexNav.setActionClickListener(new ICallBack() {
            @Override
            public void callBack() {
                BillDetailListActivity.start(BillDetailListActivity.class, that, null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        binding.activityMinePurseIndexBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void _initCell() {

        binding.activityMinePurseIndexHbjl.setOnClickListener(this);
        binding.activityMinePurseIndexLqmx.setOnClickListener(this);
        binding.activityMinePurseIndexSmrz.setOnClickListener(this);
        binding.activityMinePurseIndexWdkb.setOnClickListener(this);
        binding.activityMinePurseIndexBdwx.setOnClickListener(this);
        binding.activityMinePurseIndexBdzfb.setOnClickListener(this);
        binding.activityMinePurseIndexUstd.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseIndexNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseIndexRechargeTv) {
            PurseRechargeActivity.start(PurseRechargeActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexRechargeTv2) {
            Map map = new HashMap();
            map.put("type", "1");
            PurseRechargeActivity.start(PurseRechargeActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexTixianTv) {
//            ToastUtils.toastMsg("敬请期待");
            PurseTiXianActivity.start(PurseTiXianActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexHbjl) {
//            BillDetailListActivity.start(BillDetailListActivity.class,this,null);
            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexLqmx) {
            BillDetailListActivity.start(BillDetailListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdzfb) {
            BindAlipayActivity.start(BindAlipayActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexUstd) {
            BindUsdtEntryActivity.start(BindUsdtEntryActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdwx) {
            Map map = new HashMap();
            map.put("type", "1");
            BindAlipayActivity.start(BindAlipayActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexWdkb) {

            Map map = new HashMap();
            map.put("type", "3");
            BindAlipayActivity.start(BindAlipayActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexSmrz) {
            ToastUtils.toastMsg("已完成实名");
//            if (!Constant.isRunningRealName) {
//                Constant.isRunningRealName = true;
//                XKitRouter.withKey(Constant.RealName_Router)
//                        .withContext(AppProxy.getInstance().getContext())
//                        .navigate();
//            }
        }
//        else if (v == binding.activityMinePurseIndexCell8.itemPurseIndexCellRl) {
//            HashMap map = new HashMap();
//            map.put("type","0");
//            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
//        } else if (v == binding.activityMinePurseIndexCell9.itemPurseIndexCellRl) {
//            HashMap map = new HashMap();
//            map.put("type","1");
//            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
//        }
    }

}
