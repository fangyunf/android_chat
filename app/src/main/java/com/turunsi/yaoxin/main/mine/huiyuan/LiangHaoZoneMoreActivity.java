package com.turunsi.yaoxin.main.mine.huiyuan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityLianghaoZoneMoreBinding;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.LiangHaoNumberAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

public class LiangHaoZoneMoreActivity extends BaseActivity {
    ActivityLianghaoZoneMoreBinding binding;
    private HuiYuanBean group;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLianghaoZoneMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventBus.getDefault().register(this);
        group = (HuiYuanBean) getIntent().getSerializableExtra("huiyuan");
        binding.activityMineMyHuiyuanListNav.addCloseImageButton().setOnClickListener(view -> finish());
        //loadData();
        binding.tvMore.setOnClickListener(view -> loadData());
        initView();
    }

    private void initView() {
        RecyclerView rvNumbers = binding.rvNumbers;
        int memberLevel = Integer.parseInt(group.memberConfig.memberLevel);
        String imageName = "mine_grade_level_" + memberLevel;
        int resId = this.getResources().getIdentifier(imageName, "mipmap", this
                .getPackageName());
        // 如果找到了资源，则可以使用这个ID获取Drawable
        Drawable drawable = null;
        if (resId > 0) {
            drawable = ContextCompat.getDrawable(this, resId);
        }
        binding.ivGroupIcon.setImageDrawable(drawable);
        rvNumbers.setLayoutManager(new GridLayoutManager(this, 3));
        LiangHaoNumberAdapter numberAdapter = new LiangHaoNumberAdapter(memberLevel);
        rvNumbers.setAdapter(numberAdapter);
        numberAdapter.setItems(group.memberCode);
        numberAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
//            Intent intent = new Intent(LiangHaoZoneMoreActivity.this, LiangHaoBuyActivity.class);
//            intent.putExtra("huiyuan", group);
//            intent.putExtra("index", i);
//            startActivity(intent);
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    RegisterBean registerBean = new RegisterBean();
                    registerBean.memberCode = group.memberCode.get(i) + "";
                    registerBean.password = password;
                    LoadingDialog.showDialog(getSupportFragmentManager(), "购买中..");
                    HttpUtil.apiW().meteor_buyMember(registerBean).enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg("购买成功");
                            EventBus.getDefault().post(new BaseEvent("reload_fuhao"));
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {
                        }

                        @Override
                        public void end() {
                            super.end();
                            LoadingDialog.dismissDialog();
                        }
                    });
                }
            }, NumberUtil.formartMoney_zhengshu(group.memberConfig.price));
            // 显示窗口
            popEnterPassword.showAtLocation(binding.rvNumbers, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

        });
    }

    private void loadData() {
        HttpUtil.apiW().meteor_list().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                HuiYuanBean dataBean = new Gson().fromJson(body.data.toString(), HuiYuanBean.class);
                for (HuiYuanBean tempData : dataBean.list) {
                    if (group.memberConfig.memberLevel.equals(tempData.memberConfig.memberLevel)) {
                        group = tempData;
                        initView();
                        break;
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                // 可加错误提示
            }
        });

//        RegisterBean registerBean = new RegisterBean();
//        registerBean.level = Integer.parseInt(group.memberConfig.memberLevel);
//        LoadingDialog.showDialog(getSupportFragmentManager(), "加载中..");
//        HttpUtil.apiW().meteorChangelist(registerBean).enqueue(new CommonCallback<NetData>() {
//            @Override
//            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                MemberCodeBean huiYuanBean = new Gson().fromJson(body.data.toString(), MemberCodeBean.class);
//                group.memberCode = huiYuanBean.list;
//                initView();
//            }
//
//            @Override
//            public void Failure(Call<NetData> call, Throwable t) {
//            }
//
//            @Override
//            public void end() {
//                super.end();
//                LoadingDialog.dismissDialog();
//            }
//        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
        if ("reload_fuhao".equals(event.getTag())) {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}