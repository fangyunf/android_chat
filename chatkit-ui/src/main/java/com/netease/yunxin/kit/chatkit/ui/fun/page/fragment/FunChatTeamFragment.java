// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_TEAM_SETTING_PAGE;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMTeamMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatEggOpenDialogFragment;
import com.netease.yunxin.kit.chatkit.ui.fun.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Fun皮肤群聊聊天界面Fragment，继承自FunChatFragment
 * 单聊和群聊有一些差异，为了方便维护，将差异化的部分抽象到FunChatP2PFragment和FunChatTeamFragment中
 */
public class FunChatTeamFragment extends FunChatFragment {
    private static final String TAG = "ChatTeamFragment";
    Team teamInfo;
    TeamMember currentMember;
    IMMessage anchorMessage;
    private boolean showDeleteDialog = false;
    Observer<FetchResult<List<IMTeamMessageReceiptInfo>>> teamReceiptObserver;

    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void initData(Bundle bundle) {
        ALog.d(LIB_TAG, TAG, "initData");
        sessionType = SessionTypeEnum.Team;
        teamInfo = (Team) bundle.getSerializable(RouterConstant.CHAT_KRY);
        sessionID = (String) bundle.getSerializable(RouterConstant.CHAT_ID_KRY);
        if (teamInfo == null && TextUtils.isEmpty(sessionID)) {
            getActivity().finish();
            return;
        }
        if (TextUtils.isEmpty(sessionID)) {
            sessionID = teamInfo.getId();
        }
        anchorMessage = (IMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
        chatView
                .getTitleBar()
                .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
                .setActionImg(R.drawable.ic_more_point)
                .setActionListener(
                        v -> {
                            // go to team setting
                            chatView.hideCurrentInput();
                            XKitRouter.withKey(PATH_FUN_TEAM_SETTING_PAGE)
                                    .withContext(requireContext())
                                    .withParam(KEY_TEAM_ID, sessionID)
                                    .navigate();
                        });

        ImageView actionImageView = chatView.getTitleBar().getActionImageView();
        ViewGroup.LayoutParams layoutParams = actionImageView.getLayoutParams();
        layoutParams.width = SizeUtils.dp2px(40);
        layoutParams.height = SizeUtils.dp2px(40);
        actionImageView.setLayoutParams(layoutParams);
        aitManager = new AitManager(getContext(), sessionID);
        aitManager.setUIStyle(AitManager.STYLE_FUN);
        aitManager.updateTeamInfo(teamInfo);
        chatView.setAitManager(aitManager);
        refreshView();
        _requestData();
        //startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _requestCaiData(); // Call your method here
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 2000); // Schedule the task to run every 1 second
    }

