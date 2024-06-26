package com.turunsi.yaoxin.main.mine.address;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineAddressListBinding;
import com.turunsi.yaoxin.main.mine.address.adapter.AddressListAdapter;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;

public class AddressListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineAddressListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineAddressListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineAddressListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineAddressListAddRl.setOnClickListener(this);

        binding.activityMineAddressListRv.setLayoutManager(new LinearLayoutManager(this));
        AddressListAdapter adapter = new AddressListAdapter();
        binding.activityMineAddressListRv.setAdapter(adapter);

        for (int i = 0; i < 10; i++) {
            AddressListBean bean = new AddressListBean();
            dataList.add(bean);
        }
        adapter.setItems(dataList);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineAddressListNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineAddressListAddRl) {
            AddressAddActivity.start(AddressAddActivity.class, this, null);
        }
    }

}
