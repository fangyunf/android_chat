package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.FirstLetterUtil;
import com.nanchen.wavesidebar.SearchEditText;
import com.nanchen.wavesidebar.Trans2PinYinUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewForbiddenListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserMingDanListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

/** 单人禁言名单（云信 SDK） */
public class FunTeamSettingNew_MuteListActivity extends BaseActivity implements View.OnClickListener {

    private FunTeamSettingNewForbiddenListActivityBinding binding;
    private String groupId;
    private final TeamSettingUserMingDanListAdapter adapter = new TeamSettingUserMingDanListAdapter();
    private final ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    private final ArrayList<GroupInfoBean> mShowModels = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        _getParams();
        if (extras != null && extras.get("groupId") != null) {
            groupId = (String) extras.get("groupId");
        }
        super.onCreate(savedInstanceState);
        binding = FunTeamSettingNewForbiddenListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        _requestData(1);
    }

    @Override
    protected void _initView() {
        binding.funTeamSettingNewForbiddenListActivityNav.setTitle("单人禁言名单");
        binding.funTeamSettingNewForbiddenListActivityNav.addCloseImageButton().setOnClickListener(this);

        adapter.opt_type = TeamSettingUserMingDanListAdapter.OPT_MUTE;

        RecyclerView recyclerView = binding.funTeamSettingNewForbiddenListActivityRv;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, (parent, adapterPosition) -> true);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);

        WaveSideBarView sideBar = binding.mainSideBar;
        sideBar.setOnSelectIndexItemListener(
                letter -> {
                    for (int i = 0; i < mContactModels.size(); i++) {
                        if (mContactModels.get(i).getIndex().equals(letter)) {
                            ((LinearLayoutManager) recyclerView.getLayoutManager())
                                    .scrollToPositionWithOffset(i, 0);
                            return;
                        }
                    }
                });

        SearchEditText searchEditText = binding.funTeamSettingNewForbiddenListActivityEt;
        searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        mShowModels.clear();
                        String keyword = s.toString();
                        for (GroupInfoBean model : mContactModels) {
                            String pinyin = Trans2PinYinUtil.trans2PinYin(model.getName());
                            if (pinyin.contains(keyword) || model.getName().contains(keyword)) {
                                mShowModels.add(model);
                            }
                        }
                        adapter.setItems(mShowModels);
                        adapter.notifyDataSetChanged();
                    }
                });

        adapter.addOnItemChildClickListener(
                R.id.cell_fun_team_setting_users_mingdan_state_tv,
                new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
                    @Override
                    public void onItemClick(
                            @NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter,
                            @NonNull View view,
                            int i) {
                        GroupInfoBean item = baseQuickAdapter.getItem(i);
                        if (item == null || item.rankState == 1 || item.rankState == 2) {
                            return;
                        }
                        boolean targetMute = item.forbidState != 1;
                        muteMemberByNim(item, targetMute);
                    }
                });
    }

    private void muteMemberByNim(GroupInfoBean item, boolean mute) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(item.userId)) {
            return;
        }
        NIMClient.getService(TeamService.class)
                .muteTeamMember(groupId, item.userId, mute)
                .setCallback(
                        new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                item.forbidState = mute ? 1 : 0;
                                adapter.notifyDataSetChanged();
                                ToastUtils.toastMsg(mute ? "已禁言" : "已解除禁言");
                            }

                            @Override
                            public void onFailed(int code) {
                                ToastUtils.toastMsg("操作失败(" + code + ")");
                            }

                            @Override
                            public void onException(Throwable exception) {
                                ToastUtils.toastMsg("操作失败");
                            }
                        });
    }

    private void applyMuteStateFromNim() {
        Set<String> mutedAccounts = new HashSet<>();
        List<TeamMember> muted =
                NIMClient.getService(TeamService.class).queryMutedTeamMembers(groupId);
        if (muted != null) {
            for (TeamMember member : muted) {
                if (member != null && !TextUtils.isEmpty(member.getAccount())) {
                    mutedAccounts.add(member.getAccount());
                }
            }
        }
        for (GroupInfoBean bean : mContactModels) {
            bean.forbidState = mutedAccounts.contains(bean.userId) ? 1 : 0;
        }
    }

    protected void _requestData(int page) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page + "";
        bean.pageNo = "100";
        HttpUtil.apiW()
                .group_groupUserListPost(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                                List<GroupInfoBean> tempList =
                                        new Gson().fromJson(body.data.toString(), type);
                                if (!tempList.isEmpty()) {
                                    mContactModels.addAll(tempList);
                                    if (tempList.size() == 100) {
                                        _requestData(page + 1);
                                        return;
                                    }
                                }
                                Collections.sort(
                                        mContactModels,
                                        new Comparator<GroupInfoBean>() {
                                            @Override
                                            public int compare(
                                                    GroupInfoBean o1, GroupInfoBean o2) {
                                                String first =
                                                        FirstLetterUtil.getFirstLetter(o1.name);
                                                String second =
                                                        FirstLetterUtil.getFirstLetter(o2.name);
                                                return first.compareTo(second);
                                            }
                                        });
                                applyMuteStateFromNim();
                                updateUI();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }

    private void updateUI() {
        adapter.contacts = mContactModels;
        adapter.setItems(mContactModels);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewForbiddenListActivityNav.addCloseImageButton()) {
            finish();
        }
    }
}
