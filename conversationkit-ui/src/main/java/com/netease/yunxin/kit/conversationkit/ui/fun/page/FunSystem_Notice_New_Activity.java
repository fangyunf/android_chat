package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivityFunSelectedUserBinding;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivitySystemNoticeNew1Binding;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter.Fun_Selected_UserListAdapter;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.TeamIconUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunSystem_Notice_New_Activity extends BaseActivity implements View.OnClickListener {

    ActivitySystemNoticeNew1Binding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySystemNoticeNew1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activitySystemNoticeNewNav);
        binding.activitySystemNoticeNewNav.addCloseImageButton().setOnClickListener(this);
        binding.activitySystemNoticeNewXttzLl.setOnClickListener(this);
        binding.activitySystemNoticeNewQtzLl.setOnClickListener(this);
        binding.activitySystemNoticeNewQbxxLl.setOnClickListener(this);
        HttpUtil.apiW().friends_applyListNum(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        GroupInfoBean applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        if (applyNumBean.groupApplyNum > 0) {
                            binding.activitySystemNoticeNewQtzNumTv.setText(applyNumBean.groupApplyNum + "");
                            binding.activitySystemNoticeNewQtzNumTv.setVisibility(View.VISIBLE);
                        } else {
                            binding.activitySystemNoticeNewQtzNumTv.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void _initView() {
    }


    @Override
    public void onClick(View view) {
        if (view == binding.activitySystemNoticeNewNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.activitySystemNoticeNewXttzLl) {
            XKitRouter.withKey("SystemNotice_NewActivity")
                    .withContext(this)
                    .navigate();
        } else if (view == binding.activitySystemNoticeNewQtzLl) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE)
                    .withParam("type", "1")
                    .withContext(this)
                    .navigate();
        } else if (view == binding.activitySystemNoticeNewQbxxLl) {
            XKitRouter.withKey(Constant.XiaoZhuShouActivityKey)
                    .withContext(this)
                    .navigate();
        }
    }
}
