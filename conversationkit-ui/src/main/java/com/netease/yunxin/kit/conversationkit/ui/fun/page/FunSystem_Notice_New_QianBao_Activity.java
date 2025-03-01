package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivitySystemNoticeNew1Binding;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivitySystemNoticeNew1QbBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter.Fun_Notice_QB_ListAdapter;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.view.LoadingDialog;

import retrofit2.Call;
import retrofit2.Response;

public class FunSystem_Notice_New_QianBao_Activity extends BaseActivity implements View.OnClickListener {

    ActivitySystemNoticeNew1QbBinding binding;
    Fun_Notice_QB_ListAdapter adapter = new Fun_Notice_QB_ListAdapter();;
    private SmartRefreshLayout refreshLayout;
    int pageIndex = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySystemNoticeNew1QbBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activitySystemNoticeNew1QbNav.addCloseImageButton().setOnClickListener(this);
        binding.activitySystemNoticeNew1QbRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activitySystemNoticeNew1QbRv.setAdapter(adapter);

        refreshLayout = binding.activitySystemNoticeNew1QbSml;
        // 设置下拉刷新监听器
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                pageIndex = 1;
                _requestData();
            }
        });

        // 设置上拉加载监听器
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                pageIndex ++;
                _requestData();
            }
        });
        _requestData();
    }

    @Override
    protected void _requestData() {
        LoadingDialog.showDialog(getSupportFragmentManager(),"请求中");
        RegisterBean registerBean = new RegisterBean();
        registerBean.userId = DataUtil.getUserid();
        registerBean.pageNo = pageIndex + "";
        HttpUtil.apiW().send_queryCharge(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        GroupInfoBean applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        if (pageIndex == 1) {
                            adapter.setItems(applyNumBean.data);
                        } else {
                            adapter.addAll(applyNumBean.data);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                        LoadingDialog.dismissDialog();
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();

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
        if (view == binding.activitySystemNoticeNew1QbNav.addCloseImageButton()){
            finish();
        }
    }
}
