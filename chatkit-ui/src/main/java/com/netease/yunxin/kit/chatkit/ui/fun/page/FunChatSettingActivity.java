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
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
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
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

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
        StatusBarUtils.transtStatusBar(this,binding.funChatSettingActivityNav);
        viewModel = new ViewModelProvider(this).get(ChatSettingViewModel.class);
        setContentView(binding.getRoot());
        binding.funChatSettingActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.funChatSettingActivityNav.getTitleView().setText("聊天设置");
        if (type == 1) {
            binding.funChatSettingActivityNav.getTitleView().setText("好友资料");

        }
        initView();
        initData();
        initRequest();
        registerResult();
    }

    void _reuestInfo() {
        RegisterBean bean = new RegisterBean();
        bean.userId = accId;
        HttpUtil.apiW().friends_searchByUserIdF(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        binding.funChatSettingActivityId.setText("ID: "+ userBean.memberCode);
                        binding.nameTv.setText(userBean.name);
                        if (userBean.remark != null && !userBean.remark.isEmpty())
                        {
                            binding.funChatSettingActivityMemo.rightTv.setText(userBean.remark);
                            binding.funChatSettingActivityMemo.rightTv.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
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
                                BaseCommentActivity.REQUEST_COMMENT_NAME_KEY, userInfoData.friendInfo.getAlias());
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


        binding.funChatSettingActivityClearHistory.titTv.setText("清除聊天记录");
        binding.funChatSettingActivityClearHistory.funTitleTfArrowViewLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ALog.d("123");
                DialogAlertUtil.showAlert("确认删除聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type == 1) {
                            NIMClient.getService(MsgService.class).clearChattingHistory(userBean.userId,SessionTypeEnum.P2P);

                            EventBus.getDefault().post(new BaseEvent("clearP2PMessageList"));
                        }
                    }
                },getSupportFragmentManager());
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
                            RegisterBean bean = new RegisterBean();
                            bean.memberCode = userBean.memberCode;
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
                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                            .withParam(RouterConstant.CHAT_ID_KRY, userInfoData.data.getAccount())
                            .withContext(FunChatSettingActivity.this)
                            .navigate();
                    finish();
                } else  {
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
                                RegisterBean bean = new RegisterBean();
                                    bean.memberCode = userBean.memberCode;
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

    private void refreshView() {
        if (friendInfo != null) {
            binding.funChatSettingActivityAvatarView.setData(
                    friendInfo.getAvatar(),
                    friendInfo.getAvatarName(),
                    AvatarColor.avatarColor(friendInfo.getAccount()));
            binding.nameTv.setText(friendInfo.getName());
            binding.funChatSettingActivityId.setText("ID:" + friendInfo.getAccount());
//      binding.noTeamNameTv.setText(friendInfo.getName());
        } else if (userInfo == null) {
            binding.funChatSettingActivityAvatarView.setData(null, accId, AvatarColor.avatarColor(accId));
            binding.nameTv.setText(accId);
//      binding.noTeamNameTv.setText(accId);
        } else {
            String name =
                    TextUtils.isEmpty(userInfo.getComment()) ? userInfo.getName() : userInfo.getComment();
            if (TextUtils.isEmpty(name)) {
                name = userInfo.getAccount();
            }
            ALog.d(LIB_TAG, TAG, "initView name -->> " + name);
            binding.funChatSettingActivityAvatarView.setData(
                    userInfo.getAvatar(), name, AvatarColor.avatarColor(userInfo.getAccount()));
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
                                _reuestInfo();
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
        }
    }
}
