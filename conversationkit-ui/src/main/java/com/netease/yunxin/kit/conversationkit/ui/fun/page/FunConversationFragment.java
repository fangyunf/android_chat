// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunPopItemFactory;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.Bean.ConversationCustomInfoBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.PreferenceUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.sunfusheng.marqueeview.IMarqueeItem;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Response;

public class FunConversationFragment extends ConversationBaseFragment {


    private int nimUnread = 0;
    private int sysNoticeUnread = 0;
    private int applyUnread = 0;
    private int friendApplyUnread = 0;
    private int finishCount = 0; // 记录完成的请求数
    private final int TOTAL_REQUESTS = 3;

    private FunConversationFragmentBinding viewBinding;

    private boolean _needRefresh;
    private String noticeText = "";
    /**
     * 与顶部 tvNoticeCount 一致，用于列表头「系统公告」角标
     */
    private int cachedTotalNoticeUnread;
    private RecyclerView.Adapter<?> headerAdapterRef;
    private int topIndex;
    private Dialog searchDialog;
    private View searchDialogRoot;
    private View searchDimV;
    private LinearLayout searchContentLl;
    private LinearLayout searchPanelLl;
    private EditText searchEt;
    private ImageView searchClearIv;
    private ConversationView searchConversationView;
    private OnBackPressedCallback searchBackCallback;
    private boolean searchDialogInited;

    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();

    public FunConversationFragment() {
    }

