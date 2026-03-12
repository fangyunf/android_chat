package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunRedPacketRecordListBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendRedPacketBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.adapter.RedPacketRecordListAdapter;
import com.netease.yunxin.kit.chatkit.ui.fun.page.adapter.RedPacketResultDetailAdapter;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.TimeUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.actionsheet.ActionSheet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunRedPacketRecordListActivity extends BaseActivity implements View.OnClickListener {
    ActivityFunRedPacketRecordListBinding binding;
    List<CustomMsgBean> receiveList = new ArrayList<>();
    List<CustomMsgBean> sendList = new ArrayList<>();
    String selectedMonth = TimeUtils.getTodayDateString("yyyy-MM");
    RedPacketRecordListAdapter adapter = new RedPacketRecordListAdapter();
    int selectedIndex = 0;

    // 分页相关变量
    private String receiveEndId;
    private String sendEndId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunRedPacketRecordListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        transtStatusBar(binding.activityFunRedPacketRecordListNav);
        _initView();
        selectItem(0);
    }

    @Override
    protected void _initView() {
        binding.activityFunRedPacketRecordListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunRedPacketRecordListNameTv.setText(DataUtil.getUserInfo().username);
        GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunRedPacketRecordListHeadIv, DataUtil.getUserInfo().avatar, 25);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.activityFunRedPacketRecordListChooseDateLl.getLayoutParams();
        params.topMargin = params.topMargin + BarUtils.getStatusBarHeight();
        binding.activityFunRedPacketRecordListChooseDateLl.setLayoutParams(params);
        binding.activityFunRedPacketRecordListChooseDateLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListSendLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListReceivceLl.setOnClickListener(this);
        binding.activityFunRedPacketRecordListChooseDateTv.setText(selectedMonth);
        binding.activityFunRedPacketRecordListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityFunRedPacketRecordListRv.setAdapter(adapter);

        binding.smLayout.setOnRefreshListener(refreshLayout -> {
            if (selectedIndex == 0) {
                receiveEndId = null;
                getReceiveList(true);
            } else if (selectedIndex == 1) {
                sendEndId = null;
                getSendRecord(true);
            }
        });
        binding.smLayout.setOnLoadMoreListener(refreshLayout -> {
            // 加载更多时设置 endId 为最后一条数据的 id

            if (selectedIndex == 0) {
                if (!receiveList.isEmpty()) {
                    receiveEndId = receiveList.get(receiveList.size() - 1).id;
                }
                getReceiveList(false);
            } else if (selectedIndex == 1) {
                if (!sendList.isEmpty()) {
                    sendEndId = sendList.get(sendList.size() - 1).id;
                }
                getSendRecord(false);
            }
        });
    }

    void selectItem(int type) {
//        if (type == selectedIndex) {
//            return;
//        }
        selectedIndex = type;
        if (type == 0) {
            if (!receiveList.isEmpty()) {
                adapter._type = 0;
                adapter.setItems(receiveList);
                adapter.notifyDataSetChanged();
            }
            binding.activityFunRedPacketRecordListReceivceTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_white));
            binding.activityFunRedPacketRecordListSendTv.setTextColor(getResources().getColor(com.netease.yunxin.kit.contactkit.ui.R.color.color_666666));

            binding.activityFunRedPacketRecordListReceivceLl.setSelected(true);
            binding.activityFunRedPacketRecordListSendLl.setSelected(false);


        } else {
            if (!sendList.isEmpty()) {
                adapter._type = 1;
                adapter.setItems(sendList);
                adapter.notifyDataSetChanged();
            }
            binding.activityFunRedPacketRecordListSendTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_white));
            binding.activityFunRedPacketRecordListReceivceTv.setTextColor(getResources().getColor(com.netease.yunxin.kit.contactkit.ui.R.color.color_666666));
            binding.activityFunRedPacketRecordListReceivceLl.setSelected(false);
            binding.activityFunRedPacketRecordListSendLl.setSelected(true);
        }
    }


    private void getReceiveList(boolean isRefresh) {
        RegisterBean bean = new RegisterBean();
        bean.date = selectedMonth;
        if (!isRefresh && !TextUtils.isEmpty(receiveEndId)) {
            bean.endId = receiveEndId;
        }
        HttpUtil.apiW().red_reciveRecord(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type userListType = new TypeToken<List<CustomMsgBean>>() {
                }.getType();
                List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), userListType);
                if (selectedIndex == 0) {
                    adapter._type = 0;
                    if (isRefresh) {
                        receiveList.clear();
                    }
                    receiveList.addAll(tempList);
                    adapter.setItems(receiveList);
                    adapter.notifyDataSetChanged();
                    binding.smLayout.finishRefresh();
                    binding.smLayout.finishLoadMore();
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                binding.smLayout.finishRefresh();
                binding.smLayout.finishLoadMore();
            }
        });
    }

    private void getSendRecord(boolean isRefresh) {
        RegisterBean bean = new RegisterBean();
        bean.date = selectedMonth;
        if (!isRefresh && !TextUtils.isEmpty(sendEndId)) {
            bean.endId = sendEndId;
        }
        HttpUtil.apiW().red_sendRecord(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type userListType = new TypeToken<List<CustomMsgBean>>() {
                }.getType();
                List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), userListType);
                if (selectedIndex == 1) {
                    adapter._type = 1;
                    if (isRefresh) {
                        sendList.clear();
                    }
                    sendList.addAll(tempList);
                    adapter.setItems(sendList);
                    adapter.notifyDataSetChanged();
                    binding.smLayout.finishRefresh();
                    binding.smLayout.finishLoadMore();
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                binding.smLayout.finishRefresh();
                binding.smLayout.finishLoadMore();
            }
        });
    }

    protected void _requestData() {
        getReceiveList(true);
        getSendRecord(true);
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
            DateEntity defaultEn = DateEntity.target(new Date());

            wheelLayout.setRange(start, end, defaultEn);
            wheelLayout.setDateMode(DateMode.YEAR_MONTH);
            wheelLayout.setDateLabel("年", "月", "");
            picker.setOnDatePickedListener(new OnDatePickedListener() {
                @Override
                public void onDatePicked(int year, int month, int day) {
                    selectedMonth = year + "-" + month;
                    binding.activityFunRedPacketRecordListChooseDateTv.setText(selectedMonth);
//                    receiveList = new ArrayList<>();
//                    sendList = new ArrayList<>();
//                    adapter.setItems(receiveList);
//                    adapter.notifyDataSetChanged();
                    _requestData();
                }
            });
            picker.show();
        } else if (v == binding.activityFunRedPacketRecordListReceivceLl) {
            selectItem(0);
        } else if (v == binding.activityFunRedPacketRecordListSendLl) {
            selectItem(1);
        }
    }
}
