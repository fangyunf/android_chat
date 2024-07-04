// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactNewFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ContactUserListAdapter;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * contact page
 */
public class ContactNewFragment extends BaseFragment implements View.OnClickListener {
    private final String TAG = "ContactFragment";
    private ContactNewFragmentBinding binding;
    private ContactEntranceBean verifyBean;
    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    ContactUserListAdapter adapter = new ContactUserListAdapter();
    protected IContactCallback contactCallback;
    GroupInfoBean applyNumBean = new GroupInfoBean();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ContactNewFragmentBinding.inflate(inflater, container, false);
        binding.contactNewFragmentGroupNoticeRl.setOnClickListener(this);
        binding.contactNewFragmentNewFriendRl.setOnClickListener(this);
        binding.contactNewFragmentBlackList.setOnClickListener(this);


        StatusBarUtils.transtStatusBar(getActivity(),binding.contactNewFragmentNav);
        binding.contactNewFragmentNav.clearLeftMenu();
        _initViews();
        _requestData();
        return binding.getRoot();
    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "MyChannel";
//            String description = "Channel for my notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID", name, importance);
//            channel.setDescription(description);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
//    private void sendNotification(String message) {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(getContext(),getActivity().getClass());
//
//        Intent intent = new Intent(this, getContext());
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MY_CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle("My Notification")
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        notificationManager.notify(0, builder.build());
//    }


    @Override
    public void onPause() {
        super.onPause();
        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().friends_friendList(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        mContactModels = new Gson().fromJson(body.data.toString(), type);
                        for (GroupInfoBean tempBean :
                                mContactModels) {
                            if (tempBean.userId.equals(DataUtil.getKeFuId())) {
                                mContactModels.remove(tempBean);
                                break;
                            }

                        }

                        adapter.contacts = mContactModels;
                        adapter.setItems(mContactModels);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

        HttpUtil.apiW().friends_applyListNum(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        if (applyNumBean.friendApplyNum > 0) {
                            binding.contactNewFragmentNewFriendTv.setText(applyNumBean.friendApplyNum + "");
                            binding.contactNewFragmentNewFriendTv.setVisibility(View.VISIBLE);
                        } else {
                            binding.contactNewFragmentNewFriendTv.setVisibility(View.GONE);
                        }
                        if (applyNumBean.groupApplyNum > 0) {
                            binding.contactNewFragmentGroupNoticeTv.setText(applyNumBean.groupApplyNum + "");
                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.VISIBLE);
                        } else {
                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.GONE);
                        }
                        if (contactCallback != null) {
                            contactCallback.updateUnreadCount(applyNumBean.friendApplyNum+applyNumBean.groupApplyNum);
                        }

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    protected void _initViews() {
        RecyclerView mRecyclerView = binding.contactNewFragmentRv;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i).userId)
                        .withParam("type", "1")
                        .withContext(requireActivity())
                        .navigate();
            }
        });

        // 侧边设置相关
        WaveSideBarView mWaveSideBarView = binding.contactNewFragmentMainSideBar;
        mWaveSideBarView.setOnSelectIndexItemListener(new WaveSideBarView.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String letter) {
                for (int i = 0; i < mContactModels.size(); i++) {
                    if (mContactModels.get(i).getIndex().equals(letter)) {
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });

        binding.contactTitleLayout.showRight2ImageView(false);
        binding
                .contactTitleLayout
                .setRightImageClick(
                        v ->
//                                XKitRouter.withKey(PATH_ADD_FRIEND_PAGE)
//                                        .withContext(getContext())
//                                        .navigate()
                                XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
                                        .withContext(requireContext())
                                        .navigate()
                );

    }

    protected void loadTitle() {

    }


    @Override
    public void onClick(View v) {
        if (v == binding.contactNewFragmentGroupNoticeRl) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withParam("type","1")
                    .withContext(requireContext())
                    .navigate();
        } else if (v == binding.contactNewFragmentNewFriendRl) {

            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withContext(requireContext())
                    .navigate();
        } else if (v == binding.contactNewFragmentBlackList) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE)
                    .withContext(requireContext())
                    .navigate();
        }
        //
    }

    public void setContactCallback(IContactCallback contactCallback) {
        this.contactCallback = contactCallback;
        this.contactCallback.updateUnreadCount(applyNumBean.friendApplyNum+applyNumBean.groupApplyNum);
    }
}
