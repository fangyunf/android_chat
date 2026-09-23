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
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityOfficialDynamicsBinding;
import com.turunsi.yaoxin.main.mine.dynamics.adapter.OfficialDynamicAdapter;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 朋友圈 Tab：沿用官方动态列表界面
 */
public class OfficialDynamicsFragment extends BaseFragment {

    private ActivityOfficialDynamicsBinding binding;
    private OfficialDynamicAdapter adapter;
    private View headerView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = ActivityOfficialDynamicsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) {
            StatusBarUtils.setStatusBarLightMode(getActivity(), false, true);
            StatusBarUtils.transtStatusBar(getActivity(), binding.activityOfficialDynamicsNav);
        }
        binding.activityOfficialDynamicsNav.setTitle("朋友圈");
        initView();
        getUserInfo();
        loadData();
    }

    private void getUserInfo() {
        HttpUtil.apiW()
                .home_getUserByToken(new RegisterBean())
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null) {
                                    return;
                                }
                                UserBean userBean =
                                        new Gson().fromJson(body.data.toString(), UserBean.class);
                                if (userBean != null) {
                                    DataUtil.putUserInfo(userBean);
                                    if (!TextUtils.isEmpty(userBean.token)) {
                                        DataUtil.putToken(userBean.token);
                                    }
                                    updateHeaderView(userBean);
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }

    private void updateHeaderView(UserBean userBean) {
        if (headerView == null || userBean == null || getContext() == null) {
            return;
        }
        com.makeramen.roundedimageview.RoundedImageView avatarIv =
                headerView.findViewById(R.id.header_official_dynamics_avatar_iv);
        TextView userNameTv = headerView.findViewById(R.id.header_official_dynamics_user_name_tv);
        if (avatarIv != null) {
            GlideUtil.yh_loadImageRoundedCorner(getContext(), avatarIv, userBean.avatar, 10);
        }
        if (userNameTv != null) {
            String name =
                    !TextUtils.isEmpty(userBean.username)
                            ? userBean.username
                            : (!TextUtils.isEmpty(userBean.name) ? userBean.name : "");
            userNameTv.setText(name);
        }
    }

    private void initView() {
        binding.activityOfficialDynamicsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OfficialDynamicAdapter();

        RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter =
                new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        headerView =
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.header_official_dynamics, parent, false);
                        UserBean cached = DataUtil.getUserInfo();
                        if (cached != null) {
                            updateHeaderView(cached);
                        }
                        return new RecyclerView.ViewHolder(headerView) {};
                    }

                    @Override
                    public void onBindViewHolder(
                            @NonNull RecyclerView.ViewHolder holder, int position) {}

                    @Override
                    public int getItemCount() {
                        return 1;
                    }
                };

        ConcatAdapter concatAdapter = new ConcatAdapter(headerAdapter, adapter);
        binding.activityOfficialDynamicsRv.setAdapter(concatAdapter);
    }

    private void loadData() {
        HttpUtil.apiW()
                .customer_noticeList()
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null || adapter == null) {
                                    return;
                                }
                                Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                                List<GroupInfoBean> tempList =
                                        new Gson().fromJson(body.data.toString(), type);
                                adapter.setItems(tempList);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }
}
