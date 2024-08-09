package com.turunsi.yaoxin.main.mine.purse;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketRecordListActivity;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindAlipayActivity;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.PurseUSDTRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianAddAccountActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseIndexBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.BankCardListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.PurseRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

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
        binding.activityMinePurseIndexTixianTv.setOnClickListener(this);
        Context that = this;
        binding.activityMinePurseIndexNav.setActionClickListener(new ICallBack() {
            @Override
            public void callBack() {
                BillDetailListActivity.start(BillDetailListActivity.class,that,null);
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
                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        binding.activityMinePurseIndexBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void _initCell() {
        binding.activityMinePurseIndexCell1.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_hbjl);
        binding.activityMinePurseIndexCell2.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_zfb);
        binding.activityMinePurseIndexCell3.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_zcpt_wzgl);
        binding.activityMinePurseIndexCell4.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_yinlian);
        binding.activityMinePurseIndexCell5.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_szrmb);
        binding.activityMinePurseIndexCell6.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_weixin);
        binding.activityMinePurseIndexCell7.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_yuleyouxi);
        binding.activityMinePurseIndexCell8.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_mima);
        binding.activityMinePurseIndexCell9.itemPurseIndexCellIconIv.setImageResource(R.mipmap.mine_purse_index_xiugaimima);

        binding.activityMinePurseIndexCell1.itemPurseIndexCellTitleTv.setText("红包记录");
        binding.activityMinePurseIndexCell2.itemPurseIndexCellTitleTv.setText("绑定支付宝");
        binding.activityMinePurseIndexCell3.itemPurseIndexCellTitleTv.setText("USDT充值地址");
        binding.activityMinePurseIndexCell3.itemPurseIndexCellRl.setVisibility(View.GONE);
//        binding.activityMinePurseIndexCell3.itemPurseIndexCellRl.setVisibility(View.GONE);
//        binding.activityMinePurseIndexCell3.itemPurseIndexCellRl.setVisibility(View.GONE);
        binding.activityMinePurseIndexCell4.itemPurseIndexCellTitleTv.setText("银行卡");
        binding.activityMinePurseIndexCell4.itemPurseIndexCellRl.setVisibility(View.GONE);
        binding.activityMinePurseIndexCell5.itemPurseIndexCellTitleTv.setText("数字人民币");
        binding.activityMinePurseIndexCell5.itemPurseIndexCellRl.setVisibility(View.GONE);
        binding.activityMinePurseIndexCell6.itemPurseIndexCellTitleTv.setText("绑定微信");
        binding.activityMinePurseIndexCell6.itemPurseIndexCellRl.setVisibility(View.GONE);
        binding.activityMinePurseIndexCell7.itemPurseIndexCellTitleTv.setText("娱乐游戏");
        binding.activityMinePurseIndexCell8.itemPurseIndexCellTitleTv.setText("设置密码");
        binding.activityMinePurseIndexCell9.itemPurseIndexCellTitleTv.setText("修改密码");

        binding.activityMinePurseIndexCell1.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell2.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell3.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell4.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell5.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell6.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell7.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell8.itemPurseIndexCellRl.setOnClickListener(this);
        binding.activityMinePurseIndexCell9.itemPurseIndexCellRl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseIndexNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseIndexRechargeTv) {
            PurseRechargeActivity.start(PurseRechargeActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexTixianTv) {
//            ToastUtils.toastMsg("敬请期待");
            PurseTiXianActivity.start(PurseTiXianActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell1.itemPurseIndexCellRl) {
//            BillDetailListActivity.start(BillDetailListActivity.class,this,null);
            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell2.itemPurseIndexCellRl) {
            BindAlipayActivity.start(BindAlipayActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell3.itemPurseIndexCellRl) {
//            PurseUSDTRechargeActivity.start(PurseUSDTRechargeActivity.class,this,null);
            ToastUtils.toastMsg("敬请期待");
        } else if (v == binding.activityMinePurseIndexCell4.itemPurseIndexCellRl) {
            ToastUtils.toastMsg("敬请期待");
        } else if (v == binding.activityMinePurseIndexCell5.itemPurseIndexCellRl) {
            ToastUtils.toastMsg("敬请期待");
        } else if (v == binding.activityMinePurseIndexCell6.itemPurseIndexCellRl) {
            ToastUtils.toastMsg("敬请期待");
        } else if (v == binding.activityMinePurseIndexCell7.itemPurseIndexCellRl) {
            ToastUtils.toastMsg("敬请期待");
        } else if (v == binding.activityMinePurseIndexCell8.itemPurseIndexCellRl) {
            HashMap map = new HashMap();
            map.put("type","0");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
        } else if (v == binding.activityMinePurseIndexCell9.itemPurseIndexCellRl) {
            HashMap map = new HashMap();
            map.put("type","1");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
        }
    }

}
