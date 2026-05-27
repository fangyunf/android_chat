// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchMessageActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.FunSearchMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchBaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/** Fun皮肤搜索页面，继承自ChatSearchBaseActivity */
public class FunChatSearchActivity extends ChatSearchBaseActivity {
  private static final int FILTER_NONE = 0;
  private static final int FILTER_TEXT = 1;
  private static final int FILTER_MEDIA = 2;
  private static final int FILTER_FILE = 3;
  private static final int FILTER_DATE = 4;
  private static final int TAB_SELECTED_COLOR = Color.parseColor("#3B5CC9");
  private static final int TAB_NORMAL_COLOR = Color.parseColor("#8FA1D8");
  private static final int LOCAL_QUERY_LIMIT = 1000;

  private final String TAG = "ChatSearchFunActivity";
  private FunChatSearchMessageActivityBinding viewBinding;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
  private ActivityResultLauncher<Intent> memberSelectorLauncher;
  private GroupInfoBean groupInfoBean;
  private boolean isP2pMode;
  private String sessionId;
  private SessionTypeEnum sessionType = SessionTypeEnum.Team;
  private int currentFilter = FILTER_NONE;
  private String selectedMemberId = "";
  private String selectedMemberName = "";
  private long selectedDateStart = 0L;
  private long selectedDateEnd = 0L;
  private int searchRequestSerial = 0;

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(com.yaoxin.appbase.R.color.color_F2F2F2);
    viewBinding = FunChatSearchMessageActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    searchRV = viewBinding.searchRv;
    searchET = viewBinding.searchEt;
    clearIV = viewBinding.clearIv;
    emptyLayout = viewBinding.emptyLayout;
    viewBinding.searchRv.addItemDecoration(getItemDecoration());
    viewBinding.emptyTv.setText("还没有数据哦~");

