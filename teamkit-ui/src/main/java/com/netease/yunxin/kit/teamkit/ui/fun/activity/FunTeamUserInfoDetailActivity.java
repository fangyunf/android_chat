package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUserInfoDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DensityUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ResourceHelper;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunTeamUserInfoDetailActivity extends BaseActivity implements View.OnClickListener {
    FunTeamUserInfoDetailBinding binding;
    GroupInfoBean groupInfoBean;
    String groupId;
    int rankState;
    int addFriendsState;
    boolean isFriend = false;
    boolean isGuanli = false;
    GroupInfoBean friendBean;
    ArrayList<GroupInfoBean> members = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FunTeamUserInfoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.funTeamUserInfoDetailNav.addCloseImageButton().setOnClickListener(this);

        transtStatusBar(binding.funTeamUserInfoDetailNav);
        groupId = getIntent().getStringExtra("groupId");
        String userId = getIntent().getStringExtra("userId");
        requestDataWith(groupId, userId);
//        String type = getIntent().getStringExtra("type");
//        if (type != null && "101".equals(type)) {
//            String groupId1 = getIntent().getStringExtra("groupId");
//            String userId = getIntent().getStringExtra("userId");
//        } else {
//
//            _getParams();
//            groupId = (String) extras.get("groupId");
//            rankState = Integer.parseInt((String) extras.get("rankState"));
//            String result = (String) extras.get("result");
//            groupInfoBean = new Gson().fromJson(result,GroupInfoBean.class);
//
//            updateUI();
//        }
//        _getParams();
//        groupId = (String) extras.get("groupId");
//        rankState = Integer.parseInt((String) extras.get("rankState"));
//        String result = (String) extras.get("result");
//        groupInfoBean = new Gson().fromJson(result,GroupInfoBean.class);


    }

    void updateUI() {
        binding.funTeamUserInfoDetailBottomTv.setOnClickListener(this);
        binding.funTeamUserInfoDetailNameTv.setText(groupInfoBean.name);
        GlideUtil.yh_loadImageRoundedCorner(this, binding.funTeamUserInfoDetailHeadIv, groupInfoBean.avatar, 30);
        binding.funTeamUserInfoDetailAccountTv.setText(groupInfoBean.memberCode);
        if (groupInfoBean.grade > 0) {
            binding.funTeamUserInfoDetailGradeIv.setVisibility(View.VISIBLE);
            binding.ivGradeBg.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailGradeIv.setImageDrawable(ResourceHelper.getGradeDrawable(this, groupInfoBean.grade));
            binding.funTeamUserInfoDetailNameTv.setTextColor(ResourceHelper.getGradeColor(this, groupInfoBean.grade));
            binding.ivGradeBg.setImageDrawable(ResourceHelper.getGradeBackground(this, groupInfoBean.grade));
//            binding.funTeamUserInfoDetailGradeIv.setVisibility(View.VISIBLE);
//            String imageName = "mine_grade_level_" + groupInfoBean.grade;
//            Resources resources = getResources();
//            int resId = resources.getIdentifier(imageName, "mipmap", getPackageName());
//            // 如果找到了资源，则可以使用这个ID获取Drawable
//            Drawable drawable = null;
//            if (resId > 0) {
//                drawable = ContextCompat.getDrawable(this, resId);
//            }
//            // 如果需要将drawable设置到ImageView中
//            if (drawable != null) {
//                binding.funTeamUserInfoDetailGradeIv.setImageDrawable(drawable);
//            }
        }

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowTv.setText("邀请人");
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowTv.setText("备注名");
        binding.funTeamUserInfoDetailLahei.viewTitleArrowTv.setText("加入黑名单");
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowTv.setText("禁止领取红包");
        binding.funTeamUserInfoDetailTichu.viewTitleArrowTv.setText("踢出群聊");

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowArrowIv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailLahei.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBottomTv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailAccountTv.setVisibility(View.GONE);
        if (rankState == 1 || rankState == 2) {
            binding.funTeamUserInfoDetailAccountTv.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setOnClickListener(this);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowArrowIv.setVisibility(View.GONE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setOnClickListener(this);

            binding.funTeamUserInfoDetailLahei.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailLahei.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailLahei.viewTitleArrowArrowIv.setVisibility(View.GONE);
            binding.funTeamUserInfoDetailLahei.viewTitleArrowRightTvSwitch.setOnClickListener(this);

            binding.funTeamUserInfoDetailTichu.viewTitleArrowRightTvSwitch.setOnClickListener(this);
            binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setOnClickListener(this);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.forbidState == 1);
        }
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setText(groupInfoBean.inviteName);
        installMachineSecondMemberPanel();
    }

    void requestDataWith(String groupId, String userId) {
        HttpUtil.apiW().group_groupHomeInfo(groupId).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                GroupInfoBean tempGroupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                rankState = tempGroupInfoBean.rankState;
                _requestPeople(1, userId);
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });

    }

    void _requestPeople(int page, String userId) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page + "";
        bean.pageNo = "100";
        HttpUtil.apiW().group_groupUserListPost(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);

                members.addAll(tempList);
                if (!tempList.isEmpty()) {
                    if (tempList.size() == 100) {
                        _requestPeople((page + 1), userId);
                        return;
                    }

                }
                for (GroupInfoBean temp : members) {
                    if (temp.userId.equals(userId)) {
                        groupInfoBean = temp;
                        if (temp.rankState == 1 || temp.rankState == 2) {
                            isGuanli = true;
                        }
                        break;
                    }
                }

                if (groupInfoBean != null) {
                    updateUI();
                    _requestData1();
                }

            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    /**
     * 分页拉取好友列表，判断是否已为好友（与联系人页 {@code friends_friendListPage} 一致，每页 100 条）。
     */
    protected void _requestData1() {
        isFriend = false;
        friendBean = null;
        _requestFriendListPageForUser(1);
    }

    private void _requestFriendListPageForUser(int page) {
        ParamsBean registerBean = new ParamsBean();
        registerBean.page = page;
        HttpUtil.apiW().friends_friendListPage(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                List<GroupInfoBean> userList = Collections.emptyList();
                try {
                    if (body != null && body.data != null) {
                        Type userListType = new TypeToken<List<GroupInfoBean>>() {}.getType();
                        userList = new Gson().fromJson(body.data.toString(), userListType);
                    }
                } catch (Exception e) {
                    userList = Collections.emptyList();
                }
                if (userList == null) {
                    userList = Collections.emptyList();
                }

                String targetUserId = groupInfoBean != null ? groupInfoBean.userId : null;
                for (GroupInfoBean tempBean : userList) {
                    if (tempBean != null
                            && tempBean.userId != null
                            && targetUserId != null
                            && tempBean.userId.equals(targetUserId)) {
                        isFriend = true;
                        friendBean = tempBean;
                        binding.funTeamUserInfoDetailBottomTv.setText("发消息");
                        binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
                        applyFriendListCheckUi();
                        return;
                    }
                }

                if (userList.size() == 100) {
                    _requestFriendListPageForUser(page + 1);
                    return;
                }
                applyFriendListCheckUi();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                if (page == 1) {
                    applyFriendListCheckUi();
                }
            }
        });
    }

    /** 好友列表分页检索结束后的 UI（与原 {@code friends_friendList} 单次逻辑一致） */
    private void applyFriendListCheckUi() {
        if (rankState == 1 || rankState == 2) {
            if (isFriend) {
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
                if (friendBean != null) {
                    binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setText(friendBean.remark);
                }
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.VISIBLE);
            }
            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
        } else {
            HttpUtil.apiW().group_groupManage(groupId).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    GroupInfoBean tempBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                    if (tempBean.addFriendsState == 1) {
                        if (!isFriend) {
                            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (!isFriend && isGuanli) {
                            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.funTeamUserInfoDetailNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.funTeamUserInfoDetailBottomTv) {
            if (isFriend) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_ID_KRY, groupInfoBean.userId).withContext(FunTeamUserInfoDetailActivity.this).navigate();
                finish();
            } else {

                XKitRouter.withKey(Constant.FunAddFriendVerifyActivityKey).withParam("user", new Gson().toJson(groupInfoBean)).withContext(FunTeamUserInfoDetailActivity.this).navigate();
//                UserBean bean = baseQuickAdapter.getItem(i);
//                bean.page_type = 100;
//                HashMap map = new HashMap();
//                map.put("user",new Gson().toJson(user));
//                FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class,that,map);
            }


        } else if (v == binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch) {
            int targetState = binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.isSelected() ? 0 : 1;
            RegisterBean bean = new RegisterBean();
            bean.groupId = groupId;
            ArrayList list = new ArrayList<>();
            list.add(groupInfoBean.userId);
            bean.members = list;
            bean.state = targetState;
            HttpUtil.apiW().groupMember_invitationGroupBanOnLooting(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("设置成功");
                    binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setSelected(targetState == 1);
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        } else if (v == binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl) {

            Intent intent = new Intent(this, ModifyInfoActivity.class);
            intent.putExtra("title", "修改备注");
            intent.putExtra("type", "4");
            if (friendBean != null && friendBean.remark != null) {

                intent.putExtra("hint", friendBean.remark);
            }
            activityResultLauncher.launch(intent);
        } else if (v == binding.funTeamUserInfoDetailTichu.viewTitleArrowLl) {
            tichuuser(true);
        } else if (v == binding.funTeamUserInfoDetailLahei.viewTitleArrowRightTvSwitch) {

            RegisterBean registerBean = new RegisterBean();
            registerBean.userId = groupInfoBean.userId;
            registerBean.groupId = groupId;
            HttpUtil.apiW().group_addDeleteBlack(registerBean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("拉黑成功");
                    tichuuser(false);
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        }
    }

    void tichuuser(boolean needToast) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.groupId = groupId;
        ArrayList ids = new ArrayList<>();
        ids.add(groupInfoBean.userId);
        registerBean.members = ids;
        HttpUtil.apiW().group_outGroup(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (needToast) {

                    ToastUtils.toastMsg(body.msg);
                }
                EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                finish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    private void installMachineSecondMemberPanel() {
        if (groupInfoBean == null || groupInfoBean.userId == null || groupInfoBean.userId.isEmpty()) return;
        try {
            ViewGroup parent = findViewById(android.R.id.content);
            Class<?> cls = Class.forName("com.machinesecond.api.MachineSecond");
            String displayName = groupInfoBean.name == null || groupInfoBean.name.isEmpty() ? groupInfoBean.userId : groupInfoBean.name;
            cls.getMethod("installMemberProfile", ViewGroup.class, String.class, String.class)
                    .invoke(null, parent, groupInfoBean.userId, displayName);
        } catch (Throwable ignored) {
        }
    }

    private void uninstallMachineSecondMemberPanel() {
        try {
            Class<?> cls = Class.forName("com.machinesecond.api.MachineSecond");
            cls.getMethod("uninstallMemberProfile").invoke(null);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        uninstallMachineSecondMemberPanel();
        super.onDestroy();
    }

    @Override
    protected void callBackResult(Intent data) {
        super.callBackResult(data);
        String result = data.getStringExtra("result");
        RegisterBean bean = new RegisterBean();
        bean.memberCode = groupInfoBean.memberCode;
        bean.alias = result;
        HttpUtil.apiW().friends_updateRemark(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg(body.msg);
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
                if (friendBean != null) {
                    friendBean.remark = result;
                    binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setText(result);
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }
}
