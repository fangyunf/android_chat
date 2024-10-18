package com.turunsi.yaoxin.eggs;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.turunsi.yaoxin.databinding.ActivityMineAddressListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineGroupListBinding;
import com.turunsi.yaoxin.main.mine.address.AddressAddActivity;
import com.turunsi.yaoxin.main.mine.address.adapter.AddressListAdapter;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GroupListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineGroupListBinding binding;
    GroupListAdapter adapter;
    List<GroupInfoBean> dataList;
    String caiDanId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineGroupListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (extras.get("id") != null) {
            caiDanId = (String) extras.get("id");
        }
        binding.activityMineGroupListNav.addCloseImageButton().setOnClickListener(this);

        binding.activityMineGroupListRv.setLayoutManager(new LinearLayoutManager(this));
         adapter = new GroupListAdapter();
        binding.activityMineGroupListRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
//                for (GroupInfoBean bean : dataList) {
//                    bean.isSelected = false;
//                }
//                dataList.get(i).isSelected = true;
//                adapter.setItems(dataList);
//                adapter.notifyDataSetChanged();

                EggSuccessDialogFragment.showTitle(getSupportFragmentManager(), new EggSuccessDialogFragment.EggSuccessDialogFragmentBlock() {
                    @Override
                    public void upGrade() {

                        RegisterBean registerBean = new RegisterBean();
                        registerBean.caiDanId = caiDanId;
                        registerBean.groupId = baseQuickAdapter.getItem(i).groupId;
                        HttpUtil.apiW().caidan_sjCaiDan(registerBean)
                                .enqueue(new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                        ToastUtils.toastMsg("发放成功");
                                        finish();
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {

                                    }
                                });
                    }
                },"发放彩蛋至 " + baseQuickAdapter.getItem(i).name);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new BaseEvent("reload_my_egg_list"));

    }

    @Override
    protected void _requestData() {
        super._requestData();
        HttpUtil.api8446().group_userGroups()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();

                         dataList = new Gson().fromJson(body.data.toString(),type);

                        adapter.setItems(dataList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineGroupListNav.addCloseImageButton()) {
            finish();
        }
    }

}
