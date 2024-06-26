package com.turunsi.yaoxin.main.mine.purse.bankcard;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;

public class BankCardListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBankCardListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBankCardListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseBankCardListNav.addCloseImageButton().setOnClickListener(this);


        binding.activityMinePurseBankCardListRv.setLayoutManager(new LinearLayoutManager(this));
        BankCardListAdapter adapter = new BankCardListAdapter();
        binding.activityMinePurseBankCardListRv.setAdapter(adapter);

        for (int i = 0; i < 1; i++) {
            BankCardListBean bean = new BankCardListBean();
            dataList.add(bean);
        }
        adapter.setItems(dataList);
        Context that = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<BankCardListBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<BankCardListBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                PurseBankListAddActivity.start(PurseBankListAddActivity.class,that,null);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseBankCardListNav.addCloseImageButton()) {
            finish();
        }
    }

}
