package com.netease.yunxin.kit.contactkit.ui.fun.addfriend;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAddFriendVerifyActivityBinding;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DensityUtils;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class FunAddFriendVerifyActivity extends BaseActivity implements View.OnClickListener {
    private UserBean userBean;
    FunAddFriendVerifyActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FunAddFriendVerifyActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.funAddFriendVerifyActivityNav.addCloseImageButton().setOnClickListener(this);

        transtStatusBar(binding.funAddFriendVerifyActivityNav);
        binding.funAddFriendVerifyActivitySendRl.setOnClickListener(this);

        String result = getIntent().getStringExtra("user");
        if (extras != null && extras.get("user") != null) {
            result = (String) extras.get("user");
        }
        // 从聊天「非好友」tip 跳转时只传了账号，需先拉取用户信息
        if (TextUtils.isEmpty(result)) {
            String accountId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
            if (extras != null && extras.getString(RouterConstant.KEY_ACCOUNT_ID_KEY) != null) {
                accountId = extras.getString(RouterConstant.KEY_ACCOUNT_ID_KEY);
            }
            if (!TextUtils.isEmpty(accountId)) {
                fetchUserByAccountAndBind(accountId);
                return;
            }
        }

        if (result != null) {
            userBean = new Gson().fromJson(result, UserBean.class);
        }
        if (extras != null || result != null) {
            String userBeanString = "";
            if (extras != null && extras.get("user") != null) {
                userBeanString = (String) extras.get("user");
            }
            if (result != null) {
                userBeanString = result;
            }
            userBean = new Gson().fromJson(userBeanString, UserBean.class);
            bindUserBean();
        }
    }

    /**
     * 仅传入账号时（如从聊天非好友 tip 进入），先搜索用户再绑定 UI
     */
    private void fetchUserByAccountAndBind(String accountId) {
        RegisterBean bean = new RegisterBean();
        bean.phoneAndCode = accountId;
        bean.type = 0;
        HttpUtil.apiW().friends_search(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        if (body != null && body.data != null) {
                            userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                            bindUserBean();
                        } else {
                            ToastUtils.toastMsg("未找到该用户");
                            finish();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        ToastUtils.toastMsg("获取用户信息失败");
                        finish();
                    }
                });
    }

    private void bindUserBean() {
        if (userBean == null) return;
        GlideUtil.yh_loadImageRoundedCorner(this, binding.funAddFriendVerifyActivityHeadIv, userBean.avatar, DensityUtils.dp2px(30));
        binding.funAddFriendVerifyActivityNameTv.setText(userBean.name);
        binding.funAddFriendVerifyActivityAccountTv.setText("ID:" + userBean.memberCode);
        if (userBean.page_type == 100) {
            binding.funAddFriendVerifyActivityAccountTv.setText("ID:" + userBean.memberCode);
            binding.funAddFriendVerifyActivityNav.getTitleView().setText("好友验证");
            binding.funAddFriendVerifyActivitySendRl.setVisibility(View.GONE);
            binding.funAddFriendVerifyActivityTwoOptLl.setVisibility(View.VISIBLE);
            binding.funAddFriendVerifyActivityEt.setEnabled(false);
            binding.funAddFriendVerifyActivityEt.setText(!userBean.leaveMessage.isEmpty() ? userBean.leaveMessage : "暂无留言");
            binding.funAddFriendVerifyActivityEtTitleTv.setText("对方留言");

            binding.funAddFriendVerifyActivityAggreRl.setOnClickListener(this);
            binding.funAddFriendVerifyActivityRefuseRl.setOnClickListener(this);
        } else if (userBean.page_type == 101) {
            binding.funAddFriendVerifyActivityNameTv.setText(userBean.userName);
            binding.funAddFriendVerifyActivityAccountTv.setText("ID:" + userBean.userMemberCode);
            GlideUtil.yh_loadImage(this, binding.funAddFriendVerifyActivityHeadIv, userBean.userAvatar);
            binding.funAddFriendVerifyActivityNav.getTitleView().setText("入群申请");
            binding.funAddFriendVerifyActivitySendRl.setVisibility(View.GONE);
            binding.funAddFriendVerifyActivityTwoOptLl.setVisibility(View.VISIBLE);
            binding.funAddFriendVerifyActivityEt.setEnabled(false);
            binding.funAddFriendVerifyActivityEt.setText("暂无留言");
            binding.funAddFriendVerifyActivityEtTitleTv.setText("对方留言");

            binding.funAddFriendVerifyActivityAggreRl.setOnClickListener(this);
            binding.funAddFriendVerifyActivityRefuseRl.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.funAddFriendVerifyActivityNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.funAddFriendVerifyActivitySendRl) {
            RegisterBean bean = new RegisterBean();
            bean.memberCode = userBean.memberCode;
            bean.msg = getTextStr(binding.funAddFriendVerifyActivityEt);
            HttpUtil.apiW().friends_addFriends(bean)
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
        } else if (v == binding.funAddFriendVerifyActivityRefuseRl) {
            optAddFriend(false);
        } else if (v == binding.funAddFriendVerifyActivityAggreRl) {

            optAddFriend(true);
        }
    }

    private void optAddFriend(boolean isAgree) {
        if (userBean.page_type == 101) {

            //1 同意,2 拒绝
            RegisterBean bean = new RegisterBean();
            bean.id = userBean.id;
            bean.state = isAgree ? 1 : 2;
            HttpUtil.apiW().group_groupConsentOrRefuse(bean)
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
        } else {

            //1 同意,2 拒绝
            RegisterBean bean = new RegisterBean();
            bean.id = userBean.id;
            bean.type = isAgree ? 1 : 2;
            HttpUtil.apiW().friends_appFriendApplyEd(bean)
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
