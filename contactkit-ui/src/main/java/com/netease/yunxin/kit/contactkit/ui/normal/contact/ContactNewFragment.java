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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunPopItemFactory;
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
    protected IContactCallback contactCallback;
    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    ArrayList<GroupInfoBean> mContactModelsAll = new ArrayList<>(); // 保存完整的好友列表用于搜索
    SimpleFriendListAdapter friendAdapter = new SimpleFriendListAdapter();
    GroupInfoBean applyNumBean = new GroupInfoBean();
    SimpleFriendListAdapter groupListAdapter = new SimpleFriendListAdapter();
    List<GroupInfoBean> groupListDataList = new ArrayList<>();
    List<GroupInfoBean> teamListDataListAll = new ArrayList<>(); // 保存完整的群聊列表用于搜索
    SimpleFriendListAdapter teamListAdapter = new SimpleFriendListAdapter();
    List<GroupInfoBean> teamListDataList = new ArrayList<>();
    NewFriendListAdapter verifyAdapter = new NewFriendListAdapter();
    List<UserBean> verifyList = new ArrayList<>();
    List<UserBean> verifyListAll = new ArrayList<>(); // 保存完整的新好友列表用于搜索
    int _selectIndex = 0;
    private ContactNewFragmentBinding binding;
    private ContactEntranceBean verifyBean;
    private View headerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter;
    private EditText searchEditText; // 搜索输入框

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
        // 初始化搜索输入框
        searchEditText = binding.funConversationFragmentEt;
        initSearchListener();

        _initViews();
        _requestData();

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        getMessageCount();
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
    }


    private void getMessageCount() {
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
                    contactCallback.updateUnreadCount(applyNumBean.friendApplyNum);
                }

            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    @Override
    protected void _requestData() {
//        _requestMemeber(1);
        loadFriendList();

        getMessageCount();

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

                // 保存完整列表用于搜索
                verifyListAll = new ArrayList<>(verifyList);

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

                    if (friendTab != null) {
                        friendTab.setSelected(_selectIndex == 0);
                    }
                    if (groupTab != null) {
                        groupTab.setSelected(_selectIndex == 1);
                    }
                    if (teamTab != null) {
                        teamTab.setSelected(_selectIndex == 2);
                    }
                    if (friendLine != null) {
                        friendLine.setVisibility(_selectIndex == 0 ? View.VISIBLE : View.INVISIBLE);
                    }
                    if (groupLine != null) {
                        groupLine.setVisibility(_selectIndex == 1 ? View.VISIBLE : View.INVISIBLE);
                    }
                    if (teamLine != null) {
                        teamLine.setVisibility(_selectIndex == 2 ? View.VISIBLE : View.INVISIBLE);
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
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE).withContext(getContext()).navigate();
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

    private void updateEmptyView(boolean empty) {
        if (binding == null || binding.contactNewFragmentEmptyTv == null) {
            return;
        }
        binding.contactNewFragmentEmptyTv.setVisibility(empty ? View.VISIBLE : View.GONE);
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

                // 保存完整列表用于搜索
                mContactModelsAll = new ArrayList<>(mContactModels);

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

                // 如果有搜索内容，重新执行搜索
                if (searchEditText != null && !TextUtils.isEmpty(searchEditText.getText().toString().trim())) {
                    performSearch(searchEditText.getText().toString().trim());
                    return;
                }
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
                            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE).withParam(RouterConstant.CHAT_ID_KRY, friend.userId).withParam("type", "1").withContext(requireActivity()).navigate();
                        }
                    }
                });

                if (_selectIndex == 0) {
                    ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, friendAdapter);
                    binding.contactNewFragmentRv.setAdapter(concatAdapter);
                    updateEmptyView(mContactModels == null || mContactModels.isEmpty());
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                if (_selectIndex == 0) {
                    updateEmptyView(true);
                }
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

                    // 保存完整列表用于搜索
                    teamListDataListAll = new ArrayList<>(teamListDataList);

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

                    // 如果有搜索内容，重新执行搜索
                    if (searchEditText != null && !TextUtils.isEmpty(searchEditText.getText().toString().trim())) {
                        performSearch(searchEditText.getText().toString().trim());
                        return;
                    }

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
                                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE).withParam(RouterConstant.CHAT_ID_KRY, item.groupId).withContext(that).navigate();
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
            // XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE).withContext(requireContext()).navigate();

            Context context = getContext();
            int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
            ContentListPopView contentListPopView = new ContentListPopView.Builder(context).addItem(FunPopItemFactory.getCreateAdvancedTeamItem(context, memberLimit)).addItem(FunPopItemFactory.getDivideLineItem(context)).addItem(FunPopItemFactory.getAddFriendItem(context)).addItem(FunPopItemFactory.getDivideLineItem(context)).addItem(FunPopItemFactory.getScanItem(context)).enableShadow(false).backgroundRes(com.netease.yunxin.kit.conversationkit.ui.R.drawable.fun_conversation_view_pop_bg).build();
            contentListPopView.showAsDropDown(v, (int) requireContext().getResources().getDimension(com.netease.yunxin.kit.conversationkit.ui.R.dimen.pop_margin_right), 0);

        }
    }


    /**
     * 初始化搜索监听器
     */
    private void initSearchListener() {
        if (searchEditText == null) return;

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                performSearch(searchText);
            }
        });
    }

    /**
     * 执行搜索
     */
    private void performSearch(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            // 搜索为空，恢复原始列表
            restoreOriginalList();
            return;
        }

        // 根据当前选中的 Tab 进行搜索
        switch (_selectIndex) {
            case 0:
                // 搜索好友列表
                searchFriendList(searchText);
                break;
            case 1:
                // 搜索分组列表（如果有数据）
                searchGroupList(searchText);
                break;
            case 2:
                // 搜索群聊列表
                searchTeamList(searchText);
                break;
        }
    }

    /**
     * 搜索好友列表
     */
    private void searchFriendList(String searchText) {
        if (mContactModelsAll.isEmpty()) {
            return;
        }

        ArrayList<GroupInfoBean> filteredList = new ArrayList<>();
        for (GroupInfoBean contact : mContactModelsAll) {
            if (contact == null) continue;

            // 优先匹配备注，如果没有备注则匹配名称
            String matchText = null;
            if (contact.remark != null && !contact.remark.isEmpty()) {
                matchText = contact.remark;
            } else if (contact.name != null && !contact.name.isEmpty()) {
                matchText = contact.name;
            }

            if (matchText != null && matchText.toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(contact);
            }
        }

        // 更新 adapter
        friendAdapter.contacts = filteredList;
        friendAdapter.setItems(filteredList);
        friendAdapter.notifyDataSetChanged();
        updateEmptyView(filteredList.isEmpty());
    }

    /**
     * 搜索分组列表
     */
    private void searchGroupList(String searchText) {
        // 分组列表搜索逻辑（如果有数据）
        // 目前分组列表为空，可以在这里添加搜索逻辑
    }

    /**
     * 搜索群聊列表
     */
    private void searchTeamList(String searchText) {
        if (teamListDataListAll.isEmpty()) {
            return;
        }

        ArrayList<GroupInfoBean> filteredList = new ArrayList<>();
        for (GroupInfoBean team : teamListDataListAll) {
            if (team == null) continue;

            if (team.name != null && team.name.toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(team);
            }
        }

        // 更新 adapter
        teamListAdapter.contacts = filteredList;
        teamListAdapter.setItems(filteredList);
        teamListAdapter.notifyDataSetChanged();
    }

    /**
     * 恢复原始列表
     */
    private void restoreOriginalList() {
        switch (_selectIndex) {
            case 0:
                // 恢复好友列表
                friendAdapter.contacts = mContactModelsAll;
                friendAdapter.setItems(mContactModelsAll);
                friendAdapter.notifyDataSetChanged();
                updateEmptyView(mContactModelsAll == null || mContactModelsAll.isEmpty());
                break;
            case 1:
                // 恢复分组列表
                groupListAdapter.setItems(new ArrayList<>());
                groupListAdapter.notifyDataSetChanged();
                break;
            case 2:
                // 恢复群聊列表
                teamListAdapter.contacts = teamListDataListAll;
                teamListAdapter.setItems(teamListDataListAll);
                teamListAdapter.notifyDataSetChanged();
                break;
        }
    }

    public void setContactCallback(IContactCallback contactCallback) {
        this.contactCallback = contactCallback;
        this.contactCallback.updateUnreadCount(applyNumBean.friendApplyNum + applyNumBean.groupApplyNum);
    }
}
