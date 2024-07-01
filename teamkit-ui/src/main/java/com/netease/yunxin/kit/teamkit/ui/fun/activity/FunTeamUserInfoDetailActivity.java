package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUserInfoDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DensityUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

    ArrayList<GroupInfoBean> members = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FunTeamUserInfoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.funTeamUserInfoDetailNav.addCloseImageButton().setOnClickListener(this);

        groupId = getIntent().getStringExtra("groupId");
        String userId = getIntent().getStringExtra("userId");
        requestDataWith(groupId,userId);
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
        GlideUtil.yh_loadImageRoundedCorner(this,binding.funTeamUserInfoDetailHeadIv,groupInfoBean.avatar,30);
        binding.funTeamUserInfoDetailAccountTv.setText(groupInfoBean.memberCode);

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowTv.setText("邀请人");
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowTv.setText("备注名");
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowTv.setText("禁止领取购物券");
        binding.funTeamUserInfoDetailTichu.viewTitleArrowTv.setText("踢出群聊");

        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowArrowIv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailJinzhi.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailBottomTv.setVisibility(View.GONE);
        binding.funTeamUserInfoDetailAccountTv.setVisibility(View.GONE);
        if (rankState == 1 || rankState == 2) {
            binding.funTeamUserInfoDetailAccountTv.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowLl.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setVisibility(View.VISIBLE);

            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowArrowIv.setVisibility(View.GONE);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setOnClickListener(this);
            binding.funTeamUserInfoDetailTichu.viewTitleArrowRightTvSwitch.setOnClickListener(this);
            binding.funTeamUserInfoDetailTichu.viewTitleArrowLl.setOnClickListener(this);
            binding.funTeamUserInfoDetailJinzhi.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.forbidState == 1);
        }
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
        binding.funTeamUserInfoDetailYaoqingren.viewTitleArrowRightTv.setText(groupInfoBean.inviteName);
    }
     void requestDataWith(String groupId,String userId) {
        HttpUtil.apiW().group_groupHomeInfo(groupId)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        GroupInfoBean tempGroupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        rankState = tempGroupInfoBean.rankState;
                        _requestPeople(1,userId);

                    }
                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

    }
    void _requestPeople(int page, String userId) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page +"";
        bean.pageNo ="100";
        HttpUtil.apiW().group_groupUserListPost(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);

                        members.addAll(tempList);
                        if (!tempList.isEmpty()) {
                            if (tempList.size() == 100) {
                                _requestPeople((page + 1),userId);
                                return;
                            }

                        }
                        for (GroupInfoBean temp:
                                members) {
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

    protected void _requestData1() {
        RegisterBean bean = new RegisterBean();
        HttpUtil.apiW().friends_friendList(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type userListType = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        List<GroupInfoBean> userList = new Gson().fromJson(body.data.toString(),userListType);
                        for (GroupInfoBean tempBean :
                                userList) {
                            if (tempBean.userId.equals(groupInfoBean.userId)) {
                                isFriend = true;
//                                binding.funTeamUserInfoDetailBeizhuming.viewTitleArrowLl.setVisibility(View.VISIBLE);
                                binding.funTeamUserInfoDetailBottomTv.setText("发消息");
                                binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);

                                break;
                            }
                        }
                        if (rankState == 1 || rankState == 2) {
                            binding.funTeamUserInfoDetailBottomTv.setVisibility(View.VISIBLE);
                        } else {
                            HttpUtil.apiW().group_groupManage(groupId)
                                    .enqueue(new CommonCallback<NetData>() {
                                        @Override
                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                            GroupInfoBean tempBean = new Gson().fromJson(body.data.toString(),GroupInfoBean.class);

                                            if (tempBean.addFriendsState == 0) {
                                                if (!isFriend) {
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
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });


    }

    @Override
    public void onClick(View v) {
        if (v == binding.funTeamUserInfoDetailNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.funTeamUserInfoDetailBottomTv) {
            if (isFriend) {
                XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
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


        } else if (v == binding.funTeamUserInfoDetailTichu.viewTitleArrowLl) {
            RegisterBean registerBean = new RegisterBean();
            registerBean.groupId = groupId;
            ArrayList ids = new ArrayList<>();
            ids.add(groupInfoBean.userId);
            registerBean.members = ids;
            HttpUtil.apiW().group_outGroup(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            ToastUtils.toastMsg(body.msg);
                            finish();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
}
