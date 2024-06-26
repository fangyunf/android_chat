// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.SearchEditText;
import com.nanchen.wavesidebar.Trans2PinYinUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberForbiddenListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewForbiddenListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewTeamUsersActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserListAdapter;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserMingDanListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class FunTeamSettingNew_ForbiddenListActivity extends BaseActivity implements View.OnClickListener {

    FunTeamSettingNewForbiddenListActivityBinding binding;
    String groupId;
    String opt_type;
    TeamSettingUserMingDanListAdapter adapter = new TeamSettingUserMingDanListAdapter();

    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    ArrayList<GroupInfoBean> mShowModels = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        _getParams();
        if (extras.get("groupId") != null) {
            groupId = (String) extras.get("groupId");
        }
        super.onCreate(savedInstanceState);
        binding =
                FunTeamSettingNewForbiddenListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();


    }

    @Override
    protected void _initView() {
        binding.funTeamSettingNewForbiddenListActivityNav.addCloseImageButton().setOnClickListener(this);
        // RecyclerView设置相关
        RecyclerView mRecyclerView = binding.funTeamSettingNewForbiddenListActivityRv;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(adapter);


        // 侧边设置相关
        WaveSideBarView mWaveSideBarView = binding.mainSideBar;
        mWaveSideBarView.setOnSelectIndexItemListener(new WaveSideBarView.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String letter) {
                for (int i=0; i<mContactModels.size(); i++) {
                    if (mContactModels.get(i).getIndex().equals(letter)) {
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });


        // 搜索按钮相关
        SearchEditText mSearchEditText = binding.funTeamSettingNewForbiddenListActivityEt;
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mShowModels.clear();
                for (GroupInfoBean model : mContactModels) {
                    String str = Trans2PinYinUtil.trans2PinYin(model.getName());
                    if (str.contains(s.toString()) || model.getName().contains(s.toString())) {
                        mShowModels.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        adapter.addOnItemChildClickListener(R.id.cell_fun_team_setting_users_mingdan_state_tv, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                GroupInfoBean item = baseQuickAdapter.getItem(i);
                int targetState = item.forbidState == 1 ? 0 : 1;


                RegisterBean bean = new RegisterBean();
                bean.groupId = groupId;
                ArrayList list = new ArrayList<>();
                list.add(item.userId);
                bean.members = list;
                bean.state = targetState;
                HttpUtil.apiW().groupMember_invitationGroupBanOnLooting(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg("设置成功");
                                item.forbidState = targetState;
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        });
    }

    @Override
    protected void _requestData() {
//        RegisterBean bean = new RegisterBean();
//        bean.groupId = groupId;
//        bean.pageNo = "1";
//        HttpUtil.apiW().groupMember_queryGroupMemberBanneds(bean)
//                        .enqueue(new CommonCallback<NetData>() {
//                            @Override
//                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                            }
//
//                            @Override
//                            public void Failure(Call<NetData> call, Throwable t) {
//
//                            }
//                        });
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = "1";
        HttpUtil.apiW().group_groupUserListPost(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        mContactModels = new Gson().fromJson(body.data.toString(), type);
                        updateUI();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    void updateUI() {

        adapter.contacts = mContactModels;
        adapter.setItems(mContactModels);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewForbiddenListActivityNav.addCloseImageButton()) {
            finish();
        }
    }
}
