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
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.databinding.ActivityMineBankBillDetailListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.PurseBankListAddActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailGridSpacingItemDecoration;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bill.adapter.BillDetailList_ShaiXuan_Adapter;
import com.turunsi.yaoxin.main.mine.purse.bill.bean.BillDetailBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.TimeUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class BillDetailListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBankBillDetailListBinding binding;
    BillDetailListAdapter adapter = new BillDetailListAdapter();

    ArrayList<BillDetailBean> shaixuanList = new ArrayList<>();
    int moudleType = -1;
    String selectedMonth = TimeUtils.getTodayDateString("yyyy-MM");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBankBillDetailListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineBankBillDetailListNav.addCloseImageButton().setOnClickListener(this);

        transtStatusBar(binding.activityMineBankBillDetailListNav);
        binding.activityMineBankBillDetailListShaixuanLl.setOnClickListener(this);
        binding.activityMineBankBillDetailListDateLl.setOnClickListener(this);

        binding.activityMineBankBillDetailListGrayRl.setOnClickListener(this);
        binding.activityMineBankBillDetailListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityMineBankBillDetailListRv.setAdapter(adapter);

        binding.activityMineBankBillDetailListDateTv.setText(selectedMonth);

        String[] strs = {"全部","发送群红包","领取群红包","发送专属红包","领取专属红包","发送个人红包","领取个人红包","充值","提现","红包退回","提现驳回","购物支出","抽奖","靓号"};
        int[] types = {-1,23,26,21,24,22,25,0,1,27,5,0,100,80,78};

//        for (int i = 0; i < strs.length; i++) {
//            BillDetailBean bean = new BillDetailBean();
//            bean.title = strs[i];
//            bean.type = types[i];
//            dataList.add(bean);
//        }

        for (int i = 0; i < strs.length; i++) {
            BillDetailBean bean = new BillDetailBean();
            bean.isSelected = i == 0;
            bean.title = strs[i];
            bean.type = types[i];
            shaixuanList.add(bean);
        }

        adapter.setItems(dataList);
        Context that = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<BillDetailBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<BillDetailBean, ?> baseQuickAdapter, @NonNull View view, int i) {

                HashMap map = new HashMap();
                map.put("bean",new Gson().toJson(baseQuickAdapter.getItem(i)));
                BillDetailList_DetailActivity.start(BillDetailList_DetailActivity.class,that,map);
            }
        });



        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        binding.activityMineBankBillDetailListShaixuanRv.setLayoutManager(gridLayoutManager);
        BillDetailGridSpacingItemDecoration gridSpacingItemDecoration =
                new BillDetailGridSpacingItemDecoration(3, SizeUtils.dp2px(10), false);
        binding.activityMineBankBillDetailListShaixuanRv.addItemDecoration(gridSpacingItemDecoration);
        BillDetailList_ShaiXuan_Adapter shaiXuanAdapter = new BillDetailList_ShaiXuan_Adapter();
        binding.activityMineBankBillDetailListShaixuanRv.setAdapter(shaiXuanAdapter);

        shaiXuanAdapter.setItems(shaixuanList);
        shaiXuanAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<BillDetailBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<BillDetailBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                for (BillDetailBean tempBean :
                        shaixuanList) {
                    tempBean.isSelected = false;
                }
                shaixuanList.get(i).isSelected = true;
                binding.activityMineBankBillDetailListShaixuanTv.setText(shaixuanList.get(i).title);
                moudleType = baseQuickAdapter.getItem(i).type;
                binding.activityMineBankBillDetailListGrayRl.setVisibility(View.GONE);
                shaiXuanAdapter.notifyDataSetChanged();
                _requestData();
            }
        });
    }

    @Override
    protected void _requestData() {
        RegisterBean bean = new RegisterBean();
        bean.moudleType = moudleType;
        bean.date = selectedMonth;
        HttpUtil.apiW().red_transcationsList(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<BillDetailBean>>(){}.getType();
                        dataList = new Gson().fromJson(body.data.toString(),type);
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
        if (v == binding.activityMineBankBillDetailListNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBankBillDetailListGrayRl) {
            binding.activityMineBankBillDetailListGrayRl.setVisibility(View.GONE);
        } else if (v == binding.activityMineBankBillDetailListShaixuanLl) {
            binding.activityMineBankBillDetailListGrayRl.setVisibility(View.VISIBLE);
        } else if (v == binding.activityMineBankBillDetailListDateLl) {
            DatePicker picker = new DatePicker(this);
            picker.setBodyWidth(240);
            DateWheelLayout wheelLayout = picker.getWheelLayout();
            DateEntity start = DateEntity.target(2023,6,15);
            DateEntity end = DateEntity.target(new Date());
            DateEntity defaultEn = DateEntity.target(new Date());

            wheelLayout.setRange(start,end,defaultEn);
            wheelLayout.setDateMode(DateMode.YEAR_MONTH);
            wheelLayout.setDateLabel("年", "月", "");
            picker.setOnDatePickedListener(new OnDatePickedListener() {
                @Override
                public void onDatePicked(int year, int month, int day) {
                    selectedMonth = year + "-" + month;
                    binding.activityMineBankBillDetailListDateTv.setText(selectedMonth);
                    dataList.clear();
                    adapter.setItems(dataList);
                    adapter.notifyDataSetChanged();
                    _requestData();
                }
            });
            picker.show();
        }
    }

}
