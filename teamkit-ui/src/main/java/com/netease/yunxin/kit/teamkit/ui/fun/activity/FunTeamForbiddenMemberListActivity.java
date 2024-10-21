// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberForbiddenListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.adapter.FunTeamMemberListAdapter;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;

import retrofit2.Call;
import retrofit2.Response;

/** team member list activity */
public class FunTeamForbiddenMemberListActivity extends BaseTeamMemberListActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(com.yaoxin.appbase.R.color.color_nav_theme_color);
    _requestData();
  }

    private void _requestData() {

//        RegisterBean bean = new RegisterBean();
//        bean.groupId = teamId;
//        bean.pageNo = "0";
//        HttpUtil.apiW().groupMember_queryGroupMemberBanneds(bean)
//                .enqueue(new CommonCallback<NetData>() {
//                    @Override
//                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                    }
//
//                    @Override
//                    public void Failure(Call<NetData> call, Throwable t) {
//
//                    }
//                });
    }

    @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamMemberForbiddenListActivityBinding binding =
            FunTeamMemberForbiddenListActivityBinding.inflate(getLayoutInflater());
    ivBack = binding.ivBack;
    ivClear = binding.ivClear;
    groupEmpty = binding.groupEmtpy;
    rvMemberList = binding.rvMemberList;
    rvMemberList.addItemDecoration(
        new RecyclerView.ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
          }

          @Override
          public void onDraw(
              @NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);
          }
        });
    etSearch = binding.etSearch;
    return binding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      TeamTypeEnum typeEnum) {
    return new FunTeamMemberListAdapter(this, typeEnum, FunTeamMemberListItemBinding.class);
  }
}
