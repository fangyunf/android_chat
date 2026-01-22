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
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
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
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.SimpleFriendListAdapter;
import com.netease.yunxin.kit.contactkit.ui.normal.groupList.MyGroupListActivity;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

/**
 * contact page
 */
public class ContactNewFragment extends BaseFragment implements View.OnClickListener {
    private final String TAG = "ContactFragment";
    protected IContactCallback contactCallback;
    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    SimpleFriendListAdapter friendAdapter = new SimpleFriendListAdapter();
    GroupInfoBean applyNumBean = new GroupInfoBean();
    SimpleFriendListAdapter groupListAdapter = new SimpleFriendListAdapter();
    List<GroupInfoBean> groupListDataList = new ArrayList<>();
    SimpleFriendListAdapter teamListAdapter = new SimpleFriendListAdapter();
    List<GroupInfoBean> teamListDataList = new ArrayList<>();
    NewFriendListAdapter verifyAdapter = new NewFriendListAdapter();
    List<UserBean> verifyList = new ArrayList<>();
    int _selectIndex = 0;
    private ContactNewFragmentBinding binding;
    private ContactEntranceBean verifyBean;
    private View headerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ContactNewFragmentBinding.inflate(inflater, container, false);
        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.contactNewFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        binding.contactNewFragmentTopLl.setLayoutParams(layoutParams);
        binding.contactNewFragmentSearchIv.setOnClickListener(this);
        binding.contactNewFragmentSearchLl.setOnClickListener(this);
        binding.contactNewFragmentMoreIv.setOnClickListener(this);
        binding.contactNewFragmentSao.setOnClickListener(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        _initViews();
        _requestData();
        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        // 确保返回时重新设置 adapter
        if (headerAdapter != null) {
            switch (_selectIndex) {
                case 0:
                    switchToFriendTab();
                    break;
                case 1:
                    switchToGroupTab();
                    break;
                case 2:
                    switchToTeamTab();
                    break;
            }
        }
        loadMessageCount();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if ("refresh_notice".equals(event.getTag())) {
            loadMessageCount();
        }
    }

