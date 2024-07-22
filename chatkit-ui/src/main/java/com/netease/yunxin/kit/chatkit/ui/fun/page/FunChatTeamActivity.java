// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatTeamFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.StatusBarUtils;

import retrofit2.Call;
import retrofit2.Response;

/** Fun皮肤群聊聊天页面，逻辑在Fragment中实现，该Activity只做Fragment你管理 */
public class FunChatTeamActivity extends ChatBaseActivity {

  FunChatTeamFragment chatFragment;

  private static final String TAG = "ChatGroupActivity";

  @Override
  public void initChat() {
    Team teamInfo = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String teamId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    AppProxy.getInstance().setTeamId(teamId);
    if (teamInfo == null && TextUtils.isEmpty(teamId)) {
      ALog.e(LIB_TAG, TAG, "team info is null && team id is null" + teamId);
      finish();
      return;
    }
    //set fragment
    chatFragment = new FunChatTeamFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_KRY, teamInfo);
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, teamId);
    IMMessage message = (IMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
    if (message != null) {
      bundle.putSerializable(RouterConstant.KEY_MESSAGE, message);
    }
    chatFragment.setArguments(bundle);
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().add(R.id.container, chatFragment).commitAllowingStateLoss();
//    StatusBarUtils.setStatusBarLightMode(this, true, true);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.e(LIB_TAG, TAG, "onNewIntent");
    chatFragment.onNewIntent(intent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
