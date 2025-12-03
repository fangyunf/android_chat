package com.turunsi.yaoxin.main.mine.dynamics;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.turunsi.yaoxin.databinding.ActivityOfficialDynamicsBinding;
import com.turunsi.yaoxin.main.mine.dynamics.adapter.OfficialDynamicAdapter;
import com.turunsi.yaoxin.main.mine.dynamics.model.DynamicBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class OfficialDynamicsActivity extends BaseActivity implements View.OnClickListener {
    ActivityOfficialDynamicsBinding binding;
    OfficialDynamicAdapter adapter;
    private View headerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfficialDynamicsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityOfficialDynamicsNav.addCloseImageButton().setOnClickListener(this);
        StatusBarUtils.transtStatusBar(this, binding.activityOfficialDynamicsNav);
        // 加载当前用户头像
        initView();
        getUserInfo();
        loadData();
    }

    private void getUserInfo() {
        HttpUtil.apiW().home_getUserByToken(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                        if (userBean != null) {
                            DataUtil.putUserInfo(userBean);
                            DataUtil.putToken(userBean.token);
                            updateHeaderView(userBean);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }
    
    private void updateHeaderView(UserBean userBean) {
        if (headerView != null) {
            com.makeramen.roundedimageview.RoundedImageView avatarIv = 
                    headerView.findViewById(com.turunsi.yaoxin.R.id.header_official_dynamics_avatar_iv);
            TextView userNameTv = headerView.findViewById(com.turunsi.yaoxin.R.id.header_official_dynamics_user_name_tv);
            
            if (avatarIv != null) {
                GlideUtil.yh_loadImageRoundedCorner(this, avatarIv, userBean.avatar, 10);
            }
            if (userNameTv != null) {
                userNameTv.setText(userBean.username);
            }
        }
    }

    private void initView() {
        binding.activityOfficialDynamicsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfficialDynamicAdapter();
        
        // 创建 header adapter
        headerAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                headerView = LayoutInflater.from(parent.getContext()).inflate(
                        com.turunsi.yaoxin.R.layout.header_official_dynamics,
                        parent,
                        false);
                return new RecyclerView.ViewHolder(headerView) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // Header 绑定逻辑已在 updateHeaderView 中处理
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        };
        
        // 使用 ConcatAdapter 合并 header 和主 adapter
        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, adapter);
        binding.activityOfficialDynamicsRv.setAdapter(concatAdapter);
    }

    private void loadData() {
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
        if (v == binding.activityOfficialDynamicsNav.addCloseImageButton()) {
            finish();
        }
    }
}