    @Override
    protected void _requestData() {
//        _requestMemeber(1);
        loadFriendList();
        loadMessageCount();
        loadTeamList();
        RegisterBean bean = new RegisterBean();
        bean.pageNo = "0";
        HttpUtil.apiW().friends_applyList(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                NetData listData = new Gson().fromJson(body.data.toString(), NetData.class);
                Gson gson = new Gson();
                verifyList = gson.fromJson(new Gson().toJson(listData.data), new TypeToken<List<UserBean>>() {
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

    private void loadMessageCount() {
        HttpUtil.apiW().friends_applyListNum(new RegisterBean()).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
//                        friendAdapter.friendApplyNum = applyNumBean.friendApplyNum;
//                        friendAdapter.groupApplyNum = applyNumBean.groupApplyNum;
                // friendAdapter.notifyDataSetChanged();
                headerAdapter.notifyItemChanged(0);
//                        if (applyNumBean.groupApplyNum > 0) {
//                            binding.contactNewFragmentGroupNoticeTv.setText(applyNumBean.groupApplyNum + "");
//                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.VISIBLE);
//                        } else {
//                            binding.contactNewFragmentGroupNoticeTv.setVisibility(View.GONE);
//                        }
                if (contactCallback != null) {
                    contactCallback.updateUnreadCount(applyNumBean.friendApplyNum + applyNumBean.groupApplyNum);
                }

            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    protected void _requestMemeber(int page) {
        ParamsBean registerBean = new ParamsBean();
        registerBean.page = page;
        HttpUtil.apiW().friends_friendListPage(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                if (page == 1) {
                    mContactModels.clear();
                }
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                List<GroupInfoBean> tempBeanList = new Gson().fromJson(body.data.toString(), type);
                for (GroupInfoBean tempBean : tempBeanList) {
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
                friendAdapter.contacts = mContactModels;
                friendAdapter.setItems(mContactModels);
                if (_selectIndex == 0) {
                    // 首次加载时设置 adapter
                    ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, friendAdapter);
                    binding.contactNewFragmentRv.setAdapter(concatAdapter);
                }
                friendAdapter.notifyDataSetChanged();
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
        // 创建公共 header adapter
        headerAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_header_with_tabs, parent, false);
                initHeaderView();
                return new RecyclerView.ViewHolder(headerView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // 每次刷新时更新 Tab 状态和下划线
                if (headerView != null) {
                    TextView friendTab = headerView.findViewById(R.id.contact_header_friend_tab_tv);
                    TextView groupTab = headerView.findViewById(R.id.contact_header_group_tab_tv);
                    TextView teamTab = headerView.findViewById(R.id.contact_header_team_tab_tv);

                    View friendLine = headerView.findViewById(R.id.contact_header_friend_line);
                    View groupLine = headerView.findViewById(R.id.contact_header_group_line);
                    View teamLine = headerView.findViewById(R.id.contact_header_team_line);

                    // 重置所有状态
                    friendTab.setSelected(false);
                    groupTab.setSelected(false);
                    teamTab.setSelected(false);

                    friendLine.setVisibility(View.INVISIBLE);
                    groupLine.setVisibility(View.INVISIBLE);
                    teamLine.setVisibility(View.INVISIBLE);

                    // 根据当前选中更新状态
                    switch (_selectIndex) {
                        case 0:
                            friendTab.setSelected(true);
                            friendLine.setVisibility(View.VISIBLE);
                            break;
                        case 1:
                            groupTab.setSelected(true);
                            groupLine.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            teamTab.setSelected(true);
                            teamLine.setVisibility(View.VISIBLE);
                            break;
                    }

                    // 更新角标
                    updateHeaderBadge();
                }
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        };

        // 默认显示好友列表
        // 注意：不在这里 setAdapter，等数据加载后再设置

        Activity that = getActivity();

        // 群聊列表点击事件
        teamListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE).withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i).groupId).withContext(that).navigate();
            }
        });

