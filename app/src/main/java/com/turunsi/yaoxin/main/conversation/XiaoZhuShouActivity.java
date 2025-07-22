package com.turunsi.yaoxin.main.conversation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.databinding.ActivityLoginBinding;
import com.turunsi.yaoxin.databinding.ActivityXiaozhushouBinding;
import com.turunsi.yaoxin.fragment.OtherPlaceLoginFragment;
import com.turunsi.yaoxin.main.conversation.adapter.XiaoZhuShouListAdapter;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class XiaoZhuShouActivity extends BaseActivity implements View.OnClickListener {
    ActivityXiaozhushouBinding binding;

    XiaoZhuShouListAdapter adapter = new XiaoZhuShouListAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXiaozhushouBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        transtStatusBar(binding.activityXiaozhushouNav);
        binding.activityXiaozhushouNav.addCloseImageButton().setOnClickListener(this);
        binding.activityXiaozhushouRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityXiaozhushouRv.setAdapter(adapter);
    }

    @Override
    protected void _requestData() {
        RegisterBean registerBean = new RegisterBean();
        registerBean.pageNo = "0";

        HttpUtil.apiW().aideNews_aideMsg(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        GroupInfoBean tempBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        adapter.setItems(tempBean.data);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityXiaozhushouNav.addCloseImageButton()) {
            finish();
        }
    }
}