    // 创建实例的方法，使用 arguments 传递参数
    public static FunConversationFragment newInstance(int type) {
        FunConversationFragment fragment = new FunConversationFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View initViewAndGetRootView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewBinding = FunConversationFragmentBinding.inflate(inflater, container, false);
        // 获取传递的参数
        if (getArguments() != null) {
            _type = getArguments().getInt("type", _type);
        }
        initView();
        if (_type == 1) {
            viewBinding.funConversationFragmentTitleTv.setText("群聊");
        } else if (_type == 3) {
            viewBinding.funConversationFragmentTitleTv.setText("消息");
        } else {
            viewBinding.funConversationFragmentTitleTv.setText("对话");
        }
        setupFixedHeader();
        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewBinding.funConversationFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        viewBinding.funConversationFragmentTopLl.setLayoutParams(layoutParams);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // 这里写你想延时执行的代码
                requireActivity().runOnUiThread(() -> requestMsg());
            }
        }, 1500);
        EventBus.getDefault().register(this);
        return viewBinding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_type == 0 || _type == 3) {
            dismissSearchOverlay();
        } else if (!AppProxy.searchKeyWord1.isEmpty()) {
            AppProxy.searchKeyWord1 = "";
            conversationView.adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        if (searchDialog != null) {
            searchDialog.dismiss();
            searchDialog = null;
            searchDialogInited = false;
        }
        super.onDestroyView();
    }

    private void ensureSearchDialog() {
        if (searchDialogInited || (_type != 0 && _type != 3)) {
            return;
        }
        searchDialog = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar);
        searchDialog.setContentView(R.layout.fun_conversation_search_dialog);
        Window dialogWindow = searchDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialogWindow.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        searchDialogRoot = searchDialog.findViewById(R.id.fun_conversation_search_dialog_root);
        searchDimV = searchDialog.findViewById(R.id.fun_conversation_fragment_search_dim_v);
        searchContentLl = searchDialog.findViewById(R.id.fun_conversation_search_content_ll);
        searchPanelLl = searchDialog.findViewById(R.id.fun_conversation_fragment_search_panel_ll);
        searchEt = searchDialog.findViewById(R.id.fun_conversation_fragment_et);
        searchClearIv = searchDialog.findViewById(R.id.fun_conversation_fragment_search_clear_iv);
        searchConversationView =
                searchDialog.findViewById(R.id.fun_conversation_fragment_search_conversation_view);
        searchConversationView._type = _type;
        searchConversationView.adapter._type = _type;
        searchConversationView.setViewHolderFactory(new FunViewHolderFactory());
        searchConversationView.addItemDecoration(getItemDecoration());
        searchConversationView.setItemClickListener(createConversationClickListener());
        searchConversationView.getRecyclerView().setBackgroundColor(0xFFFFFFFF);
        searchDimV.setOnClickListener(v -> dismissSearchOverlay());
        searchClearIv.setOnClickListener(v -> searchEt.setText(""));
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                searchClearIv.setVisibility(string.isEmpty() ? View.GONE : View.VISIBLE);
                updateSearchDialogStyle(!string.isEmpty());
                filterSearchResults(string);
            }
        });
        searchDialogInited = true;
    }

    private ViewHolderClickListener createConversationClickListener() {
        return new ViewHolderClickListener() {
            @Override
            public boolean onClick(View v, BaseBean data, int position) {
                if (data.param != null) {
                    String targetId = (String) data.param;
                    if (targetId.equals(DataUtil.getXiaoZhuShouId())) {
                        dismissSearchOverlay();
                        XKitRouter.withKey(Constant.XiaoZhuShouActivityKey)
                                .withContext(requireContext())
                                .navigate();
                        return true;
                    }
                }
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                        && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                        && data instanceof ConversationBean) {
                    result =
                            ConversationKitClient.getConversationUIConfig()
                                    .itemClickListener
                                    .onClick(
                                            getContext(),
                                            (ConversationBean) data,
                                            position);
                }
                if (!result) {
                    dismissSearchOverlay();
                    XKitRouter.withKey(data.router)
                            .withParam(data.paramKey, data.param)
                            .withContext(requireContext())
                            .navigate();
                }
                return true;
            }

            @Override
            public boolean onAvatarClick(View v, BaseBean data, int position) {
                if (data.param != null) {
                    String targetId = (String) data.param;
                    if (targetId.equals(DataUtil.getXiaoZhuShouId())) {
                        dismissSearchOverlay();
                        XKitRouter.withKey(Constant.XiaoZhuShouActivityKey)
                                .withContext(requireContext())
                                .navigate();
                        return true;
                    }
                }
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                        && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                        && data instanceof ConversationBean) {
                    result =
                            ConversationKitClient.getConversationUIConfig()
                                    .itemClickListener
                                    .onAvatarClick(
                                            getContext(),
                                            (ConversationBean) data,
                                            position);
                }
                if (!result) {
                    dismissSearchOverlay();
                    XKitRouter.withKey(data.router)
                            .withParam(data.paramKey, data.param)
                            .withContext(requireContext())
                            .navigate();
                }
                return true;
            }

            @Override
            public boolean onLongClick(View v, BaseBean data, int position) {
                return false;
            }

            @Override
            public boolean onAvatarLongClick(View v, BaseBean data, int position) {
                return false;
            }
        };
    }

    private String getConversationDisplayName(ConversationBean data) {
        if (data == null || data.infoData == null) {
            return "";
        }
        if (data.param != null) {
            String targetId = (String) data.param;
            if (targetId.equals(DataUtil.getKeFuId())) {
                return "客服";
            }
            if (targetId.equals(DataUtil.getXiaoZhuShouId())) {
                return "官方小助手";
            }
        }
        if (data.infoData.getTeamInfo() != null) {
            return data.infoData.getTeamInfo().getName();
        }
        String name = data.infoData.getName();
        return name == null ? "" : name;
    }

    private void filterSearchResults(String keyword) {
        if (searchConversationView == null) {
            return;
        }
        List<ConversationBean> filtered = new ArrayList<>();
        if (!TextUtils.isEmpty(keyword)) {
            for (ConversationBean bean : conversationList) {
                if (getConversationDisplayName(bean).contains(keyword)) {
                    filtered.add(bean);
                }
            }
        }
        searchConversationView.setData(filtered);
    }

    private void updateSearchDialogStyle(boolean searching) {
        if (searchDimV == null || searchContentLl == null || searchConversationView == null) {
            return;
        }
        searchDimV.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams contentLp =
                (FrameLayout.LayoutParams) searchContentLl.getLayoutParams();
        contentLp.height =
                searching ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        contentLp.gravity = Gravity.TOP;
        searchContentLl.setLayoutParams(contentLp);
        searchConversationView.setVisibility(searching ? View.VISIBLE : View.GONE);
    }

    private void updateSearchDialogPosition() {
        if (viewBinding == null || searchDialogRoot == null || searchContentLl == null) {
            return;
        }
        searchDialogRoot.setPadding(0, 0, 0, viewBinding.bottomLayout.getHeight());
        FrameLayout.LayoutParams contentLp =
                (FrameLayout.LayoutParams) searchContentLl.getLayoutParams();
        contentLp.topMargin =
                viewBinding.topLayout.getTop()
                        + viewBinding.funConversationFragmentTopLl.getBottom();
        contentLp.gravity = Gravity.TOP;
        searchContentLl.setLayoutParams(contentLp);
    }

    private void setupSearchOverlay() {
        if (_type != 0 && _type != 3) {
            return;
        }
        searchBackCallback =
                new OnBackPressedCallback(false) {
                    @Override
                    public void handleOnBackPressed() {
                        dismissSearchOverlay();
                    }
                };
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), searchBackCallback);
    }

    private void showSearchOverlay() {
        ensureSearchDialog();
        if (searchDialog == null) {
            return;
        }
        searchEt.setText("");
        searchClearIv.setVisibility(View.GONE);
        filterSearchResults("");
        updateSearchDialogStyle(false);
        searchDialog.show();
        viewBinding.funConversationFragmentTopLl.post(() -> {
            updateSearchDialogPosition();
            updateSearchDialogStyle(false);
        });
        if (searchBackCallback != null) {
            searchBackCallback.setEnabled(true);
        }
        searchEt.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchEt, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void dismissSearchOverlay() {
        if (searchDialog == null || !searchDialog.isShowing()) {
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && searchEt != null) {
            imm.hideSoftInputFromWindow(searchEt.getWindowToken(), 0);
        }
        searchDialog.dismiss();
        if (searchClearIv != null) {
            searchClearIv.setVisibility(View.GONE);
        }
        updateSearchDialogStyle(false);
        if (searchEt != null) {
            searchEt.setText("");
        }
        filterSearchResults("");
        if (searchBackCallback != null) {
            searchBackCallback.setEnabled(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("refresh_chat_list")) {
            _needRefresh = true;
        } else if ("refresh_notice".equals(event.getTag())) {
            getMessageCount();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_needRefresh) {
            _needRefresh = false;
            requestMsg();
        }
        getMessageCount();
    }

    @Override
    protected void finishLoadData() {
        if (viewBinding != null) {
            viewBinding.refreshLayout.finishRefresh();
        }
        super.finishLoadData();
        _requestData();
    }

    void _requestData() {

        HttpUtil.apiW().customer_notice()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        GroupInfoBean groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        if (groupInfoBean != null && groupInfoBean.content != null) {

                            String message = groupInfoBean.content;
                            if (message != null && !message.isEmpty()) {
                                noticeText = message;
                                // refresh header text if header exists
                                if (headerAdapterRef != null) {
                                    headerAdapterRef.notifyItemChanged(0);
                                }
                                viewBinding.marqueeView.startWithText(message);
                                viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);
                            } else {
                                message = "";
                                noticeText = message;
                                if (headerAdapterRef != null) {
                                    headerAdapterRef.notifyItemChanged(0);
                                }
                                viewBinding.marqueeView.startWithText(message);
                                viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                            }
                        } else {
                            String message = "";
                            noticeText = message;
                            if (headerAdapterRef != null) {
                                headerAdapterRef.notifyItemChanged(0);
                            }
                            viewBinding.marqueeView.startWithText(message);
                            viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                        String message = "";
                        noticeText = message;
                        if (headerAdapterRef != null) {
                            headerAdapterRef.notifyItemChanged(0);
                        }
                        viewBinding.marqueeView.startWithText(message);
                        viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                    }
                });
        if (_type == 1 || _type == 3) {
            HttpUtil.apiW().group_userGroups(new RegisterBean())
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            Type type = new TypeToken<List<GroupInfoBean>>() {
                            }.getType();

                            List<GroupInfoBean> dataList = new Gson().fromJson(body.data.toString(), type);

                            for (GroupInfoBean tempGroupInfo : dataList) {
                                boolean hasConversation = false;
                                for (ConversationBean tempCoversation : conversationList) {

                                    if (tempGroupInfo.groupId.equals((String) tempCoversation.param)) {
                                        hasConversation = true;
                                        break;
                                    }
                                }
                                if (!hasConversation) {
                                    sendGroupMessage(tempGroupInfo.groupId);
                                }
                            }
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }

        if (_type == 0 || _type == 3) {
            HttpUtil.apiW().customer_systemAppUser(new RegisterBean())
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            String kefuId = body.data.toString().replace("\"", "");
                            requestKefu(kefuId);

                            String xiaozhushouId = "10086";
                            DataUtil.putKeFuId(kefuId);
                            DataUtil.putXiaoZhuShouId(xiaozhushouId);

                            boolean hasKefu = false;
                            boolean hasXiaoZhushou = false;

                            for (ConversationBean tempBean :
                                    conversationList) {
                                if (tempBean.infoData.getContactId().equals(kefuId)) {
                                    hasKefu = true;

                                }
                            }
                            for (ConversationBean tempBean :
                                    conversationList) {
                                if (tempBean.infoData.getContactId().equals(xiaozhushouId)) {
                                    hasXiaoZhushou = true;

                                }
                            }
//                            if (!hasKefu) {
//                                sendMessage(kefuId);
//                            }

                            if (!hasXiaoZhushou) {
                                sendMessage(xiaozhushouId);
                            }

                            loadFriendList();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }


    //获取消息数量
    private void getMessageCount() {
//        sendMessage(DataUtil.getUserid());
        nimUnread = 0;
        sysNoticeUnread = 0;
        applyUnread = 0;
        friendApplyUnread = 0;
        finishCount = 0;
        // 1. 网易云信未读
        NIMClient.getService(MsgService.class).queryUnreadMessageList(DataUtil.getUserid(), SessionTypeEnum.P2P).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> result) {
                nimUnread += result.size();
                onOneRequestFinish();
            }

            @Override
            public void onFailed(int code) {
                onOneRequestFinish();
            }

            @Override
            public void onException(Throwable exception) {
                onOneRequestFinish();
            }
        });
