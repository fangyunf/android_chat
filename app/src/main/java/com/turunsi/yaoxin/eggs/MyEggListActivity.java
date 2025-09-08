// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.databinding.ActivityEggListIndexBinding;
import com.turunsi.yaoxin.databinding.ActivityMineEggListBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class MyEggListActivity extends BaseActivity implements View.OnClickListener {

    ActivityMineEggListBinding binding;
    MyEggListAdapter adapter = new MyEggListAdapter();
    List<CustomMsgBean> noSendList = new ArrayList<>();
    List<CustomMsgBean> hasSendList = new ArrayList<>();

    boolean hasOK = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityMineEggListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        StatusBarUtils.transtStatusBar(this, binding.activityMineEggListNav);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if ("reload_my_egg_list".equals(event.getTag())) {
            _requestData();
        }
    }
//

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void _initView() {


        binding.activityMineEggListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineEggListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityMineEggListRv.setAdapter(adapter);
        binding.activityMineEggListItem1Tv.setOnClickListener(this);
        binding.activityMineEggListItem1Tv.setSelected(true);
        binding.activityMineEggListItem2Tv.setOnClickListener(this);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<CustomMsgBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<CustomMsgBean, ?> baseQuickAdapter, @NonNull View view, int i) {

                if (adapter.canSend) {
                    HashMap map = new HashMap<>();
                    map.put("id", baseQuickAdapter.getItem(i).id);
                    GroupListActivity.start(GroupListActivity.class, MyEggListActivity.this, map);
                }
            }
        });
    }


    @Override
    protected void _requestData() {

        LoadingDialog.showDialog(getSupportFragmentManager(), "请求中");
        HttpUtil.apiW().caidan_wdCaidan(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<CustomMsgBean>>() {
                        }.getType();
                        List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        noSendList = tempList;
                        adapter.canSend = true;
                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                        if (hasOK) {
                            LoadingDialog.dismissDialog();
                        }
                        hasOK = true;
                    }
                });

        HttpUtil.apiW().caidan_yffCaidan(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<CustomMsgBean>>() {
                        }.getType();
                        hasSendList = new Gson().fromJson(body.data.toString(), type);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                        if (hasOK) {
                            LoadingDialog.dismissDialog();
                        }
                        hasOK = true;
                    }
                });
    }


    @Override
    public void onClick(View view) {
        if (view == binding.activityMineEggListNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.activityMineEggListItem1Tv) {
            binding.activityMineEggListItem1Tv.setSelected(true);
            binding.activityMineEggListItem2Tv.setSelected(false);
            adapter.setItems(noSendList);
            adapter.canSend = true;
            adapter.notifyDataSetChanged();
        } else if (view == binding.activityMineEggListItem2Tv) {
            binding.activityMineEggListItem1Tv.setSelected(false);
            binding.activityMineEggListItem2Tv.setSelected(true);
            adapter.setItems(hasSendList);
            adapter.canSend = false;
            adapter.notifyDataSetChanged();

        }
    }

}
