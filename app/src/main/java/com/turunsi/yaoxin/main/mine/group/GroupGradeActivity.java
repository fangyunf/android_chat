package com.turunsi.yaoxin.main.mine.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.databinding.ActivityGroupGradeBinding;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailList_DetailActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.GridSpacingItemDecoration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.view.LoadingDialog;

/**
 * 群等级购买页面
 */
public class GroupGradeActivity extends BaseActivity {

    private ActivityGroupGradeBinding binding;
    private GroupGradeAdapter adapter;
    private List<GroupInfoBean> gradeList = new ArrayList<>();
    private String groupId;
    private GroupInfoBean groupInfo;
    private ActivityResultLauncher<Intent> selectGroupLauncher;

    public static void start(Class<? extends Activity> clazz, Context context) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    public static void startWithGroup(Class<? extends Activity> clazz, Context context, String groupId) {
        Intent intent = new Intent(context, clazz);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupGradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 注册选择群的结果回调
        selectGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        groupId = result.getData().getStringExtra("groupId");
                        for (GroupInfoBean item : adapter.getItems()) {
                            if (item.isSelected) {
                                buyGroupGrade(item, groupId);
                                return;
                            }
                        }
                    }
                });
        // 有 groupId，直接初始化
        initView();
        getUserInfo();
        loadData();
    }


    private void initView() {
        binding.activityBuyFeatureNav.addCloseImageButton().setOnClickListener(view -> finish());
        StatusBarUtils.transtStatusBar(this, binding.activityBuyFeatureNav);
        // 初始化 RecyclerView - 使用2列网格布局
        binding.activityGroupGradeRv.setLayoutManager(new GridLayoutManager(this, 2));
        // 列间距12dp，行间距12dp，不包含边缘
        binding.activityGroupGradeRv.addItemDecoration(new GridSpacingItemDecoration(2, SizeUtils.dp2px(10), SizeUtils.dp2px(0), false));
        adapter = new GroupGradeAdapter();
        binding.activityGroupGradeRv.setAdapter(adapter);
        // 点击购买
        adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            for (GroupInfoBean item : baseQuickAdapter.getItems()) {
                item.isSelected = false;
            }
            GroupInfoBean item = baseQuickAdapter.getItem(i);
            item.isSelected = true;
            adapter.notifyDataSetChanged();
        });

        // 购买记录
        binding.activityGroupGradePurchaseRecordTv.setOnClickListener(v -> {
            BillDetailList_DetailActivity.start(BillDetailList_DetailActivity.class, GroupGradeActivity.this, null);
        });

        // 立即开通
        binding.activityGroupGradeBuyBtn.setOnClickListener(v -> {
            for (GroupInfoBean item : adapter.getItems()) {
                if (item.isSelected) {
                    // 没有 groupId，跳转到选择群页面
                    Intent intent = new Intent(this, SelectGroupActivity.class);
                    selectGroupLauncher.launch(intent);
                    return;
                }
            }
        });
    }

    private void getUserInfo() {
        HttpUtil.apiW().home_getUserByToken(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                        if (userBean != null) {
                            GlideUtil.yh_loadImageRoundedCorner(GroupGradeActivity.this, binding.activityGroupGradeAvatarIv, userBean.avatar, 10);
                            binding.activityGroupGradeNameTv.setText(userBean.username);
                            binding.activityGroupGradeIdTv.setText("ID:" + userBean.memberCode);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }

    private void loadData() {
        // 加载群等级列表
        HttpUtil.apiW().group_groupGrade().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                gradeList = new Gson().fromJson(body.data.toString(), type);
                if (gradeList != null && !gradeList.isEmpty()) {
                    // 过滤掉 grade 为 1 的数据
                    List<GroupInfoBean> filteredList = new ArrayList<>();
                    for (GroupInfoBean item : gradeList) {
                        if (item.grade != 1) {
                            filteredList.add(item);
                        }
                    }
                    adapter.setItems(filteredList);
                    // 默认选中第一个
                    if (!filteredList.isEmpty()) {
                        filteredList.get(0).isSelected = true;
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                ToastUtils.toastMsg("加载失败");
            }
        });
    }

    /**
     * 购买群等级
     */
    private void buyGroupGrade(GroupInfoBean gradeInfo, String _groupId) {
        PopEnterPassword popEnterPassword = new PopEnterPassword(this, password -> {
            RegisterBean registerBean = new RegisterBean();
            registerBean.grade = gradeInfo.grade + "";
            registerBean.groupId = _groupId;
            registerBean.password = password;
            LoadingDialog.showDialog(getSupportFragmentManager(), "请稍等...");
            HttpUtil.apiW().group_buyGroupGrade(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg("购买成功");
                            LoadingDialog.dismissDialog();
                            finish();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {
                            LoadingDialog.dismissDialog();
                        }
                    });
        }, NumberUtil.formartMoney(gradeInfo.price + ""));
        // 显示窗口
        popEnterPassword.showAtLocation(binding.activityGroupGradeBuyBtn,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
    }
}