//        NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallback<List<RecentContact>>() {
//            @Override
//            public void onSuccess(List<RecentContact> recents) {
//                for (RecentContact recent : recents) {
//                    if (recent.getContactId().equals(DataUtil.getUserid())) {  // targetId 是你要查询的会话ID
//                        nimUnread += recent.getUnreadCount();
//                    }
//                }
//                onOneRequestFinish();
//            }
//
//            @Override
//            public void onFailed(int code) {
//                onOneRequestFinish();
//            }
//
//            @Override
//            public void onException(Throwable exception) {
//                onOneRequestFinish();
//            }
//        });

        // 2. 系统通知未读
        HttpUtil.apiW().customer_noticeList().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                int oldCount = PreferenceUtils.INSTANCE.getInt("sysNotice", 0);
                sysNoticeUnread = Math.max(0, tempList.size() - oldCount);
                onOneRequestFinish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                onOneRequestFinish();
            }
        });

        // 3. 好友申请未读
        HttpUtil.apiW().friends_applyListNum(new RegisterBean()).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                GroupInfoBean applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                friendApplyUnread = Math.max(0, applyNumBean.friendApplyNum);
                applyUnread = Math.max(0, applyNumBean.groupApplyNum);
                onOneRequestFinish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                onOneRequestFinish();
            }
        });
    }

    private void loadFriendList() {
        HttpUtil.apiW().friends_friendList(new RegisterBean()).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                // 移除客服（使用迭代器避免 ConcurrentModificationException）
                mContactModels = new ArrayList<>();
                for (GroupInfoBean tempBean : tempList) {
                    if (tempBean != null && !tempBean.userId.equals(DataUtil.getKeFuId())) {
                        mContactModels.add(tempBean);
                    }
                }
                // 排序
                Collections.sort(mContactModels, new Comparator<GroupInfoBean>() {
                    @Override
                    public int compare(GroupInfoBean o1, GroupInfoBean o2) {
                        String firstLetter = FirstLetterUtil.getFirstLetter(o1.name);
                        String secondLetter = FirstLetterUtil.getFirstLetter(o2.name);
                        return firstLetter.compareTo(secondLetter);
                    }
                });
                DataUtil.setFriendInfoList(mContactModels);
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    // 每个请求完成后调用
    private void onOneRequestFinish() {
        finishCount++;
        if (finishCount == TOTAL_REQUESTS) {
            int noticeCardUnread = sysNoticeUnread + applyUnread + nimUnread;
            int totalUnread = noticeCardUnread + friendApplyUnread;
            cachedTotalNoticeUnread = sysNoticeUnread;
            updateBadge(viewBinding.funConversationFragmentNewFriendBadgeTv, friendApplyUnread);
            updateBadge(viewBinding.funConversationFragmentNoticeBadgeTv, noticeCardUnread);
            if (totalUnread > 0) {
                viewBinding.tvNoticeCount.setVisibility(View.VISIBLE);
                viewBinding.tvNoticeCount.setText(String.valueOf(totalUnread));
            } else {
                viewBinding.tvNoticeCount.setVisibility(View.GONE);
            }
            if (headerAdapterRef != null) {
                headerAdapterRef.notifyItemChanged(0);
            }
            finishCount = 0;
        }
    }

    private void updateBadge(TextView badgeView, int count) {
        if (badgeView == null) {
            return;
        }
        if (count > 0) {
            badgeView.setVisibility(View.VISIBLE);
            badgeView.setText(String.valueOf(count));
        } else {
            badgeView.setVisibility(View.GONE);
        }
    }


    void requestKefu(String kefuId) {
        RegisterBean bean = new RegisterBean();
        bean.userId = kefuId;
        HttpUtil.apiW().friends_searchByUserIdF(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        if ("0".equals(userBean.friend)) {
                            RegisterBean bean = new RegisterBean();
                            bean.memberCode = userBean.memberCode;
                            bean.msg = "客服";
                            HttpUtil.apiW().friends_addFriends(bean)
                                    .enqueue(new CommonCallback<NetData>() {
                                        @Override
                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                            NIMClient.getService(MsgService.class).clearChattingHistory(kefuId, SessionTypeEnum.P2P);
//                              NIMClient.getService(MsgService.class).clearServerHistory(kefuId,SessionTypeEnum.P2P);
                                        }

                                        @Override
                                        public void Failure(Call<NetData> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void sendMessage(String account) {
        // 自定义消息内容
//    Map<String, Object> content = new HashMap<>();
//    content.put("type", "custom");
//    content.put("data", "这是自定义会话记录的内容");

        // 设置自定义消息配置
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false; // 自定义消息不计入未读数

        // 构建自定义消息
        IMMessage message = MessageBuilder.createCustomMessage(account, SessionTypeEnum.P2P, null,
                null, config, null);


        message.setConfig(config);

        // 保存自定义消息
        NIMClient.getService(MsgService.class).saveMessageToLocal(message, true).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.P2P);
//        NIMClient.getService(MsgService.class).clearServerHistory(account,SessionTypeEnum.P2P);
                // 保存成功
            }

            @Override
            public void onFailed(int code) {
                // 保存失败
            }

            @Override
            public void onException(Throwable exception) {
                // 保存异常
            }
        });
    }

    private void sendGroupMessage(String account) {
        // 自定义消息内容
//    Map<String, Object> content = new HashMap<>();
//    content.put("type", "custom");
//    content.put("data", "这是自定义会话记录的内容");

        // 设置自定义消息配置
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false; // 自定义消息不计入未读数

        // 构建自定义消息
        IMMessage message = MessageBuilder.createCustomMessage(account, SessionTypeEnum.Team, null,
                null, config, null);
//    IMMessage message1 = MessageBuilder.createEmptyMessage()

        message.setConfig(config);

        // 保存自定义消息
        NIMClient.getService(MsgService.class).saveMessageToLocal(message, true).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.Team);
//        NIMClient.getService(MsgService.class).clearServerHistory(account,SessionTypeEnum.Team);
                // 保存成功
            }

            @Override
            public void onFailed(int code) {
                // 保存失败
            }

            @Override
            public void onException(Throwable exception) {
                // 保存异常
            }
        });
    }
    // 发送消息的方法
