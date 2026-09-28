// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.CHAT_P2P_INVITER_USER_LIMIT;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.DeleteTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSettingActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CloseChatPageEvent;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSettingViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.userinfo.FunCommentActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.userinfo.FunUserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseCommentActivity;
import com.netease.yunxin.kit.contactkit.ui.userinfo.UserInfoViewModel;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ResourceHelper;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import com.yaoxin.appbase.model.GroupInfoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Fun皮肤单聊聊天设置页面
 */
public class FunChatSettingActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ChatSettingActivity";

    FunChatSettingActivityBinding binding;

    ChatSettingViewModel viewModel;
    protected UserInfoViewModel viewModel1;
    protected ContactUserInfoBean userInfoData;
    protected UserBean userBean;
    protected ActivityResultLauncher<Intent> commentLauncher;

    UserInfo userInfo;
    FriendInfo friendInfo;
    String accId;

    int type = 0;
    protected final EventNotify<CloseChatPageEvent> closeEventNotify =
            new EventNotify<CloseChatPageEvent>() {
                @Override
                public void onNotify(@NonNull CloseChatPageEvent message) {
                    finish();
                }

                @NonNull
                @Override
                public String getEventType() {
                    return CloseChatPageEvent.EVENT_TYPE;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        String type1 = getIntent().getStringExtra("type");
        if (type1 != null) {
            type = Integer.parseInt(type1);
        }
        super.onCreate(savedInstanceState);
        EventCenter.registerEventNotify(closeEventNotify);
        changeStatusBarColor(R.color.color_white);
        binding = FunChatSettingActivityBinding.inflate(getLayoutInflater());
        StatusBarUtils.transtStatusBar(this, binding.funChatSettingActivityNav);
        viewModel = new ViewModelProvider(this).get(ChatSettingViewModel.class);
        setContentView(binding.getRoot());
        binding.funChatSettingActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.funChatSettingActivityNav.getTitleView().setText("聊天设置");
        if (type == 1) {
            binding.funChatSettingActivityNav.getTitleView().setText("查看主页");

        }
        initView();
        initData();
        initRequest();
        registerResult();
        binding.funChatSettingActivityAddFriendTv.setOnClickListener(this);
    }

    /** 用列表/缓存好友数据填充详情；置顶/免打扰/黑名单等由云信接口补充 */
    private void bindFromFriendList() {
        GroupInfoBean friend = resolveFriendFromList();
        if (friend == null) {
            // 无列表数据时仍用云信资料展示，业务接口所需 memberCode 为空则相关操作会提示暂无法操作
            if (userBean == null) {
                userBean = new UserBean();
                userBean.userId = accId;
            }
            return;
        }
        if (userBean == null) {
            userBean = new UserBean();
        }
        userBean.userId = !TextUtils.isEmpty(friend.userId) ? friend.userId : accId;
        userBean.memberCode = friend.memberCode;
        userBean.name = friend.name;
        userBean.avatar = friend.avatar;
        userBean.remark = friend.remark;
        userBean.grade = friend.grade;
        userBean.friend = "1";

        if (!TextUtils.isEmpty(userBean.memberCode)) {
            binding.funChatSettingActivityId.setVisibility(View.VISIBLE);
            binding.funChatSettingActivityId.setText("ID: " + userBean.memberCode);
        } else {
            binding.funChatSettingActivityId.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(userBean.name)) {
            binding.nameTv.setText(userBean.name);
        }
        if (!TextUtils.isEmpty(userBean.avatar)) {
            GlideUtil.yh_loadImageRoundedCorner(
                    this, binding.funChatSettingActivityAvatarView, userBean.avatar, 0);
        }
        if (!TextUtils.isEmpty(userBean.remark)) {
            binding.funChatSettingActivityMemo.rightTv.setText(userBean.remark);
            binding.funChatSettingActivityMemo.rightTv.setVisibility(View.VISIBLE);
        }
        // 从通讯录进详情默认是好友
        binding.funChatSettingActivityContentLl.setVisibility(View.VISIBLE);
        binding.funChatSettingActivitySendMsgRl.setVisibility(View.VISIBLE);
        binding.funChatSettingActivityAddFriendTv.setVisibility(View.GONE);

        if (userBean.grade > 0) {
            binding.funTeamUserInfoDetailGradeIv.setVisibility(View.VISIBLE);
            binding.ivGradeBg.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailGradeIv.setImageDrawable(
                    ResourceHelper.getGradeDrawable(this, userBean.grade));
            binding.nameTv.setTextColor(ResourceHelper.getGradeColor(this, userBean.grade));
            binding.ivGradeBg.setImageDrawable(ResourceHelper.getGradeBackground(this, userBean.grade));
        }
    }

    @Nullable
    private GroupInfoBean resolveFriendFromList() {
        String friendJson = getIntent().getStringExtra("friend");
        if (!TextUtils.isEmpty(friendJson)) {
            try {
                GroupInfoBean friend = new Gson().fromJson(friendJson, GroupInfoBean.class);
                if (friend != null) {
                    return friend;
                }
            } catch (Exception ignored) {
            }
        }
        List<GroupInfoBean> friendList = DataUtil.getFriendInfoList();
        for (GroupInfoBean friend : friendList) {
            if (friend != null && TextUtils.equals(friend.userId, accId)) {
                return friend;
            }
        }
        return null;
    }

    private void initRequest() {

        viewModel1 = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel1.init(accId);
        viewModel1
                .getFriendFetchResult()
                .observe(
                        this,
                        mapFetchResult -> {
                            if (mapFetchResult.getLoadStatus() == LoadStatus.Success) {
                                userInfoData = mapFetchResult.getData();
                                if (userInfoData != null) {
                                    binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.setSelected(userInfoData.isBlack);
                                }
                            } else {
                                if (!NetworkUtils.isConnected()) {
                                    Toast.makeText(this, com.netease.yunxin.kit.contactkit.ui.R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
        viewModel1.fetchData(accId);
    }

    private void initView() {
        userInfo = (UserInfo) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
        accId = (String) getIntent().getSerializableExtra(RouterConstant.CHAT_ID_KRY);
        if (userInfo == null && TextUtils.isEmpty(accId)) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(accId)) {
            accId = userInfo.getAccount();
        }
        bindFromFriendList();
        refreshView();
        binding.funChatSettingActivityMemo.titTv.setText("备注名");
        Activity activity = this;
        Context that = this;

        binding.funChatSettingActivityMemo.funTitleTfArrowViewLl.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userInfoData == null || userInfoData.data == null) {
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setClass(that, FunCommentActivity.class);
                        intent.putExtra(
                                BaseCommentActivity.REQUEST_COMMENT_NAME_KEY, userInfoData.friendInfo.getAlias().isEmpty() ? userInfoData.friendInfo.getName() : userInfoData.friendInfo.getAlias());
                        commentLauncher.launch(intent);
                    }
                }
        );

//        binding.funChatSettingActivityRecommand.titTv.setText("推荐好友");
        binding.funChatSettingActivityToTop.titTv.setText("置顶聊天");
        binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.setVisibility(View.VISIBLE);
        binding.funChatSettingActivityToTop.arrowIcon.setVisibility(View.GONE);


        binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.setSelected(ConversationRepo.isStickTop(accId, SessionTypeEnum.P2P));
        binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.isSelected()) {
                            ConversationRepo.addStickTop(
                                    accId,
                                    SessionTypeEnum.P2P,
                                    "",
                                    new ChatCallback<StickTopSessionInfo>() {
                                        @Override
                                        public void onSuccess(@Nullable StickTopSessionInfo param) {
                                            binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.setSelected(true);
                                            ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
                                        }
                                    });
                        } else {
                            ConversationRepo.removeStickTop(
                                    accId,
                                    SessionTypeEnum.P2P,
                                    "",
                                    new ChatCallback<Void>() {
                                        @Override
                                        public void onSuccess(@Nullable Void param) {
                                            binding.funChatSettingActivityToTop.funTitleTfArrowViewSwitch.setSelected(false);
                                            ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
                                        }
                                    });
                        }
                    }
                }

        );


        binding.funChatSettingActivityClearHistory.titTv.setText("清理聊天记录");
        binding.funChatSettingActivityClearHistory.funTitleTfArrowViewLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ALog.d("123");
                DialogAlertUtil.showAlert("确认删除聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type == 1) {


                            NIMClient.getService(MsgService.class).clearChattingHistory(accId, SessionTypeEnum.P2P);
                            NIMClient.getService(MsgService.class).clearServerHistory(accId, SessionTypeEnum.P2P);

                            EventBus.getDefault().post(new BaseEvent("clearP2PMessageList"));
                        }
                    }
                }, getSupportFragmentManager());
            }
        });

        binding.funChatSettingActivityClearDontNoticeMsg.titTv.setText("消息免打扰");
        binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.setVisibility(View.VISIBLE);
        binding.funChatSettingActivityClearDontNoticeMsg.arrowIcon.setVisibility(View.GONE);

        boolean isNotice = ConversationRepo.isNotify(accId, SessionTypeEnum.P2P);
        binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.setSelected(!isNotice);
        binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.setOnClickListener(
                v ->
                        ConversationRepo.setNotify(
                                accId,
                                SessionTypeEnum.P2P,
                                binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.isSelected(),
                                new ChatCallback<Void>() {
                                    @Override
                                    public void onSuccess(@Nullable Void param) {
                                        binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.setSelected(!binding.funChatSettingActivityClearDontNoticeMsg.funTitleTfArrowViewSwitch.isSelected());
                                    }
                                }));


        binding.funChatSettingActivityClearAddBlackList.titTv.setText("加入黑名单");
        binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.setVisibility(View.VISIBLE);
        binding.funChatSettingActivityClearAddBlackList.arrowIcon.setVisibility(View.GONE);

        binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.setOnClickListener(
                (View v) -> {
                    RegisterBean registerBean = new RegisterBean();
                    if (binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.isSelected()) {
                        registerBean.state = 0;
//                        viewModel1.removeBlack(accId);
                    } else {
                        registerBean.state = 1;
//                        viewModel1.addBlack(accId);
                    }
                    if (userBean == null || TextUtils.isEmpty(userBean.memberCode)) {
                        ToastUtils.toastMsg("暂无法操作");
                        return;
                    }
                    registerBean.memberCode = userBean.memberCode;
                    HttpUtil.apiW().friends_changeBlackState(registerBean)
                            .enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                    ToastUtils.toastMsg(body.msg);
                                    binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.setSelected(!binding.funChatSettingActivityClearAddBlackList.funTitleTfArrowViewSwitch.isSelected());

                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });

                }
        );

        binding.funChatSettingActivityClearDeleteFriend.titTv.setText("删除好友");
        binding.funChatSettingActivityClearDeleteFriend.titTv.setTextColor(getResources().getColor(R.color.fun_chat_color_EF0000));
        binding.funChatSettingActivityClearDeleteFriend.funTitleTfArrowViewLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogAlertUtil.showAlert("确定删除好友吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type == 1) {
                            if (userBean == null) {
                                ToastUtils.toastMsg("暂无法删除");
                                return;
                            }
                            RegisterBean bean = new RegisterBean();
                            bean.userId = !TextUtils.isEmpty(userBean.userId)
                                    ? userBean.userId
                                    : accId;
                            HttpUtil.apiW().friends_delFriend(bean)
                                    .enqueue(new CommonCallback<NetData>() {
                                        @Override
                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                            ToastUtils.toastMsg(body.msg);
                                            finish();
                                        }

                                        @Override
                                        public void Failure(Call<NetData> call, Throwable t) {

                                        }
                                    });
                        }
                    }
                }, getSupportFragmentManager());

