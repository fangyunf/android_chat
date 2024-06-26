package com.turunsi.yaoxin.main.mine.purse;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailListActivity;
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
import com.yaoxin.appbase.utils.NumberUtil;

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

        _initCell();
        binding.activityMinePurseIndexRechargeTv.setOnClickListener(this);
        binding.activityMinePurseIndexTixianTv.setOnClickListener(this);
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
        binding.activityMinePurseIndexCell1.viewPurseIconTitleIv.setImageResource(R.mipmap.mine_fragment_purse_index_bill_manager);
        binding.activityMinePurseIndexCell2.viewPurseIconTitleIv.setImageResource(R.mipmap.mine_fragment_purse_index_realname_manager);
        binding.activityMinePurseIndexCell3.viewPurseIconTitleIv.setImageResource(R.mipmap.mine_fragment_purse_index_bankcard_manager);
        binding.activityMinePurseIndexCell4.viewPurseIconTitleIv.setImageResource(R.mipmap.mine_fragment_purse_index_pwd_manager);

        binding.activityMinePurseIndexCell1.viewPurseIconTitleTv.setText("账单明细");
        binding.activityMinePurseIndexCell2.viewPurseIconTitleTv.setText("实名认证");
        binding.activityMinePurseIndexCell3.viewPurseIconTitleTv.setText("银行卡管理");
        binding.activityMinePurseIndexCell4.viewPurseIconTitleTv.setText("密码管理");

        binding.activityMinePurseIndexCell1.viewPurseIconTitleIvLl.setOnClickListener(this);
        binding.activityMinePurseIndexCell2.viewPurseIconTitleIvLl.setOnClickListener(this);
        binding.activityMinePurseIndexCell3.viewPurseIconTitleIvLl.setOnClickListener(this);
        binding.activityMinePurseIndexCell4.viewPurseIconTitleIvLl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseIndexNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseIndexCell4.viewPurseIconTitleIvLl) {
            PursePwdManagerActivity.start(PursePwdManagerActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexRechargeTv) {
            PurseRechargeActivity.start(PurseRechargeActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexTixianTv) {
            PurseTiXianActivity.start(PurseTiXianActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell3.viewPurseIconTitleIvLl) {
            BankCardListActivity.start(BankCardListActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell1.viewPurseIconTitleIvLl) {
            BillDetailListActivity.start(BillDetailListActivity.class,this,null);
        } else if (v == binding.activityMinePurseIndexCell2.viewPurseIconTitleIvLl) {
            RealNameSetActivity.start(RealNameSetActivity.class,this,null);
        }
    }

}