//  public void sendMessage(String account, String messageText) {
//    // 创建一个文本消息
//    IMMessage message = MessageBuilder.createTextMessage(
//            account,               // 对方帐号
//            SessionTypeEnum.P2P,   // 会话类型：P2P (单聊) 或 Team (群聊)
//            messageText            // 消息文本内容
//    );
//
//
//    // 发送消息
//    NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
//      @Override
//      public void onSuccess(Void param) {
//        // 发送成功
//      }
//
//      @Override
//      public void onFailed(int code) {
//        // 发送失败
//      }
//
//      @Override
//      public void onException(Throwable exception) {
//        // 发送异常
//      }
//    });
//  }

    private void initView() {
        conversationView = viewBinding.conversationView;
        conversationView._type = _type;
        conversationView.adapter._type = _type;

        networkErrorView = viewBinding.errorTv;
        emptyView = viewBinding.emptyLayout;

        setViewHolderFactory(new FunViewHolderFactory());
        viewBinding.conversationView.addItemDecoration(getItemDecoration());
//    viewBinding.funConversationFragmentNav.clearLeftMenu();
        _initHeadCell();
        loadUIConfig();
        _initTopStatus(0);
        conversationView.setData(conversationList);
        // --- Add scrolling header for P2P tab (_type == 0) using ConcatAdapter ---
        if (_type == 0 || _type == 3) {
            RecyclerView rv = conversationView.getRecyclerView();
            RecyclerView.Adapter<?> origin = conversationView.getAdapter();
            RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.fun_conversation_view_top_holder,
                            parent,
                            false);
                    return new RecyclerView.ViewHolder(v) {
                    };
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    TextView tv = holder.itemView.findViewById(R.id.messageTv);
                    tv.setText(noticeText);
                    TextView unreadTv = holder.itemView.findViewById(R.id.unreadTv);
                    if (unreadTv != null) {
                        if (cachedTotalNoticeUnread > 0) {
                            unreadTv.setVisibility(View.VISIBLE);
                            unreadTv.setText(String.valueOf(cachedTotalNoticeUnread));
                        } else {
                            unreadTv.setVisibility(View.GONE);
                        }
                    }
                    // 头像点击（或整个头部）跳转到系统公告页
                    holder.itemView.setOnClickListener(v ->
                            FunSystem_Notice_New_Activity.start(
                                    FunSystem_Notice_New_Activity.class,
                                    v.getContext(),
                                    null));
                }

                @Override
                public int getItemCount() {
                    return 1;
                }
            };
            headerAdapterRef = headerAdapter;
            ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, origin);
            rv.setAdapter(concatAdapter);
            rv.setBackgroundColor(0xFFF1F1F1);
            rv.setClipToPadding(false);
        }

        setupSearchOverlay();
        viewBinding.funConversationFragmentSearchIv.setOnClickListener(v -> {
            if (_type == 0 || _type == 3) {
                showSearchOverlay();
            } else {
                XKitRouter.withKey("SearchNewActivity")
                        .withContext(requireContext())
                        .navigate();
            }
        });
        viewBinding.funConversationFragmentKefuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, DataUtil.getKeFuId())
                        .withContext(getContext())
                        .navigate();
            }
        });

        viewBinding.funConversationFragmentMoreIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
                ContentListPopView contentListPopView =
                        new ContentListPopView.Builder(context)
                                .addItem(FunPopItemFactory.getCreateAdvancedTeamItem(context, memberLimit))
                                .addItem(FunPopItemFactory.getDivideLineItem(context))
                                .addItem(FunPopItemFactory.getAddFriendItem(context))
                                .addItem(FunPopItemFactory.getDivideLineItem(context))
                                .addItem(FunPopItemFactory.getScanItem(context))
                                .enableShadow(false)
                                .backgroundRes(R.drawable.fun_conversation_view_pop_bg)
                                .build();
                contentListPopView.showAsDropDown(
                        v, (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right), 0);
            }
        });

        viewBinding.refreshLayout.setEnableRefresh(true);
        viewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            if (viewModel != null) {
                viewModel.fetchConversation();
            } else {
                refreshLayout.finishRefresh();
            }
        });
    }

    void doOptWithIndex(int index) {
        if (index == 0) {
            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.FunSelected_User_ActivityKey)
                    .withContext(getContext())
                    .withParam("type", "1")
                    .navigate();
        } else if (index == 1) {

        } else if (index == 2) {
            XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE).withContext(getContext()).navigate();
        } else if (index == 3) {
            EventBus.getDefault().post(new BaseEvent("gotoScan"));
        }
