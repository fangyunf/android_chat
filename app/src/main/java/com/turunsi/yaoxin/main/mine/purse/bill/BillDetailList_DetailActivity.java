package com.turunsi.yaoxin.main.mine.purse.bill;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineBankBillDetailListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBankBillDetailListDetailBinding;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailGridSpacingItemDecoration;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailList_ShaiXuan_Adapter;
import com.turunsi.yaoxin.main.mine.purse.bill.bean.BillDetailBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.TimeUtil;
import com.yaoxin.appbase.utils.TimeUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BillDetailList_DetailActivity extends BaseActivity implements View.OnClickListener {

    ActivityMineBankBillDetailListDetailBinding binding;
    BillDetailBean detailBean;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String beanStr = (String) extras.get("bean");
        if (beanStr != null && !beanStr.isEmpty()) {
            detailBean = new Gson().fromJson(beanStr,BillDetailBean.class);
        }
        binding = ActivityMineBankBillDetailListDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineBankBillDetailListDetailNav.addCloseImageButton().setOnClickListener(this);

        _initView();
    }

    @Override
    protected void _initView() {
        binding.activityMineBankBillDetailListDetailTitleTv.setText(detailBean.remark);
        binding.activityMineBankBillDetailListDetailMoneyTv.setText((detailBean.amount > 0 ? "+": "") +detailBean.amount);

        binding.activityMineBankBillDetailListDetailCell2DetailTv.setOnClickListener(this);
        /*
        *
        String[] strs = {"全部","发送群购物券","领取群购物券","发送专属购物券","领取专属购物券","发送个人购物券","领取个人购物券","充值","提现","购物券退回","提现驳回","购物支出"};
        int[] types = {-1,23,26,21,24,22,25,0,1,27,5,0,100};
        * */
        switch (detailBean.moduleType) {
            case 23:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_lingqu_coupon);
                binding.activityMineBankBillDetailListDetailCell1Detail.setText("支付成功");
                binding.activityMineBankBillDetailListDetailCell4.setVisibility(View.VISIBLE);
                break;
            case 26:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_lingqu_coupon);
                binding.activityMineBankBillDetailListDetailCell1Detail.setText("已存入钱包");
                break;
            case 21:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_send_zhuanshu);
                binding.activityMineBankBillDetailListDetailCell1Detail.setText("支付成功");
                break;
                case 24:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_send_zhuanshu);
                    binding.activityMineBankBillDetailListDetailCell1Detail.setText("已存入钱包");
                break;
            case 22:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_geren_coupon);
                binding.activityMineBankBillDetailListDetailCell1Detail.setText("支付成功");
                break;
                case 25:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_geren_coupon);
                binding.activityMineBankBillDetailListDetailCell1Detail.setText("已存入钱包");
                break;
            case 0:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_chongzhi);
                break;
            case 1:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_tixian);
                break;
            case 27:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_tuihui);
                break;
            case 5:
                binding.activityMineBankBillDetailListDetailIconIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_bohui);
                break;
            default:
                break;
        }

        binding.activityMineBankBillDetailListDetailCell3DetailTv.setText(TimeUtil.stampToDate(detailBean.createTime));
        binding.activityMineBankBillDetailListDetailCell5DetailTv.setText(detailBean.traceId);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineBankBillDetailListDetailNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBankBillDetailListDetailCell2DetailTv) {
            HashMap map = new HashMap();
            map.put("redpacketId",detailBean.redpacketId);
            FunRedPacketResultActivity.start(FunRedPacketResultActivity.class,this,map);
        }
    }

}
