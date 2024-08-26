// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class MyEggListActivity extends BaseActivity implements View.OnClickListener {

    ActivityMineEggListBinding binding;
    MyEggListAdapter adapter = new MyEggListAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityMineEggListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        StatusBarUtils.transtStatusBar(this,binding.activityMineEggListNav);


    }

    @Override
    protected void _initView() {


        binding.activityMineEggListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineEggListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityMineEggListRv.setAdapter(adapter);

    }


    @Override
    protected void _requestData() {

        HttpUtil.apiW().caidan_wdCaidan(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<CustomMsgBean>>() {}.getType();
                        List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), type);

                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }



    @Override
    public void onClick(View view) {
        if (view == binding.activityMineEggListNav.addCloseImageButton()) {
            finish();
        }
    }

}
