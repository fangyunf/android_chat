package com.turunsi.yaoxin.main.mine.order;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineOrderListBinding;
import com.turunsi.yaoxin.main.mine.order.adapter.OrderListAdapter;
import com.turunsi.yaoxin.main.mine.order.bean.OrderListBean;
import com.turunsi.yaoxin.main.mine.order.entity.TabEntity;

import java.util.ArrayList;

public class OrderListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineOrderListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineOrderListNav.addCloseImageButton().setOnClickListener(this);

        CommonTabLayout layout = binding.activityMineOrderListTabLayout;
        String[] mTitles = {"全部", "待付款", "待发货", "待收货", "待评价"};
        ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
        for (String mTitle : mTitles) {
            mTabEntities.add(new TabEntity(mTitle));
        }
        layout.setTabData(mTabEntities);

        binding.activityMineOrderListRv.setLayoutManager(new LinearLayoutManager(this));
        OrderListAdapter adapter = new OrderListAdapter();
        binding.activityMineOrderListRv.setAdapter(adapter);

        for (int i = 0; i < 10; i++) {
            OrderListBean bean = new OrderListBean();
            dataList.add(bean);
        }
        adapter.setItems(dataList);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineOrderListNav.addCloseImageButton()) {
            finish();
        }
    }

}
