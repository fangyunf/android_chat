// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
import com.netease.yunxin.kit.chatkit.ui.common.ChatZhuanZhangHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.SoftKeyboardFixerForFullscreen;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Fun皮肤聊天界面Fragment，继承自ChatBaseFragment Fun皮肤下的差异化UI在这里实现，基础功能在父类中实现
 */
public abstract class FunChatFragment extends ChatBaseFragment {

    FunChatFragmentBinding viewBinding;

    @Override
    public View initViewAndGetRootView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        viewBinding = FunChatFragmentBinding.inflate(inflater, container, false);
        chatView = viewBinding.chatView;
        changeStatusBarColor(R.color.color_white);
        viewBinding.chatView.getTitleBar().getBackImageView().setImageResource(com.yaoxin.appbase.R.mipmap.ic_back_black);
//        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
//        try {
//
//            SoftKeyboardFixerForFullscreen.assistActivity(getActivity());
//            FrameLayout frameLayout = viewBinding.chatView.getTitleBarLayout();
//            frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    // 确保只调用一次
//                    frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    int height = frameLayout.getHeight();
//                    LinearLayout.LayoutParams frameLayoutParams = (LinearLayout.LayoutParams) viewBinding.chatView.getTitleBarLayout().getLayoutParams();
//                    frameLayoutParams.height = height + BarUtils.getStatusBarHeight();
//                    viewBinding.chatView.getTitleBarLayout().setLayoutParams(frameLayoutParams);
//                    viewBinding.chatView.getTitleBarLayout().setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
//                }
//            });
//        } catch (Exception e) {
//
//        }


        return viewBinding.getRoot();
    }

    void _updateMessageCell(IMMessage message) {
        chatView.getMessageListView().updateMessage(message, null);
    }

    @Override
    public Integer getReplayMessageClickPreviewDialogBgRes() {
        return R.color.color_ededed;
    }

    @Override
    public String getUserInfoRoutePath() {
        return RouterConstant.PATH_FUN_USER_INFO_PAGE;
    }

    protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
        return new FunChatForwardSelectDialog();
    }

    @Override
    protected void forwardP2P() {

        ChatUtils.startP2PSelector(
                getContext(), RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE, null, forwardP2PLauncher);
    }

    @Override
    protected void forwardTeam() {
        ChatUtils.startTeamList(
                getContext(), RouterConstant.PATH_FUN_MY_TEAM_PAGE, forwardTeamLauncher);
    }

    @Override
    public void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {
        FunChatMessageForwardConfirmDialog confirmDialog =
                FunChatMessageForwardConfirmDialog.createForwardConfirmDialog(
                        type, sessionIds, getSessionName(), true, forwardAction);
        confirmDialog.setCallback(
                (inputMsg) -> {
                    if (!NetworkUtils.isConnected()) {
                        Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (TextUtils.equals(forwardAction, ActionConstants.POP_ACTION_TRANSMIT)) {
                        ChatMessageBean msg = getForwardMessage();
                        if (msg != null) {
                            for (String accId : sessionIds) {
                                viewModel.sendForwardMessage(msg, inputMsg, accId, type);
                            }
                        }
                    } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
                        viewModel.sendMultiForwardMessage(
                                getSessionName(), inputMsg, sessionIds, type, ChatMsgCache.getMessageList());
                        clearMessageMultiSelectStatus();
                    } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
                        viewModel.sendForwardMessages(
                                getSessionName(), inputMsg, sessionIds, type, ChatMsgCache.getMessageList());
                        clearMessageMultiSelectStatus();
                    }
                });
        confirmDialog.show(getParentFragmentManager(), FunChatMessageForwardConfirmDialog.TAG);
    }

    public String getSessionName() {
        return sessionID;
    }

    @Override
    protected void clickMessage(IMMessageInfo messageInfo, boolean isReply) {
        if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.custom) {
            if (messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
                        .withContext(getContext())
                        .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
                        .navigate();
                return;
            }
            if (!messageInfo.getMessage().getAttachStr().isEmpty()) {
                CustomMsgBean msgBean = new Gson().fromJson(messageInfo.getMessage().getAttachStr(), CustomMsgBean.class);
                if (msgBean == null) {
                    return;
                }
                if (ChatZhuanZhangHelper.isZhuanZhangMessageType(msgBean.type)) {
                    CustomMsgBean detail = ChatZhuanZhangHelper.parseFromMessage(messageInfo);
                    if (detail == null) {
                        if (!TextUtils.isEmpty(msgBean.data)) {
                            msgBean.result = new Gson().fromJson(msgBean.data, CustomMsgBean.class);
                        }
                        detail = msgBean.result != null ? msgBean.result : msgBean;
                        ChatZhuanZhangHelper.normalizeDetail(detail);
                    }
                    ChatZhuanZhangHelper.openZhuanZhangDetail(
                            getActivity() != null ? getActivity() : getContext(), detail);
                    return;
                }
                msgBean.result = new Gson().fromJson(msgBean.data, CustomMsgBean.class);
                if (msgBean.type == 10086) {
                    for (GroupInfoBean bean : DataUtil.getFriendInfoList()) {
                        if (bean.memberCode.equals(msgBean.result.memberCode)) {
                            RegisterBean bean1 = new RegisterBean();
                            bean1.phoneAndCode = msgBean.result.memberCode;
                            bean1.type = 0;
                            HttpUtil.apiW().friends_search(bean1)
                                    .enqueue(new CommonCallback<NetData>() {
                                        @Override
                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                            UserBean userInfo = new Gson().fromJson(body.data.toString(), UserBean.class);
                                            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                                                    .withParam(RouterConstant.CHAT_ID_KRY, userInfo.userId)
                                                    .withContext(requireActivity())
                                                    .navigate();
                                        }

                                        @Override
                                        public void Failure(Call<NetData> call, Throwable t) {

                                        }
                                    });
                            return;
                        }
                    }
                    RegisterBean bean = new RegisterBean();
                    bean.phoneAndCode = msgBean.result.memberCode;
                    bean.type = 0;
                    HttpUtil.apiW().friends_search(bean)
                            .enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                    UserBean userInfo = new Gson().fromJson(body.data.toString(), UserBean.class);
                                    HashMap map = new HashMap();
                                    map.put("user", new Gson().toJson(userInfo));
                                    FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class, getActivity(), map);
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                    return;
                }
                RegisterBean bean = new RegisterBean();
                bean.redpacketId = msgBean.result.id;
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


                                    if (getActivity() != null) {
                                        try {
                                            FunOpenRedPacketFragment.showV(getActivity().getSupportFragmentManager(), bean.redpacketId, redBean.type, sessionID, msgBean, messageInfo.getMessage(), new FunOpenRedPacketFragment.OpenRedPacketBlock() {
                                                @Override
                                                public void hasOpen(IMMessage message) {
                                                    _updateMessageCell(message);
                                                }
                                            });
                                        } catch (Exception e) {

                                        }
                                    } else {
                                        ToastUtils.toastMsg("网络错误");
                                    }
                                }
                                if (redBean.type == 4 || redBean.type == 5) {
                                    /// 当前用户领取已领取过当前红包，展示领取详细信息
                                    ///当红包是个人/专属，当前非目标领取用户，直接显示查看领取详情
                                    HashMap map = new HashMap();
                                    map.put("redpacketId", bean.redpacketId);
                                    Activity context = getActivity();
                                    if (context != null) {
                                        FunRedPacketResultActivity.start(FunRedPacketResultActivity.class, context, map);
                                    }
                                }

                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });

                return;
            }
        }
        super.clickMessage(messageInfo, isReply);
    }
}
