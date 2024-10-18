package com.netease.yunxin.kit.contactkit.ui.normal.groupList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.contactkit.ui.databinding.ActivityMyGroupListNewBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.GroupListAdapter;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MyGroupListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMyGroupListNewBinding binding;
    GroupListAdapter adapter;
    List<GroupInfoBean> dataList;
    String caiDanId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGroupListNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMyGroupListNewNav.addCloseImageButton().setOnClickListener(this);

        binding.activityMyGroupListNewRv.setLayoutManager(new LinearLayoutManager(this));
         adapter = new GroupListAdapter();
        binding.activityMyGroupListNewRv.setAdapter(adapter);
        Activity activity = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                        .withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i).groupId)
                        .withContext(activity)
                        .navigate();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void _requestData() {
        super._requestData();
        HttpUtil.api8446().group_userGroups()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();

                         dataList = new Gson().fromJson(body.data.toString(),type);

                        adapter.setItems(dataList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMyGroupListNewNav.addCloseImageButton()) {
            finish();
        }
    }

}
