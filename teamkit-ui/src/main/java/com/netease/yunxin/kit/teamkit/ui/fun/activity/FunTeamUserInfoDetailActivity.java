package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
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
    String groupId, userId;
    int rankState;
    int addFriendsState;
    boolean isFriend = false;
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
        userId = getIntent().getStringExtra("userId");
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
        if (!TextUtils.isEmpty(groupInfoBean.memberCode)
                && !"0".equals(groupInfoBean.memberCode)) {
            binding.funTeamUserInfoDetailAccountTv.setText(groupInfoBean.memberCode);
        } else {
            binding.funTeamUserInfoDetailAccountTv.setText("--");
        }
        if (groupInfoBean.grade > 0) {
            binding.funTeamUserInfoDetailGradeIv.setVisibility(View.VISIBLE);
            String imageName = "mine_grade_level_" + groupInfoBean.grade;
            Resources resources = getResources();
            int resId = resources.getIdentifier(imageName, "mipmap", getPackageName());
            // 如果找到了资源，则可以使用这个ID获取Drawable
            Drawable drawable = null;
            if (resId > 0) {
                drawable = ContextCompat.getDrawable(this, resId);
            }
            // 如果需要将drawable设置到ImageView中
            if (drawable != null) {
                binding.funTeamUserInfoDetailGradeIv.setImageDrawable(drawable);
            }
        }

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowTv.setText("邀请人");
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowTv.setText("备注名");
        binding.funTeamUserInfoDetailLahei.viewTitleArrowTv.setText("加入黑名单");
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowTv.setText("禁止领取红包");
        binding.funTeamUserInfoDetailTichu.viewTitleArrowTv.setText("踢出群聊");
        binding.funTeamUserInfoDetailJinyan.viewTitleArrowTv.setText("禁言");

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowArrowIv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailLahei.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBottomTv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailAccountTv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailJinyan.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailJinyan.viewTitleArrowLl.setOnClickListener(this);

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

            binding.funTeamUserInfoDetailJinyan.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinyan.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinyan.viewTitleArrowArrowIv.setVisibility(View.GONE);

            //获取禁言状态
            NIMClient.getService(TeamService.class).queryTeamMember(groupId, userId).setCallback(new RequestCallbackWrapper<TeamMember>() {
                @Override
                public void onResult(int code, TeamMember result, Throwable exception) {
                    binding.funTeamUserInfoDetailJinyan.viewTitleArrowRightTvSwitch.setSelected(result.isMute());
                }
            });
        }
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setText(groupInfoBean.inviteName);
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

    /** 分页 friendListPage（每页 100）判断是否已为好友，对齐 changsheng */
    protected void _requestData1() {
        isFriend = false;
        friendBean = null;
        _requestFriendListPageForUser(1);
    }

    private void _requestFriendListPageForUser(int page) {
        ParamsBean registerBean = new ParamsBean();
        registerBean.page = page;
        HttpUtil.apiW()
                .friends_friendListPage(registerBean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                List<GroupInfoBean> userList = Collections.emptyList();
                                try {
                                    if (body != null && body.data != null) {
                                        Type userListType =
                                                new TypeToken<List<GroupInfoBean>>() {}.getType();
                                        userList =
                                                new Gson()
                                                        .fromJson(body.data.toString(), userListType);
                                    }
                                } catch (Exception e) {
                                    userList = Collections.emptyList();
                                }
                                if (userList == null) {
                                    userList = Collections.emptyList();
                                }
                                String targetUserId =
                                        groupInfoBean != null ? groupInfoBean.userId : null;
                                for (GroupInfoBean tempBean : userList) {
                                    if (tempBean != null
                                            && tempBean.userId != null
                                            && targetUserId != null
                                            && tempBean.userId.equals(targetUserId)) {
                                        isFriend = true;
                                        friendBean = tempBean;
                                        binding.funTeamUserInfoDetailBottomTv.setText("发消息");
                                        binding.funTeamUserInfoDetailBottomTv.setVisibility(
                                                View.VISIBLE);
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

    private void applyFriendListCheckUi() {
        if (isFriend && (rankState == 1 || rankState == 2)) {
            binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
            if (friendBean != null) {
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setText(
                        friendBean.remark);
            }
            binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.VISIBLE);
        }
        if (shouldFetchGroupManageForAddFriend()) {
            HttpUtil.apiW()
                    .group_groupManage(groupId)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call,
                                        Response<NetData> response,
                                        NetData body) {
                                    GroupInfoBean groupManageInfo =
                                            new Gson()
                                                    .fromJson(
                                                            body.data.toString(),
                                                            GroupInfoBean.class);
                                    addFriendsState = groupManageInfo.addFriendsState;
                                    updateAddFriendButtonVisibility();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {
                                    updateAddFriendButtonVisibility();
                                }
                            });
        } else {
            updateAddFriendButtonVisibility();
        }
    }

    /**
     * 仅普通成员查看普通成员时需要读取群成员保护开关。
     */
    private boolean shouldFetchGroupManageForAddFriend() {
        if (groupInfoBean == null) {
            return false;
        }
        if (TextUtils.equals(groupInfoBean.userId, DataUtil.getUserid())) {
            return false;
        }
        boolean viewerIsAdmin = rankState == 1 || rankState == 2;
        boolean targetIsAdmin = groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2;
        return !viewerIsAdmin && !targetIsAdmin;
    }

    /**
     * 群主/管理员不受群成员保护限制，始终可加好友；
     * 普通成员查看管理员/群主时始终显示加好友；
     * 普通成员互看时受群成员保护模式控制（addFriendsState == 0 表示保护开启，不可互加）。
     */
    private void updateAddFriendButtonVisibility() {
        binding.funTeamUserInfoDetailBottomTv.setVisibility(View.GONE);
        if (groupInfoBean == null || TextUtils.equals(groupInfoBean.userId, DataUtil.getUserid())) {
            return;
        }
        if (isFriend) {
            binding.funTeamUserInfoDetailBottomTv.setText("发消息");
            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
            return;
        }
        boolean viewerIsAdmin = rankState == 1 || rankState == 2;
        boolean targetIsAdmin = groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2;
        // 群主/管理员可加普通成员；普通成员可加群主/管理员；普通成员互加看保护开关
        if (viewerIsAdmin || targetIsAdmin || addFriendsState == 1) {
            binding.funTeamUserInfoDetailBottomTv.setText("加好友");
            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 群内加好友：已有有效 memberCode 直接进验证页；
     * 否则才用 GET searchByUserId + groupId 补码（0/空禁止，关闭爆粉）。
     */
    private void openAddFriendVerify() {
        if (groupInfoBean == null) {
            ToastUtils.toastMsg("用户信息异常");
            return;
        }
        if (!TextUtils.isEmpty(groupInfoBean.memberCode)
                && !"0".equals(groupInfoBean.memberCode)) {
            XKitRouter.withKey(Constant.FunAddFriendVerifyActivityKey)
                    .withParam("user", new Gson().toJson(groupInfoBean))
                    .withContext(FunTeamUserInfoDetailActivity.this)
                    .navigate();
            return;
        }
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId)) {
            ToastUtils.toastMsg("用户信息异常");
            return;
        }
        HttpUtil.apiW()
                .friends_searchByUserIdInGroup(userId, groupId)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null) {
                                    String msg =
                                            body != null && !TextUtils.isEmpty(body.msg)
                                                    ? body.msg
                                                    : "当前不可添加该成员为好友";
                                    ToastUtils.toastMsg(msg);
                                    return;
                                }
                                UserBean userBean =
                                        new Gson().fromJson(body.data.toString(), UserBean.class);
                                String code = userBean != null ? userBean.memberCode : null;
                                if (TextUtils.isEmpty(code) || "0".equals(code)) {
                                    String msg =
                                            body != null && !TextUtils.isEmpty(body.msg)
                                                    ? body.msg
                                                    : "当前不可添加该成员为好友";
                                    ToastUtils.toastMsg(msg);
                                    return;
                                }
                                groupInfoBean.memberCode = code;
                                if (userBean != null) {
                                    if (!TextUtils.isEmpty(userBean.name)) {
                                        groupInfoBean.name = userBean.name;
                                    }
                                    if (!TextUtils.isEmpty(userBean.avatar)) {
                                        groupInfoBean.avatar = userBean.avatar;
                                    }
                                }
                                XKitRouter.withKey(Constant.FunAddFriendVerifyActivityKey)
                                        .withParam("user", new Gson().toJson(groupInfoBean))
                                        .withContext(FunTeamUserInfoDetailActivity.this)
                                        .navigate();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                ToastUtils.toastMsg("获取用户信息失败");
                            }
                        });
    }

    private void muteMember(String teamId, String account, boolean mute) {
        NIMClient.getService(TeamService.class).muteTeamMember(teamId, account, mute).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                ToastUtils.toastMsg(mute ? "禁言成功" : "解除禁言成功");
                binding.funTeamUserInfoDetailJinyan.viewTitleArrowRightTvSwitch.setSelected(mute);
            }

            @Override
            public void onFailed(int code) {
                ToastUtils.toastMsg("操作失败，code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                ToastUtils.toastMsg("操作异常");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.funTeamUserInfoDetailJinyan.viewTitleArrowLl) {
            boolean mute = !binding.funTeamUserInfoDetailJinyan.viewTitleArrowRightTvSwitch.isSelected();
            muteMember(groupId, userId, mute);
        } else if (v == binding.funTeamUserInfoDetailNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.funTeamUserInfoDetailBottomTv) {
            if (isFriend) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, groupInfoBean.userId)
                        .withContext(FunTeamUserInfoDetailActivity.this)
                        .navigate();
                finish();
            } else {
                openAddFriendVerify();
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

    @Override
    protected void callBackResult(Intent data) {
        super.callBackResult(data);
        String result = data.getStringExtra("result");
        RegisterBean bean = new RegisterBean();
        // 关闭爆粉：备注以 userId 为主，有码再带 memberCode
        bean.userId =
                !TextUtils.isEmpty(groupInfoBean.userId) ? groupInfoBean.userId : userId;
        String memberCode =
                friendBean != null && !TextUtils.isEmpty(friendBean.memberCode)
                        ? friendBean.memberCode
                        : groupInfoBean.memberCode;
        if (!TextUtils.isEmpty(memberCode) && !"0".equals(memberCode)) {
            bean.memberCode = memberCode;
        }
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
