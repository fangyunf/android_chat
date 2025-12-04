package com.turunsi.yaoxin.main.mine.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.databinding.ActivitySelectGroupBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 选择群聊页面
 */
public class SelectGroupActivity extends BaseActivity {

    private ActivitySelectGroupBinding binding;
    private SelectGroupAdapter adapter;
    private List<GroupInfoBean> groupList = new ArrayList<>();
    private List<GroupInfoBean> filteredList = new ArrayList<>();
    private String selectedGroupId;

    public static void start(Class<? extends Activity> clazz, Context context) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        loadGroupList();
    }

    private void initView() {
        binding.activitySelectGroupNav.addCloseImageButton().setOnClickListener(view -> finish());
        StatusBarUtils.transtStatusBar(this, binding.activitySelectGroupNav);

        // 初始化 RecyclerView
        binding.activitySelectGroupRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectGroupAdapter();
        binding.activitySelectGroupRv.setAdapter(adapter);

        // 点击选择群
        adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            // 取消所有选中
            for (GroupInfoBean item : baseQuickAdapter.getItems()) {
                item.isSelected = false;
            }
            // 选中当前项
            GroupInfoBean item = baseQuickAdapter.getItem(i);
            if (item != null) {
                item.isSelected = true;
                selectedGroupId = item.groupId;
                adapter.notifyDataSetChanged();
            }
        });

        // 搜索功能
        binding.activitySelectGroupSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroups(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 确定按钮
        binding.activitySelectGroupConfirmTv.setOnClickListener(v -> {
            if (selectedGroupId == null || selectedGroupId.isEmpty()) {
                ToastUtils.toastMsg("请选择群聊");
                return;
            }
            // 返回选中的群ID
            Intent intent = new Intent();
            intent.putExtra("groupId", selectedGroupId);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    /**
     * 加载群列表
     */
    private void loadGroupList() {
        RegisterBean bean = new RegisterBean();
        HttpUtil.apiW().group_userGroups(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                groupList = new Gson().fromJson(body.data.toString(), type);

                if (groupList != null && !groupList.isEmpty()) {
                    // 按首字母排序
                    Collections.sort(groupList, new Comparator<GroupInfoBean>() {
                        @Override
                        public int compare(GroupInfoBean o1, GroupInfoBean o2) {
                            return o1.getIndex().compareTo(o2.getIndex());
                        }
                    });
                    
                    filteredList.clear();
                    filteredList.addAll(groupList);
                    adapter.groups = filteredList; // 用于字母索引判断
                    adapter.setItems(filteredList);
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtils.toastMsg("暂无群聊");
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                ToastUtils.toastMsg("加载失败");
            }
        });
    }

    /**
     * 过滤群列表
     */
    private void filterGroups(String keyword) {
        filteredList.clear();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.addAll(groupList);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (GroupInfoBean group : groupList) {
                if (group.name != null && group.name.toLowerCase().contains(lowerKeyword)) {
                    filteredList.add(group);
                }
            }
        }
        
        adapter.groups = filteredList; // 更新字母索引数据
        adapter.setItems(filteredList);
        adapter.notifyDataSetChanged();
    }
}

