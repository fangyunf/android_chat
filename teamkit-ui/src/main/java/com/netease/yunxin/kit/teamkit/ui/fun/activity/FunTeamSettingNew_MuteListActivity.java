// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.SearchEditText;
import com.nanchen.wavesidebar.Trans2PinYinUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewMuteListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserMingDanListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 群管理 - 单人禁言名单（云信 TeamService.muteTeamMember）
 */
public class FunTeamSettingNew_MuteListActivity extends BaseActivity implements View.OnClickListener {

    private FunTeamSettingNewMuteListActivityBinding binding;
    private String groupId;
    private final TeamSettingUserMingDanListAdapter adapter = new TeamSettingUserMingDanListAdapter();
    private final ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    private final ArrayList<GroupInfoBean> mShowModels = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        _getParams();
        if (extras != null && extras.get("groupId") != null) {
            groupId = (String) extras.get("groupId");
        }
        super.onCreate(savedInstanceState);
        binding = FunTeamSettingNewMuteListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        _requestData(1);
    }

    @Override
    protected void _initView() {
        adapter.listType = TeamSettingUserMingDanListAdapter.LIST_TYPE_MUTE;
        binding.funTeamSettingNewMuteListActivityNav.addCloseImageButton().setOnClickListener(this);

        RecyclerView recyclerView = binding.funTeamSettingNewMuteListActivityRv;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(
                1,
                (parent, adapterPosition) -> true);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);

        WaveSideBarView sideBarView = binding.mainSideBar;
        sideBarView.setOnSelectIndexItemListener(
                letter -> {
                    for (int i = 0; i < mShowModels.size(); i++) {
                        if (mShowModels.get(i).getIndex().equals(letter)) {
                            ((LinearLayoutManager) recyclerView.getLayoutManager())
                                    .scrollToPositionWithOffset(i, 0);
                            return;
                        }
                    }
                });

        SearchEditText searchEditText = binding.funTeamSettingNewMuteListActivityEt;
        searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        mShowModels.clear();
                        String keyword = s == null ? "" : s.toString();
                        if (TextUtils.isEmpty(keyword)) {
                            mShowModels.addAll(mContactModels);
                        } else {
                            for (GroupInfoBean model : mContactModels) {
                                String pinYin = Trans2PinYinUtil.trans2PinYin(model.getName());
                                if (pinYin.contains(keyword) || model.getName().contains(keyword)) {
                                    mShowModels.add(model);
                                }
                            }
                        }
                        adapter.contacts = mShowModels;
                        adapter.setItems(mShowModels);
                        adapter.notifyDataSetChanged();
                    }
                });

        adapter.addOnItemChildClickListener(
                R.id.cell_fun_team_setting_users_mingdan_state_tv,
                new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
                    @Override
                    public void onItemClick(
                            @NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter,
                            @NonNull View view,
                            int position) {
                        GroupInfoBean item = baseQuickAdapter.getItem(position);
                        if (item == null
                                || TextUtils.isEmpty(item.userId)
                                || TextUtils.equals(item.userId, DataUtil.getUserid())) {
                            return;
                        }
                        boolean mute = item.muteState != 1;
                        NIMClient.getService(TeamService.class)
                                .muteTeamMember(groupId, item.userId, mute)
                                .setCallback(
                                        new RequestCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void param) {
                                                ToastUtils.toastMsg(mute ? "已禁言" : "已解除禁言");
                                                item.muteState = mute ? 1 : 0;
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onFailed(int code) {
                                                ToastUtils.toastMsg("操作失败(" + code + ")");
                                            }

                                            @Override
                                            public void onException(Throwable exception) {
                                                ToastUtils.toastMsg("操作失败");
                                            }
                                        });
                    }
                });
    }

    protected void _requestData(int page) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page + "";
        bean.pageNo = "100";
        HttpUtil.apiW()
                .group_groupUserListPost(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                                List<GroupInfoBean> tempList =
                                        new Gson().fromJson(body.data.toString(), type);
                                if (tempList != null && !tempList.isEmpty()) {
                                    mContactModels.addAll(tempList);
                                    if (tempList.size() == 100) {
                                        _requestData(page + 1);
                                        return;
                                    }
                                }
                                syncMuteStateFromNim();
                                updateUI();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }

    private void syncMuteStateFromNim() {
        TeamService teamService = NIMClient.getService(TeamService.class);
        for (GroupInfoBean bean : mContactModels) {
            if (TextUtils.isEmpty(bean.userId)) {
                bean.muteState = 0;
                continue;
            }
            TeamMember member = teamService.queryTeamMemberBlock(groupId, bean.userId);
            bean.muteState = (member != null && member.isMute()) ? 1 : 0;
        }
    }

    private void updateUI() {
        mShowModels.clear();
        mShowModels.addAll(mContactModels);
        adapter.contacts = mShowModels;
        adapter.setItems(mShowModels);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewMuteListActivityNav.addCloseImageButton()) {
            finish();
        }
    }
}
