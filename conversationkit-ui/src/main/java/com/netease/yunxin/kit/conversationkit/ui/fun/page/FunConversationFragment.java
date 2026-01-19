// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
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
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.sunfusheng.marqueeview.IMarqueeItem;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Response;

public class FunConversationFragment extends ConversationBaseFragment {

    private FunConversationFragmentBinding viewBinding;

    private boolean _needRefresh;


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

    private int topIndex;

    @Override
    public View initViewAndGetRootView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        viewBinding = FunConversationFragmentBinding.inflate(inflater, container, false);
        initView();

        if (_type == 1) {
            viewBinding.funConversationFragmentTitleTv.setText("群聊");
        } else if (_type == 3) {
            viewBinding.funConversationFragmentTitleTv.setText("消息");
        } else {
            viewBinding.funConversationFragmentTitleTv.setText("消息");
        }

        viewBinding.funConversationFragmentSearchIvIcon.setOnClickListener(v -> {
            XKitRouter.withKey("SearchNewActivity")
                    .withContext(requireContext())
                    .navigate();
        });
        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewBinding.funConversationFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        viewBinding.funConversationFragmentTopLl.setLayoutParams(layoutParams);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // 这里写你想延时执行的代码
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        requestMsg();
                    }
                });
            }
        }, 1500);
        EventBus.getDefault().register(this);
        searchWord();
        return viewBinding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!AppProxy.searchKeyWord0.isEmpty()) {
            viewBinding.funConversationFragmentEt.setText("");
            AppProxy.searchKeyWord0 = "";
            conversationView.adapter.notifyDataSetChanged();
        }

        if (_type == 0 || _type == 3) {
            if (!AppProxy.searchKeyWord0.isEmpty()) {
                viewBinding.funConversationFragmentEt.setText("");
                AppProxy.searchKeyWord0 = "";
                conversationView.adapter.notifyDataSetChanged();
            }
        } else {
            if (!AppProxy.searchKeyWord1.isEmpty()) {
                viewBinding.funConversationFragmentEt.setText("");
                AppProxy.searchKeyWord1 = "";
                conversationView.adapter.notifyDataSetChanged();
            }
        }

    }

    void searchWord() {
        viewBinding.funConversationFragmentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (_type == 0 || _type == 3) {
                    AppProxy.getInstance().searchKeyWord0 = string;
                } else {
                    AppProxy.getInstance().searchKeyWord1 = string;
                }
                conversationView.adapter.notifyDataSetChanged();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("refresh_chat_list")) {
            _needRefresh = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_needRefresh) {
            _needRefresh = false;
            requestMsg();
        }
    }

    @Override
    protected void finishLoadData() {
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
                                viewBinding.marqueeView.startWithText(message);
                                viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);
                            } else {
                                message = "";
                                viewBinding.marqueeView.startWithText(message);
                                viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                            }
                        } else {

                            String message = "";
                            viewBinding.marqueeView.startWithText(message);
                            viewBinding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                        String message = "";
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
                            if (!hasKefu) {
                                sendMessage(kefuId);
                            }
                            if (!hasXiaoZhushou) {
                                sendMessage(xiaozhushouId);
                            }
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
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
        viewBinding.funConversationFragmentSearchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//        XKitRouter.withKey("SearchNewActivity")
//                .withContext(requireContext())
//                .navigate();
                FunSystem_Notice_New_Activity.start(FunSystem_Notice_New_Activity.class, getContext(), null);
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

    private void _initHeadCell() {
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
            final int topPadding = SizeUtils.dp2px(0.25f);
            final int indent = SizeUtils.dp2px(76);

            @Override
            public void onDrawOver(
                    @NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int left = parent.getPaddingLeft() + indent;
                int right = parent.getWidth() - parent.getPaddingRight();

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + topPadding;

                    Paint paint = new Paint();
                    paint.setColor(
                            parent.getResources().getColor(R.color.fun_conversation_item_divide_line_color));
                    canvas.drawRect(left, top, right, bottom, paint);
                }
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
