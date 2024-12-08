package com.turunsi.yaoxin.main.mine.collection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.CollectInfoPage;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineCollectionListBinding;
import com.turunsi.yaoxin.main.mine.collection.adapter.CollectionListAdapter;
import com.turunsi.yaoxin.main.mine.collection.bean.CollectionListBean;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;

public class CollectionListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineCollectionListBinding binding;

    CollectionListAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineCollectionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineCollectionListNav.addCloseImageButton().setOnClickListener(this);

        binding.activityMineCollectionListRv.setLayoutManager(new LinearLayoutManager(this));
         adapter = new CollectionListAdapter();
        binding.activityMineCollectionListRv.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<CollectInfo>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter, @NonNull View view, int i) {
                CollectInfo item = adapter.getItem(i);
                String content = item.getData();
                try {
                    content = AESUtil.msgAseDecrypt(content);
                }catch (Exception e) {

                }
                Intent result = new Intent();
                result.putExtra("text", content);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener<CollectInfo>() {
            @Override
            public boolean onLongClick(@NonNull BaseQuickAdapter<CollectInfo, ?> baseQuickAdapter, @NonNull View view, int i) {
                DialogAlertUtil.showAlert("确定删除？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type == 1) {
                            ArrayList list = new ArrayList<>();
                            CollectInfo item = baseQuickAdapter.getItem(i);
                            Pair pair = new Pair<>(item.getId(),item.getCreateTime());
                            list.add(pair);
                            NIMClient.getService(MsgService.class).removeCollect(list).setCallback(
                                    new RequestCallback<Integer>() {
                                        @Override
                                        public void onSuccess(Integer param) {
//                                            Toast.makeText(SessionExtension.this, "批量移除收藏成功", Toast.LENGTH_SHORT).show();
                                            ToastUtils.toastMsg("删除成功");
                                            _requestData();
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                            ToastUtils.toastMsg("删除失败");
                                        }

                                        @Override
                                        public void onException(Throwable exception) {
                                        }
                                    }
                            );

                        }
                    }
                }, getSupportFragmentManager());

                return false;
            }
        });
    }

    @Override
    protected void _requestData() {
        super._requestData();
        dataList.clear();
        NIMClient.getService(MsgService.class).queryCollect(100).setCallback(
                new RequestCallback<CollectInfoPage>() {
                    @Override
                    public void onSuccess(CollectInfoPage param) {
                        if (param == null) {
                            return;
                        }
                        ArrayList<CollectInfo> collectList = param.getCollectList();
                        for (CollectInfo tempInfo :
                                collectList) {

                            int type = tempInfo.getType();
                            long id = tempInfo.getId();
                            String data = tempInfo.getData();
                            String ext = tempInfo.getExt();
                            if (tempInfo.getType() == 1024) {
                                dataList.add(tempInfo);
                            }
                        }
                        adapter.setItems(dataList);
                        adapter.notifyDataSetChanged();
//                        addCollects(param.getCollectList());
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                }

        );
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineCollectionListNav.addCloseImageButton()) {
            finish();
        }
    }

}
