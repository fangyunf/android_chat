package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivitySystemNoticeNew1Binding;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivitySystemNoticeNew1QbBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter.Fun_Notice_QB_ListAdapter;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter.XiaoZhuShouListAdapter;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunSystem_Notice_New_QianBao_Activity extends BaseActivity implements View.OnClickListener {

    ActivitySystemNoticeNew1QbBinding binding;
    Fun_Notice_QB_ListAdapter adapter = new Fun_Notice_QB_ListAdapter();
    XiaoZhuShouListAdapter xzsadapter = new XiaoZhuShouListAdapter();
    private SmartRefreshLayout refreshLayout;
    int pageIndex = 1;

    int dataType = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySystemNoticeNew1QbBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activitySystemNoticeNew1QbNav.addCloseImageButton().setOnClickListener(this);
        binding.activitySystemNoticeNew1QbSwithRl1.setOnClickListener(this);
        binding.activitySystemNoticeNew1QbSwithRl2.setOnClickListener(this);
        binding.activitySystemNoticeNew1QbSwithRl3.setOnClickListener(this);
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

    private void _resetState(int index) {
        if (index == dataType) {
            return;
        }
        binding.activitySystemNoticeNew1QbSwithRl1Iv.setVisibility(View.GONE);
        binding.activitySystemNoticeNew1QbSwithRl2Iv.setVisibility(View.GONE);
        binding.activitySystemNoticeNew1QbSwithRl3Iv.setVisibility(View.GONE);

        switch (index) {
            case 0:
                binding.activitySystemNoticeNew1QbSwithRl1Iv.setVisibility(View.VISIBLE);
                break;
            case 1:
                binding.activitySystemNoticeNew1QbSwithRl2Iv.setVisibility(View.VISIBLE);
                break;
            case 2:
                binding.activitySystemNoticeNew1QbSwithRl3Iv.setVisibility(View.VISIBLE);
                break;
        }
        dataType = index;
        pageIndex = 1;
        _requestData();

    }
    @Override
    protected void _requestData() {
        LoadingDialog.showDialog(getSupportFragmentManager(),"请求中");
        RegisterBean registerBean = new RegisterBean();
        registerBean.userId = DataUtil.getUserid();
        registerBean.pageNo = pageIndex + "";
        registerBean.pageSize = 20;
        if (dataType == 0) {
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
                            adapter.type = 0;
                            binding.activitySystemNoticeNew1QbRv.setAdapter(adapter);
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
        }else if (dataType == 1) {
            HttpUtil.apiW().send_queryPayWith(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            GroupInfoBean applyNumBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                            if (pageIndex == 1) {
                                adapter.setItems(applyNumBean.data);
                            } else {
                                adapter.addAll(applyNumBean.data);
                            }
                            adapter.type = 1;
                            binding.activitySystemNoticeNew1QbRv.setAdapter(adapter);
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
        } else if (dataType == 2) {
            RegisterBean registerBean1 = new RegisterBean();
            registerBean1.pageNo = "0";

            HttpUtil.apiW().aideNews_aideMsg(registerBean1)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            GroupInfoBean tempBean = new Gson().fromJson(body.data.toString(),GroupInfoBean.class);
                            List<GroupInfoBean> filteredList = new ArrayList<>();
                            for (GroupInfoBean bean : tempBean.data) {
                                if (bean.state != 102 && bean.state != 103 && bean.state != 105) {
                                    filteredList.add(bean);
                                }
                            }

                            xzsadapter.setItems(filteredList);
                            binding.activitySystemNoticeNew1QbRv.setAdapter(xzsadapter);
                            xzsadapter.notifyDataSetChanged();
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
        } else if (view == binding.activitySystemNoticeNew1QbSwithRl1) {
            _resetState(0);
        } else if (view == binding.activitySystemNoticeNew1QbSwithRl2) {
            _resetState(1);
        } else if (view == binding.activitySystemNoticeNew1QbSwithRl3) {
            _resetState(2);
        }
    }
}
