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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunOpenRedPacketFragment;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
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
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.SoftKeyboardFixerForFullscreen;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

//        viewBinding.chatView.getTitleBar().getBackImageView().setImageResource(com.yaoxin.appbase.R.mipmap.temp_ic_back_white);
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
        if (messageInfo == null
                || messageInfo.getMessage() == null
                || messageInfo.getMessage().getMsgType() != MsgTypeEnum.custom) {
            super.clickMessage(messageInfo, isReply);
            return;
        }
        if (messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
                    .withContext(getContext())
                    .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
                    .navigate();
            return;
        }

        String attachStr = messageInfo.getMessage().getAttachStr();
        if (TextUtils.isEmpty(attachStr)) {
            super.clickMessage(messageInfo, isReply);
            return;
        }

        CustomMsgBean msgBean;
        try {
            msgBean = parseCustomMsg(attachStr);
        } catch (Exception e) {
            super.clickMessage(messageInfo, isReply);
            return;
        }
        if (msgBean == null) {
            super.clickMessage(messageInfo, isReply);
            return;
        }

        // 名片
        if (msgBean.type == 10086) {
            handleMingPianClick(msgBean);
            return;
        }

        if (msgBean.result == null || TextUtils.isEmpty(msgBean.result.id)) {
            super.clickMessage(messageInfo, isReply);
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.redpacketId = msgBean.result.id;
        HttpUtil.apiW()
                .red_checkRedpacet(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null) {
                                    return;
                                }
                                CustomMsgBean redBean =
                                        new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                                if (redBean == null) {
                                    return;
                                }
                                if (redBean.type == 1 || redBean.type == 2 || redBean.type == 3) {
                                    if (redBean.type == 1) {
                                        redBean.redPacketId = bean.redpacketId;
                                    }
                                    if (getActivity() != null) {
                                        try {
                                            FunOpenRedPacketFragment.showV(
                                                    getActivity().getSupportFragmentManager(),
                                                    bean.redpacketId,
                                                    redBean.type,
                                                    sessionID,
                                                    msgBean,
                                                    messageInfo.getMessage(),
                                                    new FunOpenRedPacketFragment.OpenRedPacketBlock() {
                                                        @Override
                                                        public void hasOpen(IMMessage message) {
                                                            _updateMessageCell(message);
                                                        }
                                                    });
                                        } catch (Exception ignored) {
                                        }
                                    } else {
                                        ToastUtils.toastMsg("网络错误");
                                    }
                                }
                                if (redBean.type == 4 || redBean.type == 5) {
                                    HashMap map = new HashMap();
                                    map.put("redpacketId", bean.redpacketId);
                                    Activity context = getActivity();
                                    if (context != null) {
                                        FunRedPacketResultActivity.start(
                                                FunRedPacketResultActivity.class, context, map);
                                    }
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }

    /** 兼容 data 为 JSON 字符串或 JSON 对象两种名片/红包结构 */
    private CustomMsgBean parseCustomMsg(String attachStr) {
        JsonObject root = JsonParser.parseString(attachStr).getAsJsonObject();
        CustomMsgBean msgBean = new CustomMsgBean();
        if (root.has("type") && !root.get("type").isJsonNull()) {
            msgBean.type = root.get("type").getAsInt();
        }
        if (!root.has("data") || root.get("data").isJsonNull()) {
            return msgBean;
        }
        JsonElement dataEl = root.get("data");
        String dataJson;
        if (dataEl.isJsonPrimitive()) {
            dataJson = dataEl.getAsString();
            msgBean.data = dataJson;
        } else {
            dataJson = dataEl.toString();
            msgBean.data = dataJson;
        }
        if (!TextUtils.isEmpty(dataJson)) {
            msgBean.result = new Gson().fromJson(dataJson, CustomMsgBean.class);
        }
        return msgBean;
    }

    private void handleMingPianClick(CustomMsgBean msgBean) {
        if (msgBean.result == null || TextUtils.isEmpty(msgBean.result.memberCode)) {
            ToastUtils.toastMsg("名片信息异常");
            return;
        }
        final String memberCode = msgBean.result.memberCode;
        boolean isFriend = false;
        List<GroupInfoBean> friendList = DataUtil.getFriendInfoList();
        if (friendList != null) {
            for (GroupInfoBean bean : friendList) {
                if (bean != null
                        && !TextUtils.isEmpty(bean.memberCode)
                        && bean.memberCode.equals(memberCode)) {
                    isFriend = true;
                    break;
                }
            }
        }

        RegisterBean searchBean = new RegisterBean();
        searchBean.phoneAndCode = memberCode;
        searchBean.type = 0;
        final boolean openChat = isFriend;
        HttpUtil.apiW()
                .friends_search(searchBean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null || getActivity() == null) {
                                    return;
                                }
                                try {
                                    UserBean userInfo =
                                            new Gson()
                                                    .fromJson(body.data.toString(), UserBean.class);
                                    if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
                                        ToastUtils.toastMsg("用户不存在");
                                        return;
                                    }
                                    if (openChat) {
                                        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                                                .withParam(
                                                        RouterConstant.CHAT_ID_KRY, userInfo.userId)
                                                .withContext(requireActivity())
                                                .navigate();
                                    } else {
                                        HashMap map = new HashMap();
                                        map.put("user", new Gson().toJson(userInfo));
                                        FunAddFriendVerifyActivity.start(
                                                FunAddFriendVerifyActivity.class,
                                                getActivity(),
                                                map);
                                    }
                                } catch (Exception e) {
                                    ToastUtils.toastMsg("名片信息异常");
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }
}
