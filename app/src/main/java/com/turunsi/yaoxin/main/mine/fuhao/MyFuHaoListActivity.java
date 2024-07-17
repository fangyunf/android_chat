package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.CollectInfoPage;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.databinding.ActivityMineCollectionListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineMyFuhaoListBinding;
import com.turunsi.yaoxin.main.mine.collection.adapter.CollectionListAdapter;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.MyFuHaoListAdapter;
import com.turunsi.yaoxin.main.mine.setting.adapter.ExchangeAccountAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyFuHaoListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineMyFuhaoListBinding binding;

    MyFuHaoListAdapter adapter = new MyFuHaoListAdapter();

    ArrayList<UserBean> userBeanList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineMyFuhaoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineMyFuhaoListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineMyFuhaoListBuyIv.setOnClickListener(this);
        binding.activityMineMyFuhaoListSelfPhoneTv.setText(DataUtil.getUserInfo().phoneNo);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        binding.activityMineMyFuhaoListRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(3, SizeUtils.dp2px(10), false);
        binding.activityMineMyFuhaoListRv.addItemDecoration(gridSpacingItemDecoration);
        binding.activityMineMyFuhaoListRv.setAdapter(adapter);

        for (int i = 0; i < 20; i++) {
            UserBean bean = new UserBean();
            bean.phone = "1234444444";
            userBeanList.add(bean);
        }

        adapter.setItems(userBeanList);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<UserBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
            }
        });
    }

    @Override
    protected void _requestData() {

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineMyFuhaoListNav.addCloseImageButton()) {
            HashMap map = new HashMap();
            map.put("type","0");
            BuyFeatureActivity.start(BuyFeatureActivity.class,this,map);
            finish();
        } else if (v == binding.activityMineMyFuhaoListBuyIv) {
            HashMap map = new HashMap();
            map.put("type","1");
            BuyFeatureActivity.start(BuyFeatureActivity.class,this,map);
        }
    }

}
