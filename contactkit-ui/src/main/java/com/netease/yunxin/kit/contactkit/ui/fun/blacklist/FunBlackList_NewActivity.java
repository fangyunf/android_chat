// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.blacklist;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ActivityBlackListNewBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.blacklist.adapter.Fun_BlackList_NewAdapter;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunBlackList_NewActivity extends BaseActivity implements View.OnClickListener {

    ActivityBlackListNewBinding binding;
    Fun_BlackList_NewAdapter adapter = new Fun_BlackList_NewAdapter();

    GroupInfoBean _groupInfoBean;
    String _groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlackListNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getIntent().getStringExtra("groupId") != null) {
            _groupId = getIntent().getStringExtra("groupId");
            binding.activityBlackListNewNav.getTitleView().setText("群黑名单");
            _requestGroupData();
        } else {
            _requestPersonData();
        }
        binding.activityBlackListNewNav.addCloseImageButton().setOnClickListener(this);

        binding.activityBlackListNewRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityBlackListNewRv.setAdapter(adapter);
        adapter.addOnItemChildClickListener(R.id.cell_fun_blacklist_new_remove_tv, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (_groupId == null) {
                    GroupInfoBean bean = baseQuickAdapter.getItem(i);
                    String account = bean == null ? "" : bean.userId;
                    if (TextUtils.isEmpty(account)) {
                        account = bean == null ? "" : bean.memberCode;
                    }
                    if (TextUtils.isEmpty(account)) {
                        ToastUtils.toastMsg("移除失败");
                        return;
                    }
                    ContactRepo.removeBlackList(account, new FetchCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void param) {
                            ToastUtils.toastMsg("移除成功");
                            _requestPersonData();
                        }

                        @Override
                        public void onFailed(int code) {
                            ToastUtils.toastMsg("移除失败");
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                            ToastUtils.toastMsg("移除失败");
                        }
                    });
                } else {
                    RegisterBean registerBean = new RegisterBean();
                    registerBean.userId = baseQuickAdapter.getItem(i).userId;
                    registerBean.groupId = _groupId;
                    HttpUtil.apiW().group_addDeleteBlack(registerBean).enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            _requestGroupData();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
                }
            }
        });
        binding.activityBlackListNewSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.isEmpty()) {
                    adapter.setItems(_groupInfoBean.data);
                    adapter.notifyDataSetChanged();
                } else {
                    ArrayList<GroupInfoBean> tempArr = new ArrayList<>();
                    for (GroupInfoBean temp : _groupInfoBean.data) {
                        if (temp.name.contains(string)) {
                            tempArr.add(temp);
                        }
                    }
                    adapter.setItems(tempArr);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    protected void _requestPersonData() {
        super._requestData();
        ContactRepo.getBlackList(new FetchCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfo> param) {
                GroupInfoBean groupInfoBean = new GroupInfoBean();
                groupInfoBean.data = new ArrayList<>();
                if (param != null) {
                    for (UserInfo info : param) {
                        if (info == null) {
                            continue;
                        }
                        GroupInfoBean temp = new GroupInfoBean();
                        temp.userId = info.getAccount();
                        temp.memberCode = info.getAccount();
                        temp.name = info.getName();
                        temp.avatar = info.getAvatar();
                        groupInfoBean.data.add(temp);
                    }
                }
                _groupInfoBean = groupInfoBean;
                adapter.setItems(groupInfoBean.data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(int code) {
                ToastUtils.toastMsg("获取黑名单失败");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                ToastUtils.toastMsg("获取黑名单失败");
            }
        });
    }

    void _requestGroupData() {
        super._requestData();
        RegisterBean registerBean = new RegisterBean();
        registerBean.pageNo = "100";
        registerBean.groupId = _groupId;
        HttpUtil.apiW().group_groupBlackList(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                _groupInfoBean = new GroupInfoBean();
                _groupInfoBean.data = new Gson().fromJson(body.data.toString(), type);
//
                adapter.setItems(_groupInfoBean.data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBlackListNewNav.addCloseImageButton()) {
            finish();
        }
    }
}