//    AppProxy.getInstance().showType = index;
//    conversationView.adapter.notifyDataSetChanged();
//    List<ConversationBean> tempList = new ArrayList<>();
////    if (isFirst) {
////      isFirst = false;
//      tempList = conversationList;
////    } else {
////      tempList = conversationView.adapter.conversationList;
////    }
//    if (index == 0) {
//      conversationView.setData(tempList);
//    } else if (index == 1) {
//      ArrayList<ConversationBean> tempArr = new ArrayList<>();
//      for (ConversationBean tempBean:
//              tempList) {
//        if (tempBean.viewType == 1) {
//          tempArr.add(tempBean);
//        }
//      }
//      conversationView.setData(tempArr);
//    } else if (index == 2) {
//      ArrayList<ConversationBean> tempArr = new ArrayList<>();
//      for (ConversationBean tempBean:
//              tempList) {
//        if (tempBean.viewType == 2) {
//          tempArr.add(tempBean);
//        }
//      }
//      conversationView.setData(tempArr);
//    } else if (index == 3) {
//      ArrayList<ConversationBean> tempArr = new ArrayList<>();
//      for (ConversationBean tempBean:
//              tempList) {
//        if (DataUtil.getKeFuId() != null && tempBean.param != null) {
//          String param = (String) tempBean.param;
//          if (DataUtil.getKeFuId().equals(param) || DataUtil.getXiaoZhuShouId().equals(param)) {
//            tempArr.add(tempBean);
//          }
//        }
//      }
//      conversationView.setData(tempArr);
//    }
    }

    private void setupFixedHeader() {
        if (viewBinding == null) {
            return;
        }
        boolean showFixedHeader = _type != 1;
        viewBinding.funConversationFragmentFixedCardsLl.setVisibility(
                showFixedHeader ? View.VISIBLE : View.GONE);
        if (!showFixedHeader) {
            return;
        }
        viewBinding.funConversationFragmentNewFriendLl.setOnClickListener(
                v ->
                        XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                                .withContext(requireContext())
                                .navigate());
        viewBinding.funConversationFragmentNoticeLl.setOnClickListener(
                v ->
                        FunSystem_Notice_New_Activity.start(
                                FunSystem_Notice_New_Activity.class, getContext(), null));
    }

    private void _initHeadCell() {
        setupFixedHeader();
//    viewBinding.funConversationFragmentHeadAll.viewConversationHeadItemIv.setImageResource(R.drawable.conversation_list_index_msg_icon);
//    viewBinding.funConversationFragmentHeadAll.viewConversationHeadItemTv.setText("发起群聊");
//    viewBinding.funConversationFragmentHeadAll.viewConversationHeadItemLl.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        _initTopStatus(0);
//        doOptWithIndex(0);
//      }
//    });
//
//    viewBinding.funConversationFragmentHeadSingle.viewConversationHeadItemIv.setImageResource(R.drawable.conversation_list_index_group_icon);
//    viewBinding.funConversationFragmentHeadSingle.viewConversationHeadItemTv.setText("加入群聊");
//    viewBinding.funConversationFragmentHeadSingle.viewConversationHeadItemLl.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        _initTopStatus(1);
//        doOptWithIndex(1);
//      }
//    });
//
//    viewBinding.funConversationFragmentHeadGroup.viewConversationHeadItemIv.setImageResource(R.drawable.conversation_list_index_add_friend_icon);
//    viewBinding.funConversationFragmentHeadGroup.viewConversationHeadItemTv.setText("添加好友");
//    viewBinding.funConversationFragmentHeadGroup.viewConversationHeadItemLl.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        _initTopStatus(2);
//        doOptWithIndex(2);
//      }
//    });
//
//    viewBinding.funConversationFragmentHeadNotice.viewConversationHeadItemIv.setImageResource(R.drawable.conversation_list_index_scan_icon);
//    viewBinding.funConversationFragmentHeadNotice.viewConversationHeadItemTv.setText("扫一扫");
//    viewBinding.funConversationFragmentHeadNotice.viewConversationHeadItemLl.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        _initTopStatus(3);
//
//        doOptWithIndex(3);
//      }
//    });

    }

    void _initTopStatus(int index) {
        topIndex = index;
//    viewBinding.funConversationFragmentHeadNotice.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.black));
//    viewBinding.funConversationFragmentHeadGroup.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.black));
//    viewBinding.funConversationFragmentHeadSingle.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.black));
//    viewBinding.funConversationFragmentHeadAll.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.black));
//    switch (index) {
//      case 0:
//        viewBinding.funConversationFragmentHeadAll.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_8B5FD8));
//        break;
//      case 1:
//        viewBinding.funConversationFragmentHeadSingle.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_8B5FD8));
//        break;
//      case 2:
//        viewBinding.funConversationFragmentHeadGroup.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_8B5FD8));
//        break;
//      case 3:
//        viewBinding.funConversationFragmentHeadNotice.viewConversationHeadItemTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.color_8B5FD8));
//        break;
//      default:
//        break;
//    }

    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            final int itemSpacing = SizeUtils.dp2px(2);

            @Override
            public void getItemOffsets(
                    @NonNull Rect outRect,
                    @NonNull View view,
                    @NonNull RecyclerView parent,
                    @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                outRect.bottom = itemSpacing;
            }
        };
    }

    private void loadUIConfig() {
        if (ConversationKitClient.getConversationUIConfig() == null) {
            return;
        }
        ConversationUIConfig config = ConversationKitClient.getConversationUIConfig();


        if (config.conversationComparator != null) {
            setComparator(config.conversationComparator);
        }

        if (config.conversationFactory != null) {
            setViewHolderFactory(config.conversationFactory);
        }
//    titleBarView.setVisibility(View.GONE);

        if (config.customLayout != null) {
            config.customLayout.customizeConversationLayout(this);
        }
    }


    public LinearLayout getTopLayout() {
        return viewBinding.topLayout;
    }

    public LinearLayout getBodyLayout() {
        return viewBinding.bodyLayout;
    }

    public FrameLayout getBottomLayout() {
        return viewBinding.bottomLayout;
    }

    public FrameLayout getBodyTopLayout() {
        return viewBinding.bodyTopLayout;
    }

    public TextView getErrorTextView() {
        return viewBinding.errorTv;
    }

    public void setEmptyViewVisible(int visible) {
        viewBinding.emptyLayout.setVisibility(visible);
    }

    public View getEmptyView() {
        return viewBinding.emptyLayout;
    }
}