        // 好友列表点击事件
        friendAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                GroupInfoBean friend = baseQuickAdapter.getItem(i);
                if (friend != null) {
                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE).withParam(RouterConstant.CHAT_ID_KRY, friend.userId).withParam("type", "1").withContext(requireActivity()).navigate();
                }
            }
        });

        // 侧边设置相关（好友和群聊都显示）
        WaveSideBarView mWaveSideBarView = binding.contactNewFragmentMainSideBar;
        mWaveSideBarView.setOnSelectIndexItemListener(letter -> {
            try {
                if (_selectIndex == 0 && mContactModels != null && !mContactModels.isEmpty()) {
                    // 好友列表
                    for (int i = 0; i < mContactModels.size(); i++) {
                        if (mContactModels.get(i).getIndex().equals(letter)) {
                            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i + 1, 0); // +1 因为有 header adapter
                            return;
                        }
                    }
                } else if (_selectIndex == 2 && teamListDataList != null && !teamListDataList.isEmpty()) {
                    // 群聊列表（群聊数据也使用 name 字段和 getIndex()）
                    for (int i = 0; i < teamListDataList.size(); i++) {
                        GroupInfoBean item = teamListDataList.get(i);
                        if (item != null && item.getIndex().equals(letter)) {
                            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i + 1, 0); // +1 因为有 header adapter
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略滑动错误
            }
        });
    }

    /**
     * 初始化 header view 的点击事件
     */
    private void initHeaderView() {
        if (headerView == null) return;

        // 4个入口点击事件
        headerView.findViewById(R.id.contact_header_new_friend_ll).setOnClickListener(v -> {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE).withContext(getContext()).navigate();
        });

        headerView.findViewById(R.id.contact_header_my_groups_ll).setOnClickListener(v -> {
            MyGroupListActivity.start(MyGroupListActivity.class, getContext(), null);
        });

        headerView.findViewById(R.id.contact_header_customer_service_ll).setOnClickListener(v -> {
            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_ID_KRY, DataUtil.getKeFuId()).withContext(getContext()).navigate();
        });

        headerView.findViewById(R.id.contact_header_blacklist_ll).setOnClickListener(v -> {
            //XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE).withContext(getContext()).navigate();
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withParam("type", "1")
                    .withContext(Objects.requireNonNull(getContext()))
                    .navigate();
        });

        // Tab 点击事件
        headerView.findViewById(R.id.contact_header_friend_tab_tv).setOnClickListener(v -> {
            switchToFriendTab();
        });
        headerView.findViewById(R.id.contact_header_group_tab_tv).setOnClickListener(v -> {
            switchToGroupTab();
        });
        headerView.findViewById(R.id.contact_header_team_tab_tv).setOnClickListener(v -> {
            switchToTeamTab();
        });

        // 默认选中好友 Tab
        TextView friendTab = headerView.findViewById(R.id.contact_header_friend_tab_tv);
        TextView groupTab = headerView.findViewById(R.id.contact_header_group_tab_tv);
        TextView teamTab = headerView.findViewById(R.id.contact_header_team_tab_tv);

        friendTab.setSelected(true);
        groupTab.setSelected(false);
        teamTab.setSelected(false);
    }

    /**
     * 更新 header 中的角标
     */
    private void updateHeaderBadge() {
        if (headerView == null) return;
        TextView badgeTv = headerView.findViewById(R.id.contact_header_new_friend_num_tv);
        if (applyNumBean.friendApplyNum > 0) {
            badgeTv.setVisibility(View.VISIBLE);
            badgeTv.setText(String.valueOf(applyNumBean.friendApplyNum));
        } else {
            badgeTv.setVisibility(View.GONE);
        }

        TextView groupnum = headerView.findViewById(R.id.contact_header_group_num_tv);
        if (applyNumBean.groupApplyNum > 0) {
            groupnum.setVisibility(View.VISIBLE);
            groupnum.setText(String.valueOf(applyNumBean.groupApplyNum));
        } else {
            groupnum.setVisibility(View.GONE);
        }
    }

    protected void loadTitle() {

    }


    /**
     * 切换到好友 Tab
     */
    private void switchToFriendTab() {
        _selectIndex = 0;
        binding.contactNewFragmentMainSideBar.setVisibility(View.VISIBLE);
        // 刷新 header 更新 Tab 状态
        headerAdapter.notifyItemChanged(0);
        // 每次切换都请求最新数据
        loadFriendList();
    }

    /**
     * 加载好友列表数据
     */
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
                // 重新创建 adapter
                friendAdapter = new SimpleFriendListAdapter();
                friendAdapter.contacts = mContactModels;
                friendAdapter.setItems(mContactModels);
                // 绑定点击事件
                Activity that = getActivity();
                friendAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
                    @Override
                    public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                        GroupInfoBean friend = baseQuickAdapter.getItem(i);
                        if (friend != null) {
                            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                                    .withParam(RouterConstant.CHAT_ID_KRY, friend.userId)
                                    .withParam("type", "1")
                                    .withContext(requireActivity())
                                    .navigate();
                        }
                    }
                });

                if (_selectIndex == 0) {
                    ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, friendAdapter);
                    binding.contactNewFragmentRv.setAdapter(concatAdapter);
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    /**
     * 切换到分组 Tab
     */
    private void switchToGroupTab() {
        _selectIndex = 1;
        binding.contactNewFragmentMainSideBar.setVisibility(View.GONE);

        // 重新创建 adapter 显示空列表
        groupListAdapter = new SimpleFriendListAdapter();
        groupListAdapter.setItems(new ArrayList<>());

        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, groupListAdapter);
        binding.contactNewFragmentRv.setAdapter(concatAdapter);
    }

    /**
     * 切换到群聊 Tab
     */
    private void switchToTeamTab() {
        _selectIndex = 2;
        binding.contactNewFragmentMainSideBar.setVisibility(View.VISIBLE);
        // 刷新 header 更新 Tab 状态
        headerAdapter.notifyItemChanged(0);

        // 每次切换都请求最新数据
        loadTeamList();
    }

    /**
     * 加载群聊列表数据
     */
    private void loadTeamList() {
        HttpUtil.apiW().group_userGroups(new RegisterBean()).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                try {
                    Type type = new TypeToken<List<GroupInfoBean>>() {
                    }.getType();
                    teamListDataList = new Gson().fromJson(body.data.toString(), type);
                    if (teamListDataList == null) {
                        teamListDataList = new ArrayList<>();
                    }
                    // 排序（增加空值检查）
                    Collections.sort(teamListDataList, new Comparator<GroupInfoBean>() {
                        @Override
                        public int compare(GroupInfoBean o1, GroupInfoBean o2) {
                            if (o1 == null || o2 == null) return 0;
                            String name1 = o1.name;
                            String name2 = o2.name;
                            String firstLetter = FirstLetterUtil.getFirstLetter(name1);
                            String secondLetter = FirstLetterUtil.getFirstLetter(name2);
                            return firstLetter.compareTo(secondLetter);
                        }
                    });

                    // 重新创建 adapter（群聊使用 SimpleFriendListAdapter 支持字母分组）
                    teamListAdapter = new SimpleFriendListAdapter();
                    teamListAdapter.contacts = teamListDataList;
                    teamListAdapter.setItems(teamListDataList);

                    // 绑定点击事件
                    Activity that = getActivity();
                    if (that != null) {
                        teamListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
                            @Override
                            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                                GroupInfoBean item = baseQuickAdapter.getItem(i);
                                if (item != null && item.groupId != null) {
                                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                                            .withParam(RouterConstant.CHAT_ID_KRY, item.groupId)
                                            .withContext(that)
                                            .navigate();
                                }
                            }
                        });
                    }

                    // 设置 adapter 显示数据
                    if (_selectIndex == 2) {
                        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, teamListAdapter);
                        binding.contactNewFragmentRv.setAdapter(concatAdapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    teamListDataList = new ArrayList<>();
                    teamListAdapter = new SimpleFriendListAdapter();
                    teamListAdapter.setItems(teamListDataList);
                    if (_selectIndex == 2) {
                        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, teamListAdapter);
                        binding.contactNewFragmentRv.setAdapter(concatAdapter);
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                // 请求失败时显示空列表
                teamListDataList = new ArrayList<>();
                teamListAdapter = new SimpleFriendListAdapter();
                teamListAdapter.setItems(teamListDataList);
                if (_selectIndex == 2 && binding != null && binding.contactNewFragmentRv != null) {
                    ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, teamListAdapter);
                    binding.contactNewFragmentRv.setAdapter(concatAdapter);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v == binding.contactNewFragmentSearchIv) {
            XKitRouter.withKey("FunSystem_Notice_New_Activity").withContext(requireContext()).navigate();
        } else if (v == binding.contactNewFragmentSearchLl) {
            XKitRouter.withKey("SearchNewActivity").withContext(requireContext()).navigate();
        } else if (v == binding.contactNewFragmentMoreIv) {
            XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE).withContext(requireContext()).navigate();
        } else if (v == binding.contactNewFragmentSao) {
            EventBus.getDefault().post(new BaseEvent("gotoScan"));
        }
    }


    public void setContactCallback(IContactCallback contactCallback) {
        this.contactCallback = contactCallback;
        this.contactCallback.updateUnreadCount(applyNumBean.friendApplyNum + applyNumBean.groupApplyNum);
    }
}
