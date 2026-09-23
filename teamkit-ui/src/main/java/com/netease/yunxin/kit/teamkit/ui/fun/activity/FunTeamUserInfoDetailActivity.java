package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUserInfoDetailBinding;
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

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunTeamUserInfoDetailActivity extends BaseActivity implements View.OnClickListener {
    FunTeamUserInfoDetailBinding binding;
    GroupInfoBean groupInfoBean;
    String groupId;
    String userId;
    int rankState;
    int addFriendsState;
    boolean isFriend = false;
    GroupInfoBean friendBean;
    private boolean memberLoaded = false;
    private boolean roleLoaded = false;

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
        binding.funTeamUserInfoDetailAccountTv.setText(groupInfoBean.memberCode);
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


    private void muteMember(String teamId, String account, boolean mute) {
        NIMClient.getService(TeamService.class).muteTeamMember(teamId, account, mute)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
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

    void requestDataWith(String groupId, String userId) {
        memberLoaded = false;
        roleLoaded = false;
        // 1) 云信查自己身份（群主/管理员），避免再走 groupHomeInfo
        String selfId = IMKitClient.account();
        if (selfId == null || selfId.isEmpty()) {
            roleLoaded = true;
            tryShowAfterReady();
        } else {
            NIMClient.getService(TeamService.class)
                    .queryTeamMember(groupId, selfId)
                    .setCallback(
                            new RequestCallbackWrapper<TeamMember>() {
                                @Override
                                public void onResult(int code, TeamMember result, Throwable exception) {
                                    if (result != null) {
                                        TeamMemberType type = result.getType();
                                        if (type == TeamMemberType.Owner) {
                                            rankState = 1;
                                        } else if (type == TeamMemberType.Manager) {
                                            rankState = 2;
                                        } else {
                                            rankState = 3;
                                        }
                                    } else {
                                        rankState = 3;
                                    }
                                    roleLoaded = true;
                                    tryShowAfterReady();
                                }
                            });
        }

        // 2) 单成员资料：POST /group/dange
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.userId = userId;
        HttpUtil.apiW()
                .group_dange(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                try {
                                    if (body != null && body.data != null) {
                                        groupInfoBean =
                                                new Gson()
                                                        .fromJson(
                                                                body.data.toString(),
                                                                GroupInfoBean.class);
                                    }
                                } catch (Exception ignore) {
                                    groupInfoBean = null;
                                }
                                memberLoaded = true;
                                tryShowAfterReady();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                memberLoaded = true;
                                ToastUtils.toastMsg("加载成员信息失败");
                                tryShowAfterReady();
                            }
                        });
    }

    private void tryShowAfterReady() {
        if (!memberLoaded || !roleLoaded) {
            return;
        }
        if (groupInfoBean == null) {
            return;
        }
        updateUI();
        _requestData1();
    }

    /** 是否好友：优先本地缓存，没有再查好友列表 */
    protected void _requestData1() {
        isFriend = false;
        friendBean = null;
        List<GroupInfoBean> cached = DataUtil.getFriendInfoList();
        if (cached != null && !cached.isEmpty()) {
            applyFriendCheckResult(cached);
            return;
        }
        requestFriendListLegacy();
    }

    private void requestFriendListLegacy() {
        HttpUtil.apiW()
                .friends_friendList(new RegisterBean())
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                applyFriendCheckResult(parseFriendList(body));
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                applyFriendListCheckUi();
                            }
                        });
    }

    private List<GroupInfoBean> parseFriendList(NetData body) {
        try {
            if (body != null && body.data != null) {
                Type userListType = new TypeToken<List<GroupInfoBean>>() {}.getType();
                List<GroupInfoBean> list = new Gson().fromJson(body.data.toString(), userListType);
                return list == null ? Collections.emptyList() : list;
            }
        } catch (Exception ignore) {
        }
        return Collections.emptyList();
    }

    private void applyFriendCheckResult(List<GroupInfoBean> userList) {
        String targetUserId = groupInfoBean != null ? groupInfoBean.userId : null;
        if (userList != null && targetUserId != null) {
            for (GroupInfoBean tempBean : userList) {
                if (tempBean != null
                        && tempBean.userId != null
                        && tempBean.userId.equals(targetUserId)) {
                    isFriend = true;
                    friendBean = tempBean;
                    break;
                }
            }
        }
        applyFriendListCheckUi();
    }

    private void applyFriendListCheckUi() {
        if (isFriend) {
            binding.funTeamUserInfoDetailBottomTv.setText("发消息");
            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
        }
        if (rankState == 1 || rankState == 2) {
            if (isFriend) {
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
                if (friendBean != null) {
                    binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowRightTv.setText(
                            friendBean.remark);
                }
                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.VISIBLE);
            }
            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
        } else {
            HttpUtil.apiW()
                    .group_groupManage(groupId)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call,
                                        Response<NetData> response,
                                        NetData body) {
                                    GroupInfoBean tempBean =
                                            new Gson()
                                                    .fromJson(
                                                            body.data.toString(),
                                                            GroupInfoBean.class);
                                    if (tempBean.addFriendsState == 1) {
                                        if (!isFriend) {
                                            binding.funTeamUserInfoDetailBottomTv.setVisibility(
                                                    View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
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

                XKitRouter.withKey(Constant.FunAddFriendVerifyActivityKey)
                        .withParam("user", new Gson().toJson(groupInfoBean))
                        .withContext(FunTeamUserInfoDetailActivity.this)
                        .navigate();
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
            HttpUtil.apiW().groupMember_invitationGroupBanOnLooting(bean)
                    .enqueue(new CommonCallback<NetData>() {
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
            HttpUtil.apiW().group_addDeleteBlack(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
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
        HttpUtil.apiW().group_outGroup(registerBean)
                .enqueue(new CommonCallback<NetData>() {
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
        bean.memberCode = groupInfoBean.memberCode;
        bean.alias = result;
        HttpUtil.apiW().friends_updateRemark(bean)
                .enqueue(new CommonCallback<NetData>() {
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
