// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.nanchen.wavesidebar.SearchEditText;
import com.nanchen.wavesidebar.Trans2PinYinUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunOpenRedPacketFragment;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewForbiddenListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewVlqHbListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserMingDanListAdapter;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingVlqHBListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class FunTeamSettingNew_Vlq_HB_ListActivity extends BaseActivity implements View.OnClickListener {

    FunTeamSettingNewVlqHbListActivityBinding binding;
    String groupId;
    TeamSettingVlqHBListAdapter adapter = new TeamSettingVlqHBListAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        _getParams();
        if (extras.get("groupId") != null) {
            groupId = (String) extras.get("groupId");
        }
        super.onCreate(savedInstanceState);
        binding =
                FunTeamSettingNewVlqHbListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.funTeamSettingNewVlqHbListActivityNav);
        _initView();

        _requestData();

    }

    @Override
    protected void _initView() {
        binding.funTeamSettingNewVlqHbListActivityNav.addCloseImageButton().setOnClickListener(this);
        // RecyclerView设置相关
        RecyclerView mRecyclerView = binding.funTeamSettingNewVlqHbListActivityRv;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);


        adapter.addOnItemChildClickListener(R.id.cell_fun_team_setting_vlq_hb_list_bg_iv, new BaseQuickAdapter.OnItemChildClickListener<CustomMsgBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<CustomMsgBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                CustomMsgBean item = baseQuickAdapter.getItem(i);
                clickRed(item);
            }
        });
    }

    protected void _requestData() {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;

        HttpUtil.apiW().red_vlqzsb(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<CustomMsgBean>>() {
                        }.getType();
                        List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), type);

                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }


    void clickRed(CustomMsgBean msgBean) {

        Context that = this;
        RegisterBean bean = new RegisterBean();
        bean.redpacketId = msgBean.id + "";
        HttpUtil.apiW().red_checkRedpacet(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        CustomMsgBean redBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                        if (redBean.type == 1 || redBean.type == 2 || redBean.type == 3) {
                            //可领取
                            if (redBean.type == 1) {
                                redBean.redPacketId = bean.redpacketId;
                            }
                            msgBean.result = msgBean;
                            FunOpenRedPacketFragment.showV(getSupportFragmentManager(), bean.redpacketId, redBean.type, groupId, msgBean, null, new FunOpenRedPacketFragment.OpenRedPacketBlock() {
                                @Override
                                public void hasOpen(IMMessage message) {
                                }
                            });
                        }
                        if (redBean.type == 4 || redBean.type == 5) {
                            /// 当前用户领取已领取过当前红包，展示领取详细信息
                            ///当红包是个人/专属，当前非目标领取用户，直接显示查看领取详情
                            HashMap map = new HashMap();
                            map.put("redpacketId",bean.redpacketId);
                            FunRedPacketResultActivity.start(FunRedPacketResultActivity.class,that,map);
                        }

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }
    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewVlqHbListActivityNav.addCloseImageButton()) {
            finish();
        }
    }
}
