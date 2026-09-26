package com.turunsi.yaoxin.main.mine.purse.bill;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class BillDetailListActivity extends BaseActivity implements View.OnClickListener {
    private static final int PAGE_SIZE = 20;

    ActivityMineBankBillDetailListBinding binding;
    BillDetailListAdapter adapter = new BillDetailListAdapter();

    ArrayList<BillDetailBean> shaixuanList = new ArrayList<>();
    int moudleType = -1;
    String selectedDate = TimeUtils.getTodayDateString("yyyy-MM-dd");
    private String endId = "";
    private boolean noMore = false;
    private boolean loading = false;

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

        binding.activityMineBankBillDetailListDateTv.setText(selectedDate);

        String[] strs = {"全部", "发送群红包", "领取群红包", "发送专属红包", "领取专属红包", "发送个人红包", "领取个人红包", "充值", "提现", "红包退回", "提现驳回", "购物支出", "抽奖", "靓号"};
        int[] types = {-1, 23, 26, 21, 24, 22, 25, 0, 1, 27, 5, 0, 100, 80, 78};

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
                map.put("bean", new Gson().toJson(baseQuickAdapter.getItem(i)));
                BillDetailList_DetailActivity.start(BillDetailList_DetailActivity.class, that, map);
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
                refreshFromStart();
            }
        });

        binding.activityMineBankBillDetailListRefreshLayout.setEnableRefresh(true);
        binding.activityMineBankBillDetailListRefreshLayout.setEnableLoadMore(true);
        binding.activityMineBankBillDetailListRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        binding.activityMineBankBillDetailListRefreshLayout.setOnRefreshListener(refreshLayout -> refreshFromStart());
        binding.activityMineBankBillDetailListRefreshLayout.setOnLoadMoreListener(refreshLayout -> loadMore());
        // BaseActivity.onCreate 会在 binding 初始化前调用 _requestData，首次加载放到视图就绪后
        refreshFromStart();
    }

    private void refreshFromStart() {
        if (binding == null) {
            return;
        }
        endId = "";
        noMore = false;
        dataList = new ArrayList();
        adapter.setItems(dataList);
        adapter.notifyDataSetChanged();
        binding.activityMineBankBillDetailListRefreshLayout.resetNoMoreData();
        requestList(false);
    }

    private void loadMore() {
        if (binding == null) {
            return;
        }
        if (noMore) {
            binding.activityMineBankBillDetailListRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        requestList(true);
    }

    @Override
    protected void _requestData() {
        // BaseActivity 在 super.onCreate 里调用时 binding 尚未创建，实际请求在 onCreate 末尾触发
    }

    private void requestList(boolean append) {
        if (binding == null) {
            return;
        }
        if (loading) {
            return;
        }
        if (append && noMore) {
            binding.activityMineBankBillDetailListRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        loading = true;
        RegisterBean bean = new RegisterBean();
        bean.moudleType = moudleType;
        bean.date = selectedDate;
        if (append && !TextUtils.isEmpty(endId)) {
            bean.endId = endId;
        }
        HttpUtil.apiW().red_transcationsList(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        loading = false;
                        if (binding == null) {
                            return;
                        }
                        List<BillDetailBean> page = parseList(body);
                        if (append) {
                            dataList.addAll(page);
                        } else {
                            dataList = new ArrayList(page);
                        }
                        if (!page.isEmpty() && !TextUtils.isEmpty(page.get(page.size() - 1).id)) {
                            // 上拉游标：lastItem.transcationId，服务端字段 id
                            endId = page.get(page.size() - 1).id;
                        }
                        noMore = page.size() < PAGE_SIZE;
                        adapter.setItems(dataList);
                        adapter.notifyDataSetChanged();
                        finishRefreshLoadMore(noMore);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        loading = false;
                        if (binding == null) {
                            return;
                        }
                        finishRefreshLoadMore(false);
                    }
                });
    }

    private List<BillDetailBean> parseList(NetData body) {
        try {
            if (body == null || body.data == null) {
                return new ArrayList<>();
            }
            Type type = new TypeToken<List<BillDetailBean>>() {
            }.getType();
            List<BillDetailBean> list = new Gson().fromJson(body.data.toString(), type);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void finishRefreshLoadMore(boolean reachNoMore) {
        if (binding == null) {
            return;
        }
        if (binding.activityMineBankBillDetailListRefreshLayout.isRefreshing()) {
            binding.activityMineBankBillDetailListRefreshLayout.finishRefresh();
        }
        if (binding.activityMineBankBillDetailListRefreshLayout.isLoading()) {
            if (reachNoMore) {
                binding.activityMineBankBillDetailListRefreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                binding.activityMineBankBillDetailListRefreshLayout.finishLoadMore();
            }
        } else if (reachNoMore) {
            binding.activityMineBankBillDetailListRefreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            binding.activityMineBankBillDetailListRefreshLayout.resetNoMoreData();
        }
    }

    private DateEntity parseSelectedDateEntity() {
        try {
            String[] parts = selectedDate.split("-");
            if (parts.length >= 3) {
                return DateEntity.target(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
            }
        } catch (Exception ignored) {
        }
        return DateEntity.target(new Date());
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
            DateEntity start = DateEntity.target(2023, 6, 15);
            DateEntity end = DateEntity.target(new Date());
            DateEntity defaultEn = parseSelectedDateEntity();

            wheelLayout.setRange(start, end, defaultEn);
            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
            wheelLayout.setDateLabel("年", "月", "日");
            picker.setOnDatePickedListener(new OnDatePickedListener() {
                @Override
                public void onDatePicked(int year, int month, int day) {
                    selectedDate = String.format(Locale.CHINA, "%d-%02d-%02d", year, month, day);
                    binding.activityMineBankBillDetailListDateTv.setText(selectedDate);
                    refreshFromStart();
                }
            });
            picker.show();
        }
    }

}
