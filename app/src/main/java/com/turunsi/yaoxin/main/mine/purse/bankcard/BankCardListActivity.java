package com.turunsi.yaoxin.main.mine.purse.bankcard;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BankCardListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBankCardListBinding binding;
    BankCardListAdapter adapter = new BankCardListAdapter();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBankCardListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseBankCardListNav.addCloseImageButton().setOnClickListener(this);

        binding.activityMineBankCardListAddIv.setOnClickListener(this);
        binding.activityMinePurseBankCardListRv.setLayoutManager(new LinearLayoutManager(this));

        binding.activityMinePurseBankCardListRv.setAdapter(adapter);

        transtStatusBar(binding.activityMinePurseBankCardListNav);

        Context that = this;
        adapter.addOnItemChildClickListener(R.id.item_purse_bank_card_list_cell_unbind_tv, new BaseQuickAdapter.OnItemChildClickListener<BankCardListBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<BankCardListBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                RegisterBean bean = new RegisterBean();
                bean.id = Integer.parseInt(baseQuickAdapter.getItem(i).id);
                HttpUtil.apiW().bindCard_deleteZFB(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                ToastUtils.toastMsg("解绑成功");
                                _requestData();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        });
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("refresh_bank_list")) {
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
        RegisterBean bean = new RegisterBean();
        bean.type = 3;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<BankCardListBean>>() {}.getType();
                        List<BankCardListBean> bindList = new Gson().fromJson(body.data.toString(), type);
                        adapter.setItems(bindList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseBankCardListNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBankCardListAddIv) {
            PurseBankListAddActivity.start(PurseBankListAddActivity.class,this,null);
        }
    }

}
