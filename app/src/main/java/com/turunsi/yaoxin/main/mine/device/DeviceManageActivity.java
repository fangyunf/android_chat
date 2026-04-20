package com.turunsi.yaoxin.main.mine.device;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.turunsi.yaoxin.databinding.ActivityDeviceManageBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.DeviceInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DeviceManageActivity extends BaseActivity implements View.OnClickListener {

    ActivityDeviceManageBinding binding;
    private final DeviceManageAdapter adapter = new DeviceManageAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityDeviceNav.addCloseImageButton().setOnClickListener(this);
        binding.activityListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityListRv.setAdapter(adapter);

        adapter.setOnDeleteClickListener((deviceInfo, position) -> {
            DialogAlertUtil.showAlert("确定删除该数据？", type -> {
                if (type == 1) {
                    deleteData(deviceInfo);
                }
            }, getSupportFragmentManager());
        });
    }

    private void deleteData(DeviceInfoBean deviceInfoBean) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.id = (int) deviceInfoBean.id;
        HttpUtil.apiW().home_deleteDevice(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        adapter.remove(deviceInfoBean);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }


    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_deviceLog(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<DeviceInfoBean>>() {
                        }.getType();
                        List<DeviceInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityDeviceNav.addCloseImageButton()) {
            finish();
        }
    }
}