    void _requestData() {
        HttpUtil.apiW().group_groupHomeInfo(sessionID)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        GroupInfoBean groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        if (groupInfoBean.announcement != null && !groupInfoBean.announcement.isEmpty()) {

                            viewBinding.chatView.getMarqueeViewLL().setVisibility(View.VISIBLE);
                            viewBinding.chatView.getMarqueeView().setVisibility(View.VISIBLE);
                            String message = groupInfoBean.announcement;
                            viewBinding.chatView.getMarqueeView().startWithText(message);
                            viewBinding.chatView.getMarqueeView().startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                        } else {
                            viewBinding.chatView.getMarqueeView().setVisibility(View.GONE);
                            viewBinding.chatView.getMarqueeViewLL().setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });


    }

    void _requestCaiData() {
        RegisterBean registerBean = new RegisterBean();
        registerBean.groupId = sessionID;
        HttpUtil.apiW().caidan_groupCaidan(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<CustomMsgBean>>() {
                        }.getType();
                        List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        if (tempList == null || tempList.isEmpty()) {
                            viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewEggLl.setVisibility(View.GONE);
                            return;
                        }
                        int totalMoney = 0;
                        for (CustomMsgBean tempBean : tempList) {
                            totalMoney += Integer.parseInt(tempBean.amount);
                        }
                        viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewEggLl.setVisibility(View.VISIBLE);
                        viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewEggLl.setOnClickListener(v -> {
                            ChatEggOpenDialogFragment.showV(getActivity().getSupportFragmentManager(), tempList.get(0));
                        });
                        viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewEggTitleTv.setText(tempList.size() + "个彩蛋\n共" + NumberUtil.formartMoney_zhengshu(totalMoney + "") + "元");
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void refreshView() {
        if (teamInfo != null) {
            chatView.getTitleBar().setTitle(teamInfo.getName());
            chatView.getMessageListView().updateTeamInfo(teamInfo);
        }
        if (currentMember != null && teamInfo != null) {
            if (currentMember.getType() != TeamMemberType.Owner
                    && currentMember.getType() != TeamMemberType.Manager) {
                chatView.setInputMute(teamInfo.isAllMute());
            } else {
                chatView.setInputMute(false);
            }
        }
    }

    @Override
    protected void initViewModel() {
        ALog.d(LIB_TAG, TAG, "initViewModel");
        viewModel = new ViewModelProvider(this).get(ChatTeamViewModel.class);
        viewModel.init(sessionID, SessionTypeEnum.Team);
    }

    @Override
    protected void initToFetchData() {
        if (viewModel instanceof ChatTeamViewModel) {
            // query team info
            ((ChatTeamViewModel) viewModel).requestTeamInfo(sessionID);
            // init team members
            ((ChatTeamViewModel) viewModel).requestTeamMembers(sessionID);
            // fetch history message
            viewModel.initFetch(anchorMessage, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (chatConfig != null && chatConfig.chatListener != null) {
            chatConfig.chatListener.onSessionChange(sessionID, sessionType);
        }
        if (chatConfig != null && chatConfig.messageProperties != null) {
            viewModel.setShowReadStatus(chatConfig.messageProperties.showTeamMessageStatus);
        }

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("clearTeamMessageList")) {
            chatView.clearMessageList();
        } else if ("reload_gonggao".equals(event.getTag())) {
            _requestData();
        } else if ("egg_open_notice".equals(event.getTag())) {
            CustomMsgBean customMsgBean = event.customMsgBean;
            if (customMsgBean.groupId.equals(sessionID)) {
                viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewSuccessEggRl.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .asGif()
                        .load(R.drawable.egg_open_success) // 替换为你的本地GIF文件名
                        .listener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                resource.setLoopCount(1); // 设置GIF只播放一次
                                resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                    @Override
                                    public void onAnimationEnd(Drawable drawable) {
                                        // 当动画播放完成时隐藏ImageView
                                        viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewSuccessEggRl.setVisibility(View.GONE);
                                    }
                                });
                                return false;
                            }
                        })
                        .into(viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewSuccessEggIv);
                viewBinding.chatView.getChatViewFunLayoutBinding().funChatViewSuccessEggTv.setText("经过激烈角逐，恭喜" + customMsgBean.name + "中奖了!这是对" + customMsgBean.name + "的独一无二的才能和运气的肯定。幸运之神在您身边!!!");

            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ALog.d(LIB_TAG, TAG, "onStart:" + showDeleteDialog);
        if (showDeleteDialog) {
            showDialogToFinish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ChatTeamViewModel) viewModel)
                .getTeamMessageReceiptLiveData()
                .removeObserver(teamReceiptObserver);
        EventBus.getDefault().unregister(this);
        if (timer != null) {
            timer.cancel(); // Stop the timer when the activity is paused
            timer = null;
        }
    }

    @Override
    protected void initDataObserver() {
        super.initDataObserver();
        ALog.d(LIB_TAG, TAG, "initDataObserver");
        teamReceiptObserver =
                listFetchResult -> {
                    ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer");
                    if (listFetchResult == null || listFetchResult.getData() == null) return;
                    List<ChatMessageBean> messageList = new ArrayList<>();
                    for (IMTeamMessageReceiptInfo receiptInfo : listFetchResult.getData()) {
                        String msgId = receiptInfo.getTeamMessageReceipt().getMsgId();
                        ChatMessageBean msg = null;
                        for (ChatMessageBean messageBean : messageList) {
                            if (messageBean != null
                                    && messageBean.getMessageData() != null
                                    && TextUtils.equals(messageBean.getMessageData().getMessage().getUuid(), msgId)) {
                                msg = messageBean;
                                break;
                            }
                        }
                        if (msg == null) {
                            msg = chatView.getMessageListView().searchMessage(msgId);
                            if (msg != null) {
                                messageList.add(msg);
                            }
                        }
                    }
                    ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer,msgList:" + messageList.size());
                    for (ChatMessageBean message : messageList) {
                        chatView.getMessageListView().updateMessageStatus(message);
                    }
                };
        ((ChatTeamViewModel) viewModel)
                .getTeamMessageReceiptLiveData()
                .observeForever(teamReceiptObserver);

        ((ChatTeamViewModel) viewModel)
                .getTeamLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        team -> {
                            ALog.d(LIB_TAG, TAG, "TeamLiveData,observe");
                            if (team != null) {
                                teamInfo = team;
                                if (!team.isMyTeam()) {
                                    requireActivity().finish();
                                }
                                if (!TextUtils.isEmpty(team.getExtension())
                                        && team.getExtension().contains(IMKitConstant.TEAM_GROUP_TAG)) {
                                    viewModel.setTeamGroup(true);
                                }
                                aitManager.updateTeamInfo(teamInfo);
                                refreshView();
                            }
                        });

        ((ChatTeamViewModel) viewModel)
                .getTeamMemberData()
                .observe(
                        getViewLifecycleOwner(),
                        teamMembers -> {
                            ALog.d(LIB_TAG, TAG, "TeamMemberData,observe");

                            if (teamMembers.getSuccess() && teamMembers.getValue() != null) {
                                aitManager.setTeamMembers(teamMembers.getValue());
                                for (UserInfoWithTeam user : teamMembers.getValue()) {
                                    if (TextUtils.equals(user.getTeamInfo().getAccount(), IMKitClient.account())) {
                                        currentMember = user.getTeamInfo();
                                        refreshView();
                                        break;
                                    }
                                }
                                if (((ChatTeamViewModel) viewModel).hasLoadMessage()) {
                                    List<String> accIdList = new ArrayList<>();
                                    for (UserInfoWithTeam user : teamMembers.getValue()) {
                                        accIdList.add(user.getTeamInfo().getAccount());
                                    }
                                    if (accIdList.size() > 0) {
                                        chatView.getMessageListView().notifyUserInfoChange(accIdList);
                                    }
                                }
                            }
                        });

        ((ChatTeamViewModel) viewModel)
                .getTeamMemberChangeData()
                .observe(
                        getViewLifecycleOwner(),
                        result -> {
                            ALog.d(LIB_TAG, TAG, "TeamMemberChangeData,observe");
                            if (result.getLoadStatus() == LoadStatus.Finish && result.getData() != null) {
                                List<String> accIdList = new ArrayList<>();
                                for (UserInfoWithTeam user : result.getData()) {
                                    if (TextUtils.equals(user.getTeamInfo().getAccount(), IMKitClient.account())) {
                                        currentMember = user.getTeamInfo();
                                        refreshView();
                                    }
                                    accIdList.add(user.getTeamInfo().getAccount());
                                }
                                chatView.getMessageListView().notifyUserInfoChange(accIdList);
                            }
                        });
    }

    private void startTeamDismiss() {
        ALog.d(LIB_TAG, TAG, "startTeamDismiss");
        if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
            FunChatTeamFragment.this.requireActivity().finish();
        } else {
            if (FunChatTeamFragment.this.isResumed()) {
                showDialogToFinish();
                showDeleteDialog = false;
            } else {
                showDeleteDialog = true;
            }
        }
    }

    private void startKickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
        View dialogView = layoutInflater.inflate(R.layout.chat_alert_dialog_layout, null);
        TextView title = dialogView.findViewById(R.id.tv_dialog_title);
        TextView content = dialogView.findViewById(R.id.tv_dialog_content);
        TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
        content.setText(getString(R.string.chat_team_be_kick_content));
        title.setText(getString(R.string.chat_team_be_removed_title));
        positiveBut.setText(getString(R.string.chat_dialog_sure));
        // 设置不可取消
        builder.setCancelable(false);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        positiveBut.setOnClickListener(
                v -> {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    requireActivity().finish();
                });

        alertDialog.show();
    }

    private void showDialogToFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
        View dialogView = layoutInflater.inflate(R.layout.chat_alert_dialog_layout, null);
        TextView title = dialogView.findViewById(R.id.tv_dialog_title);
        TextView content = dialogView.findViewById(R.id.tv_dialog_content);
        TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
        content.setText(getString(R.string.chat_team_be_removed_content));
        title.setText(getString(R.string.chat_team_be_removed_title));
        positiveBut.setText(getString(R.string.chat_dialog_sure));
        positiveBut.setTextColor(getResources().getColor(R.color.fun_chat_color));
        // 设置不可取消
        builder.setCancelable(false);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        positiveBut.setOnClickListener(
                v -> {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    requireActivity().finish();
                });
        alertDialog.show();
    }

    @Override
    public void onNewIntent(Intent intent) {
        ALog.d(LIB_TAG, TAG, "onNewIntent");
        anchorMessage = (IMMessage) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE);
        ChatMessageBean anchorMessageBean =
                (ChatMessageBean) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE_BEAN);
        if (anchorMessageBean != null) {
            anchorMessage = anchorMessageBean.getMessageData().getMessage();
        } else if (anchorMessage != null) {
            anchorMessageBean = new ChatMessageBean(new IMMessageInfo(anchorMessage));
        }
        if (anchorMessage != null) {
            int position = chatView.getMessageListView().searchMessagePosition(anchorMessage.getUuid());
            if (position >= 0) {
                chatView
                        .getRootView()
                        .getViewTreeObserver()
                        .addOnGlobalLayoutListener(
                                new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        chatView.getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                        chatView
                                                .getRootView()
                                                .post(() -> chatView.getMessageListView().scrollToPosition(position));
                                    }
                                });
                chatView.getMessageListView().scrollToPosition(position);
            } else {
                chatView.clearMessageList();
                // need to add anchor message to list panel
                chatView.appendMessage(anchorMessageBean);
                viewModel.initFetch(anchorMessage, false);
            }
        }
    }

    @Override
    public String getSessionName() {
        if (teamInfo != null) {
            return teamInfo.getName();
        }
        return super.getSessionName();
    }

    public MessageBottomLayout getMessageBottomLayout() {
        return viewBinding.chatView.getBottomInputLayout();
    }

    @Override
    protected void onReceiveMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
        super.onReceiveMessage(listFetchResult);
        if (listFetchResult != null && listFetchResult.getData() != null) {
            List<ChatMessageBean> messageList = listFetchResult.getData();
            for (ChatMessageBean message : messageList) {
                if (MessageHelper.isDismissTeamMsg(message.getMessageData())) {
                    startTeamDismiss();
                    return;
                } else if (MessageHelper.isKickMsg(message.getMessageData())) {
                    startKickDialog();
                    return;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ALog.d(LIB_TAG, TAG, "onDestroy");
        if (aitManager != null) {
            aitManager.reset();
        }
    }
}
