// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNicknameActivity.KEY_TEAM_MY_NICKNAME;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewTeamUsersActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserInfoAdapter;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class FunTeamSettingNew_TeamUsersActivity extends BaseActivity implements View.OnClickListener {

    FunTeamSettingNewTeamUsersActivityBinding binding;
    String groupId;
    String opt_type;
    TeamSettingUserListAdapter adapter = new TeamSettingUserListAdapter();
    GroupInfoBean selfBean;
    GroupInfoBean teamSettingBean;
    ArraySet selectSet = new ArraySet<>();
    ArraySet unSelectSet = new ArraySet<>();

    ArrayList selectArray = new ArrayList<>();
    ArrayList unSelectArray = new ArrayList<>();

    String groupMasterUserId = "";
    List<String> managerUserIdList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        opt_type = getIntent().getStringExtra("opt_type");
        groupId = getIntent().getStringExtra("groupId");
        super.onCreate(savedInstanceState);
        binding =
                FunTeamSettingNewTeamUsersActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();

        if ("1".equals(opt_type)) {
            binding.funTeamSettingNewTeamUsersActivityNav.getTitleView().setText("选择新群主");
            binding.funTeamSettingNewTeamUsersActivityConfirmTv.setVisibility(View.VISIBLE);
        }
        if ("2".equals(opt_type)) {
            binding.funTeamSettingNewTeamUsersActivityNav.getTitleView().setText("选择管理员");
            binding.funTeamSettingNewTeamUsersActivityConfirmTv.setVisibility(View.VISIBLE);
        }
        if (opt_type != null) {

            binding.funTeamSettingNewTeamUsersActivityConfirmTv.setVisibility(View.VISIBLE);
            binding.funTeamSettingNewTeamUsersActivityConfirmTv.setOnClickListener(this);
            adapter.opt_type = Integer.parseInt(opt_type);
        }

        _requestData(1);
    }

    @Override
    protected void _initView() {
        binding.funTeamSettingNewTeamUsersActivityNav.addCloseImageButton().setOnClickListener(this);


        binding.funTeamSettingNewTeamUsersActivitySearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.isEmpty()) {
                    adapter.setItems(dataList);
                    adapter.notifyDataSetChanged();
                } else {
                    ArrayList<GroupInfoBean> tempArr = new ArrayList<>();
                    for (Object tempObj :
                            dataList) {
                        GroupInfoBean temp = (GroupInfoBean) tempObj;
                        if (temp.name.contains(string)) {
                            tempArr.add(temp);
                        }
                    }
                    adapter.setItems(tempArr);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        binding.funTeamSettingNewTeamUsersActivityRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(5, SizeUtils.dp2px(10), false);
        binding.funTeamSettingNewTeamUsersActivityRv.addItemDecoration(gridSpacingItemDecoration);
        binding.funTeamSettingNewTeamUsersActivityRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (opt_type == null) {
//                    if (selfBean.rankState == 1 || selfBean.rankState == 2) {
                    if (selfBean == null || teamSettingBean == null) {
                        ToastUtils.toastMsg("网络错误");
                        finish();
                        return;
                    }
                    GroupInfoBean item = baseQuickAdapter.getItem(i);
                    if (selfBean.rankState == 3 && item.rankState == 3 && teamSettingBean.addFriendsState == 0) {
                        ToastUtils.toastMsg("非管理员不可私聊");
                        return;
                    }
                        XKitRouter.withKey(Constant.FunTeamUserInfoDetailActivityKey)
                                .withParam("groupId",groupId)
                                .withParam("userId",baseQuickAdapter.getItem(i).userId)
                                .withContext(view.getContext())
                                .navigate();
//                    }
                }
                if ("1".equals(opt_type)) {
                    for (Object tempBean:dataList) {
                        GroupInfoBean bean = (GroupInfoBean)tempBean;
                        bean.rankState = 0;
                    }
                    baseQuickAdapter.getItem(i).rankState = 1;
                    adapter.notifyDataSetChanged();
                }
                if ("2".equals(opt_type)) {
                    GroupInfoBean item = baseQuickAdapter.getItem(i);
                    if (item.rankState == 2) {
                        unSelectArray.add(item.userId);
                        if (selectArray.contains(item.userId)) {
                            selectArray.remove(item.userId);
                        }
                    } else if (item.rankState == 3) {
                        selectArray.add(item.userId);
                        if (unSelectArray.contains(item.userId)) {
                            unSelectArray.remove(item.userId);
                        }
                    }
                    baseQuickAdapter.getItem(i).rankState = baseQuickAdapter.getItem(i).rankState == 2? 3 : 2;
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    protected void _requestData(int page) {



        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page + "";
        bean.pageNo ="100";
        LoadingDialog.showDialog(getSupportFragmentManager(), "请求中");
        HttpUtil.apiW().group_groupUserListPost(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>(){}.getType();
                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        for (GroupInfoBean groupInfoBean : tempList) {
                            if (groupInfoBean.rankState == 1) {
                                groupMasterUserId = groupInfoBean.userId;
                            } else if (groupInfoBean.rankState == 2) {
                                managerUserIdList.add(groupInfoBean.userId);
                            }
                        }
                        requestTeamSetting();
//                        if (!tempList.isEmpty()) {
//                            dataList.addAll(tempList);
//                            if (tempList.size() == 100) {
//                                _requestData((page + 1));
//                                return;
//                            }
//
//                        }

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        LoadingDialog.dismissDialog();
                    }
                });
    }
    private void requestTeamSetting() {
        HttpUtil.apiW().group_groupManage(groupId)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        teamSettingBean = new Gson().fromJson(body.data.toString(),GroupInfoBean.class);
                        requestYunXin();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        LoadingDialog.dismissDialog();
                    }
                });
    }
    private void requestYunXin() {

//        LoadingDialog.showDialog(getSupportFragmentManager(), "请求中");
        TeamRepo.getMemberList(
                groupId,
                new FetchCallback<List<UserInfoWithTeam>>() {
                    @Override
                    public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
                        ArrayList<GroupInfoBean> tempArrayList = new ArrayList<>();
                        for (UserInfoWithTeam userInfoWithTeam: param) {
                            GroupInfoBean temp = new GroupInfoBean();
                            temp.name = userInfoWithTeam.getUserInfo().getName();
                            temp.avatar = userInfoWithTeam.getUserInfo().getAvatar();
                            temp.userId = userInfoWithTeam.getUserInfo().getAccount();
                            if (temp.userId.equals(groupMasterUserId)) {
                                temp.rankState = 1;
                            } else if (managerUserIdList.contains(temp.userId)) {
                                temp.rankState = 2;
                            } else {
                                temp.rankState = 3;
                            }
                            tempArrayList.add(temp);
                        }
                        Collections.sort(tempArrayList, new Comparator<GroupInfoBean>() {
                            @Override
                            public int compare(GroupInfoBean o1, GroupInfoBean o2) {
                                if (o1.rankState > o2.rankState) {
                                    return 1; // o1 排在 o2 后面
                                } else if (o1.rankState < o2.rankState) {
                                    return -1; // o1 排在 o2 前面
                                } else {
                                    return 0; // 相等时，保持原来的顺序
                                }
                            }
                        });

                        dataList.addAll(tempArrayList);

                        if (opt_type != null) {
                            for (int i = dataList.size() - 1; i >= 0; i--) {
                                GroupInfoBean tempBean = (GroupInfoBean) dataList.get(i);
                                if ("1".equals(opt_type)) {
                                    if (tempBean.rankState == 1) {
                                        dataList.remove(i);
                                    }
                                }
                                if ("2".equals(opt_type)) {
                                    if ( tempBean.rankState == 1) {
                                        dataList.remove(i);
                                    }
                                }
                            }
                        } else {
                            for (Object tempBean : dataList) {
                                GroupInfoBean tempBean1 = (GroupInfoBean) tempBean;
                                if (tempBean1.userId.equals(DataUtil.getUserid())) {
                                    selfBean = tempBean1;
                                    break;
                                }
                            }
                        }
                        updateUI();

                        LoadingDialog.dismissDialog();
                    }

                    @Override
                    public void onFailed(int code) {
                        LoadingDialog.dismissDialog();
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        LoadingDialog.dismissDialog();
                    }
                });

    }

    void updateUI() {

        adapter.setItems(dataList);
adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewTeamUsersActivityNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.funTeamSettingNewTeamUsersActivityConfirmTv) {
            ArrayList<String> list = new ArrayList<>();
            Intent intent = new Intent();
            for (Object temObj : dataList) {
                GroupInfoBean tempBean = (GroupInfoBean)temObj;
                if ("1".equals(opt_type) && tempBean.rankState == 1) {
                    list.add(tempBean.userId);
                }
//                if ("2".equals(opt_type) && tempBean.rankState == 2) {
//                    list.add(tempBean.userId);
//                }
            }

            if ("2".equals(opt_type)) {

                intent.putExtra("userIds",selectArray);
                intent.putExtra("un_userIds",unSelectArray);
            } else {
                intent.putExtra("userIds",list);
            }
            intent.putExtra("opt_type",opt_type);
            setResult(RESULT_OK,intent);
            EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
            finish();

        }
    }
}
