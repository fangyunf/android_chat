package com.turunsi.yaoxin.main.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineAddressListBinding;
import com.turunsi.yaoxin.main.mine.address.adapter.AddressListAdapter;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 商城地址选择页面（从商品详情页跳转）
 */
public class ShopAddressListActivity extends BaseActivity implements View.OnClickListener {
    
    private ActivityMineAddressListBinding binding;
    private AddressListAdapter adapter;
    private List<AddressListBean> dataList = new ArrayList<>();
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineAddressListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.activityMineAddressListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineAddressListAddRl.setOnClickListener(this);
        
        binding.activityMineAddressListRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddressListAdapter();
        binding.activityMineAddressListRv.setAdapter(adapter);
        
        // TODO: 从API加载地址列表
        loadAddressList();
        
        // 设置点击监听
        adapter.setOnItemClickListener((adapter, view, position) -> {
            AddressListBean address = dataList.get(position);
            // 返回选中的地址
            Intent resultIntent = new Intent();
            resultIntent.putExtra("address", address);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
    
    private void loadAddressList() {
        // TODO: 从API或本地数据库加载地址列表
        // 这里先用假数据
        for (int i = 0; i < 5; i++) {
            AddressListBean bean = new AddressListBean();
            bean.id = "address_" + i;
            dataList.add(bean);
        }
        adapter.setItems(dataList);
    }
    
    @Override
    public void onClick(View v) {
        if (v == binding.activityMineAddressListNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineAddressListAddRl) {
            // TODO: 跳转到添加地址页面
            // Intent intent = new Intent(this, AddressAddActivity.class);
            // startActivity(intent);
        }
    }
}