    viewBinding.searchTitleBar.enableUnderDivider(false);
    StatusBarUtils.setStatusBarLightMode(this, true, true);
    ViewGroup.LayoutParams navParams = viewBinding.searchTitleBar.getLayoutParams();
    if (navParams.height > 0) {
      navParams.height = navParams.height + BarUtils.getStatusBarHeight();
      viewBinding.searchTitleBar.setLayoutParams(navParams);
    }
    viewBinding.searchTitleBar.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
    viewBinding.searchTitleBar.addCloseImageButton().setOnClickListener(v -> finish());
    viewBinding.cancelBtn.setOnClickListener(v -> finish());
    Team teamParam = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String p2pAccId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    isP2pMode = teamParam == null && !TextUtils.isEmpty(p2pAccId);
    if (isP2pMode) {
      sessionId = p2pAccId;
      sessionType = SessionTypeEnum.P2P;
      viewBinding.tabMemberTv.setVisibility(View.GONE);
    }
    memberSelectorLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
              }
              String userInfoJson = result.getData().getStringExtra("userInfo");
              if (TextUtils.isEmpty(userInfoJson)) {
                return;
              }
              GroupInfoBean member = new Gson().fromJson(userInfoJson, GroupInfoBean.class);
              if (member == null || TextUtils.isEmpty(member.userId)) {
                return;
              }
              selectedMemberId = member.userId;
              selectedMemberName = member.name;
              currentFilter = FILTER_TEXT;
              updateFilterViews();
              performSearch();
            });
  }

  @Override
  protected void bindingView() {
    if (searchRV != null) {
      searchAdapter = new com.netease.yunxin.kit.chatkit.ui.page.adapter.SearchMessageAdapter();
      searchRV.setLayoutManager(new LinearLayoutManager(this));
      searchRV.setAdapter(searchAdapter);
    }
    if (clearIV != null) {
      clearIV.setOnClickListener(v -> resetAllFilters());
    }
    if (searchET != null) {
      searchET.addTextChangedListener(
          new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
              updateFilterViews();
              performSearch();
            }
          });
    }
    if (!isP2pMode) {
      viewBinding.tabMemberTv.setOnClickListener(v -> openMemberSelector());
    }
    viewBinding.tabMediaTv.setOnClickListener(v -> toggleSimpleFilter(FILTER_MEDIA));
    viewBinding.tabFileTv.setOnClickListener(v -> toggleSimpleFilter(FILTER_FILE));
    viewBinding.tabDateTv.setOnClickListener(v -> showDatePicker());
    searchAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View v, BaseBean data, int position) {
            ALog.d(LIB_TAG, TAG, "item onClick position:" + position);
            KeyboardUtils.hideKeyboard(FunChatSearchActivity.this);
            if (isP2pMode) {
              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                  .withParam(RouterConstant.KEY_MESSAGE, data.param)
                  .withParam(RouterConstant.CHAT_ID_KRY, sessionId)
                  .withContext(FunChatSearchActivity.this)
                  .navigate();
            } else {
              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                  .withParam(data.paramKey, data.param)
                  .withParam(RouterConstant.CHAT_KRY, team)
                  .withContext(FunChatSearchActivity.this)
                  .navigate();
            }
            return true;
          }

          @Override
          public boolean onLongClick(View v, BaseBean data, int position) {
            return false;
          }
        });
    searchAdapter.setViewHolderFactory(
        (parent, viewType) ->
            new FunSearchMessageViewHolder(
                FunChatSearchViewHolderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false)));
  }

  @Override
  protected void initData() {
    team = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String p2pAccId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (team != null) {
      isP2pMode = false;
      sessionId = team.getId();
      sessionType = SessionTypeEnum.Team;
      requestGroupInfo();
    } else if (isP2pMode) {
      // sessionId/sessionType 已在 initViewAndSetContentView 初始化
    } else {
      finish();
      return;
    }
    updateFilterViews();
    if (isP2pMode) {
      // 私聊进入即加载本地聊天记录，无需先输入关键词或选择筛选
      performSearch();
    } else {
      showEmpty(true);
      searchAdapter.setData(new ArrayList<>());
    }
  }

  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(1);

      @Override
      public void getItemOffsets(
          @NonNull Rect outRect,
          @NonNull View view,
          @NonNull RecyclerView parent,
          @NonNull RecyclerView.State state) {
        outRect.set(0, topPadding, 0, 0);
      }
    };
  }

  private void requestGroupInfo() {
    HttpUtil.apiW()
        .group_groupHomeInfo(team.getId())
        .enqueue(
            new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (body != null && body.data != null) {
                  groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                }
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {}
            });
  }

  private void openMemberSelector() {
    if (groupInfoBean == null) {
      ToastUtils.toastMsg("群成员加载中");
      return;
    }
    DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
    XKitRouter.withKey(Constant.FunSelected_User_ActivityKey)
        .withParam("type", "7")
        .withParam("groupId", team.getId())
        .withContext(this)
        .navigate(memberSelectorLauncher);
  }

  private void toggleSimpleFilter(int targetFilter) {
    if (currentFilter == targetFilter) {
      currentFilter = FILTER_NONE;
    } else {
      currentFilter = targetFilter;
    }
    if (currentFilter != FILTER_DATE) {
      selectedDateStart = 0L;
      selectedDateEnd = 0L;
    }
    updateFilterViews();
    performSearch();
  }

  private void showDatePicker() {
    Calendar calendar = Calendar.getInstance();
    if (selectedDateStart > 0L) {
      calendar.setTimeInMillis(selectedDateStart);
    }
    DatePickerDialog dialog =
        new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
              Calendar startCalendar = Calendar.getInstance();
              startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
              startCalendar.set(Calendar.MILLISECOND, 0);
              Calendar endCalendar = Calendar.getInstance();
              endCalendar.set(year, month, dayOfMonth, 23, 59, 59);
              endCalendar.set(Calendar.MILLISECOND, 999);
              currentFilter = FILTER_DATE;
              selectedDateStart = startCalendar.getTimeInMillis();
              selectedDateEnd = endCalendar.getTimeInMillis();
              updateFilterViews();
              performSearch();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));
    dialog.show();
  }

  private void resetAllFilters() {
    searchRequestSerial++;
    currentFilter = FILTER_NONE;
    selectedMemberId = "";
    selectedMemberName = "";
    selectedDateStart = 0L;
    selectedDateEnd = 0L;
    searchET.setText("");
    updateFilterViews();
    if (isP2pMode) {
      performSearch();
    } else {
      searchAdapter.setData(new ArrayList<>());
      showEmpty(true);
    }
  }

  private void performSearch() {
    String keyword = getKeyword();
    if (TextUtils.isEmpty(sessionId) || !shouldSearch(keyword)) {
      searchRequestSerial++;
      searchAdapter.setData(new ArrayList<>());
      showEmpty(true);
      return;
    }
    queryLocalHistory(keyword);
  }

  private void queryLocalHistory(String keyword) {
    final int requestSerial = ++searchRequestSerial;
    long anchorTime =
        currentFilter == FILTER_DATE && selectedDateEnd > 0L
            ? selectedDateEnd + 1
            : System.currentTimeMillis() + 1;
    long toTime = currentFilter == FILTER_DATE ? selectedDateStart : 0L;
    IMMessage anchor = MessageBuilder.createEmptyMessage(sessionId, sessionType, anchorTime);
    NIMClient.getService(MsgService.class)
        .queryMessageListByTypesV2(
            getLocalQueryTypes(),
            anchor,
            toTime,
            QueryDirectionEnum.QUERY_OLD,
            LOCAL_QUERY_LIMIT,
            false)
        .setCallback(
            new RequestCallback<List<IMMessage>>() {
              @Override
              public void onSuccess(List<IMMessage> param) {
                if (requestSerial != searchRequestSerial) {
                  return;
                }
                List<ChatSearchBean> result = new ArrayList<>();
                if (param != null) {
                  for (IMMessage message : param) {
                    if (!shouldIncludeMessage(message)) {
                      continue;
                    }
                    if (!TextUtils.isEmpty(selectedMemberId)
                        && !selectedMemberId.equals(message.getFromAccount())) {
                      continue;
                    }
                    ChatSearchBean bean = new ChatSearchBean(message, keyword);
                    if (!matchesKeyword(bean, keyword)) {
                      continue;
                    }
                    result.add(bean);
                  }
                }
                searchAdapter.setData(result);
                showEmpty(result.isEmpty());
              }

              @Override
              public void onFailed(int code) {
                if (requestSerial != searchRequestSerial) {
                  return;
                }
                searchAdapter.setData(new ArrayList<>());
                showEmpty(true);
              }

              @Override
              public void onException(Throwable exception) {
                if (requestSerial != searchRequestSerial) {
                  return;
                }
                searchAdapter.setData(new ArrayList<>());
                showEmpty(true);
              }
            });
  }

  private boolean shouldSearch(String keyword) {
    if (isP2pMode && currentFilter == FILTER_NONE && TextUtils.isEmpty(keyword)) {
      return true;
    }
    if (!TextUtils.isEmpty(keyword)) {
      return true;
    }
    if (!isP2pMode && !TextUtils.isEmpty(selectedMemberId)) {
      return true;
    }
    if (currentFilter == FILTER_DATE) {
      return selectedDateStart > 0L && selectedDateEnd > 0L;
    }
    return currentFilter == FILTER_TEXT
        || currentFilter == FILTER_MEDIA
        || currentFilter == FILTER_FILE;
  }

  @Nullable
  private List<MsgTypeEnum> getLocalQueryTypes() {
    if (currentFilter == FILTER_TEXT) {
      return Collections.singletonList(MsgTypeEnum.text);
    }
    if (currentFilter == FILTER_MEDIA) {
      return Arrays.asList(MsgTypeEnum.image, MsgTypeEnum.video);
    }
    if (currentFilter == FILTER_FILE) {
      return Collections.singletonList(MsgTypeEnum.file);
    }
    return null;
  }

  private boolean matchesKeyword(ChatSearchBean bean, String keyword) {
    if (TextUtils.isEmpty(keyword)) {
      return true;
    }
    return bean.getSearchText().contains(keyword);
  }

  private boolean shouldIncludeMessage(IMMessage message) {
    if (message == null || message.getMsgType() == null) {
      return false;
    }
    MsgTypeEnum msgType = message.getMsgType();
    return msgType != MsgTypeEnum.notification && msgType != MsgTypeEnum.tip;
  }

  private void updateFilterViews() {
    String prefix = "";
    if (!TextUtils.isEmpty(selectedMemberName)) {
      prefix = selectedMemberName;
    } else if (currentFilter == FILTER_DATE && selectedDateStart > 0L) {
      prefix = dateFormat.format(new Date(selectedDateStart));
    }
    viewBinding.filterPrefixTv.setVisibility(TextUtils.isEmpty(prefix) ? View.GONE : View.VISIBLE);
    viewBinding.filterPrefixTv.setText(prefix);
    viewBinding.filterPrefixTv.setTextColor(TAB_SELECTED_COLOR);
    updateSearchHint();
    boolean showClear =
        !TextUtils.isEmpty(getKeyword())
            || currentFilter == FILTER_TEXT
            || currentFilter == FILTER_MEDIA
            || currentFilter == FILTER_FILE
            || !TextUtils.isEmpty(selectedMemberId)
            || (currentFilter == FILTER_DATE && selectedDateStart > 0L);
    clearIV.setVisibility(showClear ? View.VISIBLE : View.GONE);
    if (!isP2pMode) {
      setTabSelected(viewBinding.tabMemberTv, !TextUtils.isEmpty(selectedMemberId));
    }
    setTabSelected(viewBinding.tabMediaTv, currentFilter == FILTER_MEDIA);
    setTabSelected(viewBinding.tabFileTv, currentFilter == FILTER_FILE);
    setTabSelected(viewBinding.tabDateTv, currentFilter == FILTER_DATE);
  }

  private void updateSearchHint() {
    if (searchET == null) {
      return;
    }
    if (currentFilter == FILTER_TEXT) {
      searchET.setHint("搜索文本消息");
    } else if (currentFilter == FILTER_MEDIA) {
      searchET.setHint("搜索图片和视频");
    } else if (currentFilter == FILTER_FILE) {
      searchET.setHint("搜索文件");
    } else if (currentFilter == FILTER_DATE) {
      searchET.setHint("搜索当天消息");
    } else {
      searchET.setHint("查聊天记录");
    }
  }

  private void setTabSelected(TextView textView, boolean selected) {
    textView.setTextColor(selected ? TAB_SELECTED_COLOR : TAB_NORMAL_COLOR);
  }

  private String getKeyword() {
    return searchET == null ? "" : searchET.getText().toString().trim();
  }
}
