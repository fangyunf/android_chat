package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunRedPacketRecordListBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.adapter.RedPacketRecordListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.TimeUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class FunRedPacketRecordListActivity extends BaseActivity implements View.OnClickListener {
    private static final int PAGE_SIZE = 20;

    ActivityFunRedPacketRecordListBinding binding;

    List<CustomMsgBean> receiveList = new ArrayList<>();
    List<CustomMsgBean> sendList = new ArrayList<>();
    String selectedDate = TimeUtils.getTodayDateString("yyyy-MM-dd");
    RedPacketRecordListAdapter adapter = new RedPacketRecordListAdapter();
    int selectedIndex = 0;

    private String receiveEndId = "";
    private String sendEndId = "";
    private boolean receiveNoMore = false;
    private boolean sendNoMore = false;
    private boolean receiveLoading = false;
    private boolean sendLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunRedPacketRecordListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        selectItem(0);
        refreshCurrentTab();
    }

    @Override
    protected void _initView() {
        binding.activityFunRedPacketRecordListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunRedPacketRecordListNameTv.setText(DataUtil.getUserInfo().username);
        GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunRedPacketRecordListHeadIv, DataUtil.getUserInfo().avatar, 25);
        binding.activityFunRedPacketRecordListChooseDateLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListSendLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListReceivceLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListChooseDateTv.setText(selectedDate);

        binding.activityFunRedPacketRecordListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityFunRedPacketRecordListRv.setAdapter(adapter);

        binding.activityFunRedPacketRecordListRefreshLayout.setEnableRefresh(true);
        binding.activityFunRedPacketRecordListRefreshLayout.setEnableLoadMore(true);
        binding.activityFunRedPacketRecordListRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        binding.activityFunRedPacketRecordListRefreshLayout.setOnRefreshListener(refreshLayout -> refreshCurrentTab());
        binding.activityFunRedPacketRecordListRefreshLayout.setOnLoadMoreListener(refreshLayout -> loadMoreCurrentTab());
    }

    void selectItem(int type) {
        selectedIndex = type;
        if (type == 0) {
            adapter._type = 0;
            adapter.setItems(receiveList);
            adapter.notifyDataSetChanged();
            binding.activityFunRedPacketRecordListReceivceTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_white));
            binding.activityFunRedPacketRecordListSendTv.setTextColor(getResources().getColor(com.netease.yunxin.kit.contactkit.ui.R.color.color_666666));
            binding.activityFunRedPacketRecordListReceivceLl.setSelected(true);
            binding.activityFunRedPacketRecordListSendLl.setSelected(false);
            syncLoadMoreState(receiveNoMore);
        } else {
            adapter._type = 1;
            adapter.setItems(sendList);
            adapter.notifyDataSetChanged();
            binding.activityFunRedPacketRecordListSendTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_white));
            binding.activityFunRedPacketRecordListReceivceTv.setTextColor(getResources().getColor(com.netease.yunxin.kit.contactkit.ui.R.color.color_666666));
            binding.activityFunRedPacketRecordListReceivceLl.setSelected(false);
            binding.activityFunRedPacketRecordListSendLl.setSelected(true);
            syncLoadMoreState(sendNoMore);
        }
    }

    private void refreshCurrentTab() {
        if (selectedIndex == 0) {
            refreshReceive();
        } else {
            refreshSend();
        }
    }

    private void loadMoreCurrentTab() {
        if (selectedIndex == 0) {
            if (receiveNoMore) {
                binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
                return;
            }
            requestReceive(true);
        } else {
            if (sendNoMore) {
                binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
                return;
            }
            requestSend(true);
        }
    }

    private void refreshReceive() {
        receiveEndId = "";
        receiveNoMore = false;
        receiveList = new ArrayList<>();
        if (selectedIndex == 0) {
            adapter._type = 0;
            adapter.setItems(receiveList);
            adapter.notifyDataSetChanged();
            binding.activityFunRedPacketRecordListRefreshLayout.resetNoMoreData();
        }
        requestReceive(false);
    }

    private void refreshSend() {
        sendEndId = "";
        sendNoMore = false;
        sendList = new ArrayList<>();
        if (selectedIndex == 1) {
            adapter._type = 1;
            adapter.setItems(sendList);
            adapter.notifyDataSetChanged();
            binding.activityFunRedPacketRecordListRefreshLayout.resetNoMoreData();
        }
        requestSend(false);
    }

    private void requestReceive(boolean append) {
        if (receiveLoading) {
            return;
        }
        if (append && receiveNoMore) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        receiveLoading = true;
        RegisterBean bean = new RegisterBean();
        bean.date = selectedDate;
        if (append && !TextUtils.isEmpty(receiveEndId)) {
            bean.endId = receiveEndId;
        }
        HttpUtil.apiW().red_reciveRecord(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        receiveLoading = false;
                        List<CustomMsgBean> page = parseRecordList(body);
                        if (append) {
                            receiveList.addAll(page);
                        } else {
                            receiveList = new ArrayList<>(page);
                        }
                        if (!page.isEmpty() && !TextUtils.isEmpty(page.get(page.size() - 1).id)) {
                            receiveEndId = page.get(page.size() - 1).id;
                        }
                        receiveNoMore = page.size() < PAGE_SIZE;
                        if (selectedIndex == 0) {
                            adapter._type = 0;
                            adapter.setItems(receiveList);
                            adapter.notifyDataSetChanged();
                            finishRefreshLoadMore(receiveNoMore);
                        } else {
                            finishRefreshLoadMoreQuietly();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        receiveLoading = false;
                        finishRefreshLoadMore(false);
                    }
                });
    }

    private void requestSend(boolean append) {
        if (sendLoading) {
            return;
        }
        if (append && sendNoMore) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
            return;
        }
        sendLoading = true;
        RegisterBean bean = new RegisterBean();
        bean.date = selectedDate;
        if (append && !TextUtils.isEmpty(sendEndId)) {
            bean.endId = sendEndId;
        }
        HttpUtil.apiW().red_sendRecord(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        sendLoading = false;
                        List<CustomMsgBean> page = parseRecordList(body);
                        if (append) {
                            sendList.addAll(page);
                        } else {
                            sendList = new ArrayList<>(page);
                        }
                        if (!page.isEmpty() && !TextUtils.isEmpty(page.get(page.size() - 1).id)) {
                            sendEndId = page.get(page.size() - 1).id;
                        }
                        sendNoMore = page.size() < PAGE_SIZE;
                        if (selectedIndex == 1) {
                            adapter._type = 1;
                            adapter.setItems(sendList);
                            adapter.notifyDataSetChanged();
                            finishRefreshLoadMore(sendNoMore);
                        } else {
                            finishRefreshLoadMoreQuietly();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        sendLoading = false;
                        finishRefreshLoadMore(false);
                    }
                });
    }

    private List<CustomMsgBean> parseRecordList(NetData body) {
        try {
            if (body == null || body.data == null) {
                return new ArrayList<>();
            }
            Type userListType = new TypeToken<List<CustomMsgBean>>() {
            }.getType();
            List<CustomMsgBean> list = new Gson().fromJson(body.data.toString(), userListType);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void finishRefreshLoadMore(boolean noMore) {
        if (binding.activityFunRedPacketRecordListRefreshLayout.isRefreshing()) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishRefresh();
        }
        if (binding.activityFunRedPacketRecordListRefreshLayout.isLoading()) {
            if (noMore) {
                binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMore();
            }
        } else if (noMore) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            binding.activityFunRedPacketRecordListRefreshLayout.resetNoMoreData();
        }
    }

    private void finishRefreshLoadMoreQuietly() {
        if (binding.activityFunRedPacketRecordListRefreshLayout.isRefreshing()) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishRefresh();
        }
        if (binding.activityFunRedPacketRecordListRefreshLayout.isLoading()) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMore();
        }
    }

    private void syncLoadMoreState(boolean noMore) {
        if (noMore) {
            binding.activityFunRedPacketRecordListRefreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            binding.activityFunRedPacketRecordListRefreshLayout.resetNoMoreData();
        }
    }

    @Override
    protected void _requestData() {
        // BaseActivity 在 super.onCreate 里调用时 binding 尚未创建，实际请求在 onCreate 末尾触发
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
        if (v == binding.activityFunRedPacketRecordListNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityFunRedPacketRecordListChooseDateLl) {

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
                    binding.activityFunRedPacketRecordListChooseDateTv.setText(selectedDate);
                    receiveEndId = "";
                    sendEndId = "";
                    receiveNoMore = false;
                    sendNoMore = false;
                    receiveList = new ArrayList<>();
                    sendList = new ArrayList<>();
                    adapter.setItems(selectedIndex == 0 ? receiveList : sendList);
                    adapter.notifyDataSetChanged();
                    refreshCurrentTab();
                }
            });
            picker.show();
        } else if (v == binding.activityFunRedPacketRecordListReceivceLl) {
            selectItem(0);
            if (receiveList.isEmpty() && !receiveLoading) {
                refreshReceive();
            }
        } else if (v == binding.activityFunRedPacketRecordListSendLl) {
            selectItem(1);
            if (sendList.isEmpty() && !sendLoading) {
                refreshSend();
            }
        }
    }
}
