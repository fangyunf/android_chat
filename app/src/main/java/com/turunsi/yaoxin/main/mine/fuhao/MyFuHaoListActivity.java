package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.SubUserBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MyFuHaoListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineMyFuhaoListBinding binding;

    MyFuHaoListAdapter adapter = new MyFuHaoListAdapter();

    ArrayList<SubUserBean> subUserList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineMyFuhaoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityMineMyFuhaoListNav);
        binding.activityMineMyFuhaoListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineMyFuhaoListBuyTv.setOnClickListener(this);
        binding.activityMineMyFuhaoListSelfPhoneTv.setText(DataUtil.getUserInfo().phoneNo);
        // 改为垂直列表布局，匹配图1设计
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.activityMineMyFuhaoListRv.setLayoutManager(linearLayoutManager);
        binding.activityMineMyFuhaoListRv.setAdapter(adapter);

//        for (int i = 0; i < 20; i++) {
//            UserBean bean = new UserBean();
//            bean.phone = "1234444444";
//            userBeanList.add(bean);
//        }

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<SubUserBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<SubUserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                // 点击复制按钮的逻辑在适配器中处理
            }
        });
        EventBus.getDefault().register(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
        if ("reload_fuhao".equals(event.getTag())) {
            _requestData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    
    @Override
    protected void _requestData() {
        RegisterBean registerBean = new RegisterBean();
        registerBean.userId = DataUtil.getUserInfo().userId;
        HttpUtil.apiW().subUser_queryList(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<SubUserBean>>() {
                        }.getType();
                        subUserList = new Gson().fromJson(body.data.toString(), type);
                        adapter.setItems(subUserList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineMyFuhaoListNav.addCloseImageButton()) {
//            HashMap map = new HashMap();
//            map.put("type","0");
//            BuyFeatureActivity.start(BuyFeatureActivity.class,this,map);
            finish();
        } else if (v == binding.activityMineMyFuhaoListBuyTv) {
            HashMap map = new HashMap();
            map.put("type", "1");
            BuyFeatureActivity.start(BuyFeatureActivity.class, this, map);
        }
    }

}