//                viewModel1.deleteFriend(userInfoData.data.getAccount());
            }
        });


        binding.funChatSettingActivitySendMsgRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
                    if (userInfoData == null || userInfoData.data == null) {
                        return;
                    }
                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                            .withParam(RouterConstant.CHAT_ID_KRY, userInfoData.data.getAccount())
                            .withContext(FunChatSettingActivity.this)
                            .navigate();
                    finish();
                } else {
                    finish();
                }
            }
        });

    }

    private void registerResult() {
        commentLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == BaseCommentActivity.RESULT_OK
                                    && result.getData() != null) {
                                String comment =
                                        result.getData().getStringExtra(BaseCommentActivity.REQUEST_COMMENT_NAME_KEY);
                                userInfoData.friendInfo.setAlias(comment);
                                viewModel1.updateAlias(userInfoData.data.getAccount(), comment);
                                if (userBean != null) {
                                    String memberCode = resolveFriendMemberCode();
                                    if (TextUtils.isEmpty(memberCode)) {
                                        ToastUtils.toastMsg("暂无法修改备注");
                                        return;
                                    }
                                    RegisterBean bean = new RegisterBean();
                                    bean.memberCode = memberCode;
                                    bean.userId = accId;
                                    bean.alias = comment;
                                    HttpUtil.apiW().friends_updateRemark(bean)
                                            .enqueue(new CommonCallback<NetData>() {
                                                @Override
                                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                                    ToastUtils.toastMsg(body.msg);
                                                }

                                                @Override
                                                public void Failure(Call<NetData> call, Throwable t) {

                                                }
                                            });
                                }

                            }
                        });
    }

    /** 备注接口按好友关系查 memberCode，searchByUserId 返回的可能对不上，优先取好友列表里的 */
    private String resolveFriendMemberCode() {
        List<GroupInfoBean> friendList = DataUtil.getFriendInfoList();
        for (GroupInfoBean friend : friendList) {
            if (friend != null
                    && TextUtils.equals(friend.userId, accId)
                    && !TextUtils.isEmpty(friend.memberCode)) {
                return friend.memberCode;
            }
        }
        return userBean != null ? userBean.memberCode : null;
    }

    private void refreshView() {
        if (friendInfo != null) {
            GlideUtil.yh_loadImageRoundedCorner(this, binding.funChatSettingActivityAvatarView, friendInfo.getAvatar(), 0);
//            binding.funChatSettingActivityAvatarView.setData(
//                    friendInfo.getAvatar(),
//                    friendInfo.getAvatarName(),
//                    AvatarColor.avatarColor(friendInfo.getAccount()));
            binding.nameTv.setText(friendInfo.getName());
//            binding.funChatSettingActivityId.setText("ID:" + friendInfo.getAccount());
//      binding.noTeamNameTv.setText(friendInfo.getName());
        } else if (userInfo == null) {
            GlideUtil.yh_loadImageRoundedCorner(this, binding.funChatSettingActivityAvatarView, "", 0);
//            binding.funChatSettingActivityAvatarView.setData(null, accId, AvatarColor.avatarColor(accId));
            binding.nameTv.setText(accId);
//      binding.noTeamNameTv.setText(accId);
        } else {
            String name =
                    TextUtils.isEmpty(userInfo.getComment()) ? userInfo.getName() : userInfo.getComment();
            if (TextUtils.isEmpty(name)) {
                name = userInfo.getAccount();
            }
            ALog.d(LIB_TAG, TAG, "initView name -->> " + name);
            GlideUtil.yh_loadImageRoundedCorner(this, binding.funChatSettingActivityAvatarView, userInfo.getAvatar(), 0);
//            binding.funChatSettingActivityAvatarView.setData(
//                    userInfo.getAvatar(), name, AvatarColor.avatarColor(userInfo.getAccount()));
            binding.nameTv.setText(name);
            binding.funChatSettingActivityMemo.rightTv.setText(TextUtils.isEmpty(userInfo.getComment()) ? "" : userInfo.getComment());
            binding.funChatSettingActivityMemo.rightTv.setVisibility(View.VISIBLE);
//      binding.noTeamNameTv.setText(name);
        }
    }

    private void initData() {
        if (accId == null) return;
        viewModel
                .getUserInfoLiveData()
                .observe(
                        this,
                        result -> {
                            if (result.getLoadStatus() == LoadStatus.Success) {
                                friendInfo = result.getData();
                                refreshView();
                                // 列表业务字段（ID/等级等）保持用通讯录数据
                                bindFromFriendList();
                            }
                        });
        viewModel.getUserInfo(accId);


//    binding.stickTopSC.setChecked(ConversationRepo.isStickTop(accId, SessionTypeEnum.P2P));
//    binding.stickTopLayout.setOnClickListener(
//        v -> {
//          if (!binding.stickTopSC.isChecked()) {
//            ConversationRepo.addStickTop(
//                accId,
//                SessionTypeEnum.P2P,
//                "",
//                new ChatCallback<StickTopSessionInfo>() {
//                  @Override
//                  public void onSuccess(@Nullable StickTopSessionInfo param) {
//                    binding.stickTopSC.setChecked(true);
//                    ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
//                  }
//                });
//          } else {
//            ConversationRepo.removeStickTop(
//                accId,
//                SessionTypeEnum.P2P,
//                "",
//                new ChatCallback<Void>() {
//                  @Override
//                  public void onSuccess(@Nullable Void param) {
//                    binding.stickTopSC.setChecked(false);
//                    ConversationRepo.notifyStickTop(accId, SessionTypeEnum.P2P);
//                  }
//                });
//          }
//        });
//
//    binding.pinLayout.setOnClickListener(
//        v ->
//            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_PIN_PAGE)
//                .withParam(RouterConstant.KEY_SESSION_TYPE, SessionTypeEnum.P2P.getValue())
//                .withParam(RouterConstant.KEY_SESSION_ID, accId)
//                .withParam(RouterConstant.KEY_SESSION_NAME, getName())
//                .withContext(FunChatSettingActivity.this)
//                .navigate());
//
    }

    private void selectUsersCreateGroup() {
        ArrayList<String> filterList = new ArrayList<>();
        filterList.add(accId);
        XKitRouter.withKey(RouterConstant.PATH_FUN_SELECT_CREATE_TEAM_PAGE)
                .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, CHAT_P2P_INVITER_USER_LIMIT - 1)
                .withParam(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
                .withContext(this)
                .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
                .withParam(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY, filterList)
                .navigate();
    }

    private String getName() {
        if (friendInfo != null) {
            return friendInfo.getName();
        } else if (userInfo != null) {
            return userInfo.getName();
        }
        return accId;
    }

    @Override
    public void onClick(View view) {
        if (view == binding.funChatSettingActivityNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.funChatSettingActivityAddFriendTv) {
            if (userBean == null || TextUtils.isEmpty(userBean.memberCode)) {
                ToastUtils.toastMsg("暂无法添加该用户");
                return;
            }
            HashMap map = new HashMap();
            map.put("user", new Gson().toJson(userBean));
            FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class, this, map);
        }
    }
}
