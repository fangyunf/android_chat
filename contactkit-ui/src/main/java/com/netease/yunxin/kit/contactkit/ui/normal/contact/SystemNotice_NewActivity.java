package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.contactkit.ui.databinding.ActivitySystemNoticeNewBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.System_noticeAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SystemNotice_NewActivity extends BaseActivity implements View.OnClickListener {
    ActivitySystemNoticeNewBinding binding;
    System_noticeAdapter adapter = new System_noticeAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySystemNoticeNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activitySystemNoticeNewNav.addCloseImageButton().setOnClickListener(this);
        StatusBarUtils.transtStatusBar(this, binding.activitySystemNoticeNewNav);
        binding.activitySystemNoticeNewRv1.setLayoutManager(new LinearLayoutManager(this));
        binding.activitySystemNoticeNewRv1.setAdapter(adapter);

    }

    @Override
    protected void _requestData() {
        super._requestData();
        HttpUtil.apiW().customer_noticeList()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activitySystemNoticeNewNav.addCloseImageButton()) {
            finish();
        }
    }

}
