// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.addfriend;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.addfriend.BaseAddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAddFriendActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.adapter.FunAddFriendListAdapter;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.bean.FunAddFriendListBean;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BeanToMapUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class FunAddFriendActivity extends BaseAddFriendActivity {
  ArrayList<UserBean> dataList = new ArrayList<>();
  FunAddFriendListAdapter adapter = new FunAddFriendListAdapter();
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunAddFriendActivityBinding viewBinding =
        FunAddFriendActivityBinding.inflate(getLayoutInflater());
    etAddFriendAccount = viewBinding.etAddFriendAccount;
    ivFriendClear = viewBinding.ivFriendClear;
    addFriendEmptyLayout = viewBinding.addFriendEmptyLayout;
    viewBinding.funAddFriendActivityNav.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    viewBinding.funAddFriendActivityRv.setLayoutManager(new LinearLayoutManager(this));
    viewBinding.funAddFriendActivityRv.setAdapter(adapter);
    Context that = this;
    adapter.addOnItemChildClickListener(R.id.item_fun_addfriend_list_cell_add_tv, new BaseQuickAdapter.OnItemChildClickListener<UserBean>() {
      @Override
      public void onItemClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
        UserBean item = baseQuickAdapter.getItem(i);
        HashMap map = new HashMap();
        map.put("user",new Gson().toJson(item));
        FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class,that, map);
      }
    });

    etAddFriendAccount.setOnEditorActionListener(actionListener);

    return viewBinding.getRoot();
  }
  private final EditText.OnEditorActionListener actionListener =
          (v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
              String accountId = v.getEditableText().toString();
              if (!TextUtils.isEmpty(accountId)) {
                HttpUtil.api8444().friends_search(accountId)
                        .enqueue(new CommonCallback<NetData>() {
                          @Override
                          public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userInfo = new Gson().fromJson(body.data.toString(), UserBean.class);
                            startProfileActivity(userInfo);
                          }

                          @Override
                          public void Failure(Call<NetData> call, Throwable t) {

                          }
                        });
              }
            }
            return false;
          };
  protected void startProfileActivity(UserBean userInfo) {
    if (userInfo == null) {
      return;
    }
    dataList.clear();
    dataList.add(userInfo);
    adapter.setItems(dataList);
    adapter.notifyDataSetChanged();
//    return;
//    if (TextUtils.equals(userInfo.getAccount(), IMKitClient.account())) {
//      XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE).withContext(this).navigate();
//    } else {
//      XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
//          .withContext(this)
//          .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccount())
//          .navigate();
//    }
  }
}
