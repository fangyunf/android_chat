// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;

import android.app.Activity;
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
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactNewFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactCallback;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ContactUserListAdapter;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.GroupListAdapter;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.NewFriendListAdapter;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.StatusBarUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    GroupListAdapter groupListAdapter = new GroupListAdapter();
    List<GroupInfoBean> groupListDataList = new ArrayList<>();

    NewFriendListAdapter verifyAdapter = new NewFriendListAdapter();

    List<UserBean> verifyList = new ArrayList<>();

    int _selectIndex = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ContactNewFragmentBinding.inflate(inflater, container, false);

        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.contactNewFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight();
        binding.contactNewFragmentTopLl.setLayoutParams(layoutParams);
        binding.contactNewFragmentFriendTv.setOnClickListener(this);
        binding.contactNewFragmentFriendTv.setSelected(true);
        binding.contactNewFragmentGroupTv.setOnClickListener(this);
        binding.contactNewFragmentNewFriendLl.setOnClickListener(this);
        binding.contactNewFragmentSearchIv.setOnClickListener(this);
        binding.contactNewFragmentSearchLl.setOnClickListener(this);
        binding.contactNewFragmentMoreIv.setOnClickListener(this);
        _initViews();
        _requestData();
        return binding.getRoot();
    }



    @Override
    public void onPause() {
        super.onPause();
        _requestData();
    }
    protected void _requestMemeber(int page) {
        ParamsBean registerBean = new ParamsBean();
        registerBean.page = page ;
        HttpUtil.apiW().friends_friendListPage(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        if (page == 1) {
                            mContactModels.clear();
                        }
                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        List<GroupInfoBean> tempBeanList = new Gson().fromJson(body.data.toString(), type);
                        for (GroupInfoBean tempBean :
                                tempBeanList) {
                            if (!tempBean.userId.equals(DataUtil.getKeFuId())) {
                                mContactModels.add(tempBean);
                            }

                        }
                        if (tempBeanList.size() == 100) {
                            _requestMemeber(page + 1);
                            return;
                        }

                        Collections.sort(mContactModels, new Comparator<GroupInfoBean>() {
                            @Override
                            public int compare(GroupInfoBean o1, GroupInfoBean o2) {
                                // 获取name的首字母并忽略大小写比较
                                String firstLetter = FirstLetterUtil.getFirstLetter(o1.name);
                                String secondLetter = FirstLetterUtil.getFirstLetter(o2.name);
                                return firstLetter.compareTo(secondLetter);
                            }
                        });
                        DataUtil.setFriendInfoList(mContactModels);
                        adapter.contacts = mContactModels;
                        if (_selectIndex == 0) {
                            adapter.setItems(mContactModels);
                            binding.contactNewFragmentRv.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }
    @Override
    protected void _requestData() {

        _requestMemeber(1);

        HttpUtil.apiW().friends_applyListNum(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        adapter.friendApplyNum = applyNumBean.friendApplyNum;
                        adapter.groupApplyNum = applyNumBean.groupApplyNum;
                        adapter.notifyDataSetChanged();

                        if (applyNumBean.friendApplyNum > 0) {
                            binding.contactNewFragmentNewFriendNumTv.setText(applyNumBean.friendApplyNum + "");
                            binding.contactNewFragmentNewFriendNumTv.setVisibility(View.VISIBLE);
                        } else {
                            binding.contactNewFragmentNewFriendNumTv.setVisibility(View.GONE);
                        }
//                        if (applyNumBean.groupApplyNum > 0) {
//                            binding.contactNewFragmentGroupNoticeTv.setText(applyNumBean.groupApplyNum + "");
//                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.VISIBLE);
//                        } else {
//                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.GONE);
//                        }
                        if (contactCallback != null) {
                            contactCallback.updateUnreadCount(applyNumBean.friendApplyNum);
                        }

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

        HttpUtil.apiW().group_userGroups(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();

                        groupListDataList = new Gson().fromJson(body.data.toString(),type);
                        if (_selectIndex == 1) {
                            binding.contactNewFragmentRv.setAdapter(groupListAdapter);
                            groupListAdapter.setItems(groupListDataList);
                            groupListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

        RegisterBean bean = new RegisterBean();
        bean.pageNo = "0";
        HttpUtil.apiW().friends_applyList(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        NetData listData = new Gson().fromJson(body.data.toString(),NetData.class);
                        Gson gson = new Gson();
                        verifyList =
                                gson.fromJson(new Gson().toJson(listData.data), new TypeToken<List<UserBean>>() {
                                }.getType());
                        if (_selectIndex == 2) {
                            binding.contactNewFragmentRv.setAdapter(verifyAdapter);
                            verifyAdapter.setItems(verifyList);
                            verifyAdapter.notifyDataSetChanged();
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

        Activity that = getActivity();

        groupListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i).groupId)
                        .withContext(that)
                        .navigate();
            }
        });
        verifyAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<UserBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                UserBean bean = baseQuickAdapter.getItem(i);
                bean.page_type = 100;
                HashMap map = new HashMap();
                map.put("user",new Gson().toJson(bean));
                FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class,that,map);
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (baseQuickAdapter.getItemViewType(i) == Constant.RECYCLE_VIEW_ITEM) {
                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                            .withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i - 1).userId)
                            .withParam("type", "1")
                            .withContext(requireActivity())
                            .navigate();
                }
            }
        });

        adapter.addOnItemChildClickListener(R.id.contact_index_headview_1_ll, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, DataUtil.getKeFuId())
                        .withContext(getContext())
                        .navigate();
            }
        });
        adapter.addOnItemChildClickListener(R.id.contact_index_headview_2_ll, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE)
                        .withContext(requireContext())
                        .navigate();
            }
        });
        adapter.addOnItemChildClickListener(R.id.contact_index_headview_3_ll, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withContext(requireContext())
                    .navigate();
            }
        });
        adapter.addOnItemChildClickListener(R.id.contact_index_headview_4_ll, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withParam("type","1")
                    .withContext(requireContext())
                    .navigate();
            }
        });
        adapter.addOnItemChildClickListener(R.id.contact_index_headview_5_ll, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
//                XKitRouter.withKey(Constant.XiaoZhuShouActivityKey)
//                        .withContext(requireContext())
//                        .navigate();
                SystemNotice_NewActivity.start(SystemNotice_NewActivity.class,requireContext(),null);
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

    }

    protected void loadTitle() {

    }


    @Override
    public void onClick(View v) {
        if (v == binding.contactNewFragmentFriendTv) {
            resetState();
            binding.contactNewFragmentFriendTv.setSelected(true);

            _selectIndex = 0;
            binding.contactNewFragmentMainSideBar.setVisibility(View.VISIBLE);
            binding.contactNewFragmentRv.setAdapter(adapter);
            adapter.setItems(mContactModels);
            adapter.notifyDataSetChanged();

        } else if (v == binding.contactNewFragmentGroupTv) {
            resetState();
            _selectIndex = 1;
            binding.contactNewFragmentGroupTv.setSelected(true);
            binding.contactNewFragmentRv.setAdapter(groupListAdapter);
            groupListAdapter.setItems(groupListDataList);
            groupListAdapter.notifyDataSetChanged();
        } else if (v == binding.contactNewFragmentNewFriendLl) {
            _selectIndex = 2;
            resetState();
            binding.contactNewFragmentNewFriendTv.setSelected(true);
            binding.contactNewFragmentNewFriendNumTv.setVisibility(View.GONE);


            binding.contactNewFragmentRv.setAdapter(verifyAdapter);
            verifyAdapter.setItems(verifyList);
            verifyAdapter.notifyDataSetChanged();

        } else if (v == binding.contactNewFragmentSearchIv) {
            XKitRouter.withKey("FunSystem_Notice_New_Activity")
                    .withContext(requireContext())
                    .navigate();
        } else if (v == binding.contactNewFragmentSearchLl) {
            XKitRouter.withKey("SearchNewActivity")
                    .withContext(requireContext())
                    .navigate();


        }
        else if (v == binding.contactNewFragmentMoreIv) {
            XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE)
                    .withContext(requireContext())
                    .navigate();
        }
    }

    void resetState() {
        binding.contactNewFragmentMainSideBar.setVisibility(View.GONE);
        binding.contactNewFragmentFriendTv.setSelected(false);
        binding.contactNewFragmentGroupTv.setSelected(false);
        binding.contactNewFragmentNewFriendTv.setSelected(false);
    }

    public void setContactCallback(IContactCallback contactCallback) {
        this.contactCallback = contactCallback;
        this.contactCallback.updateUnreadCount(applyNumBean.friendApplyNum+applyNumBean.groupApplyNum);
    }
}
