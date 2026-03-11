package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.huiyuan.LiangHaoZoneMoreActivity;
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

import retrofit2.Call;
import retrofit2.Response;

public class LiangHaoGroupAdapter extends BaseQuickAdapter<HuiYuanBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, HuiYuanBean group) {
        ImageView ivIcon = holder.getView(R.id.ivGroupIcon);
        TextView tvMore = holder.getView(R.id.tvMore);
        RecyclerView rvNumbers = holder.getView(R.id.rvNumbers);
        int memberLevel = Integer.parseInt(group.memberConfig.memberLevel);
        String imageName = "mine_grade_level_" + memberLevel;
        int resId = getContext().getResources().getIdentifier(imageName, "mipmap", getContext().getPackageName());
        // 如果找到了资源，则可以使用这个ID获取Drawable
        Drawable drawable = null;
        if (resId > 0) {
            drawable = ContextCompat.getDrawable(getContext(), resId);
        }
        ivIcon.setImageDrawable(drawable);
        tvMore.setText("查看更多>");
        rvNumbers.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
        LiangHaoNumberAdapter numberAdapter = new LiangHaoNumberAdapter(memberLevel);
        rvNumbers.setAdapter(numberAdapter);
        numberAdapter.setItems(group.memberCode);
        // 可加tvMore点击事件

        tvMore.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), LiangHaoZoneMoreActivity.class);
            intent.putExtra("huiyuan", group);
            getContext().startActivity(intent);
        });

        numberAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
//            Intent intent = new Intent(getContext(), LiangHaoBuyActivity.class);
//            intent.putExtra("huiyuan", group);
//            intent.putExtra("index", i);
//            getContext().startActivity(intent);
            PopEnterPassword popEnterPassword = new PopEnterPassword((Activity) tvMore.getContext(), new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    RegisterBean registerBean = new RegisterBean();
                    registerBean.memberCode = group.memberCode.get(i) + "";
                    registerBean.password = password;
                    LoadingDialog.showDialog(((AppCompatActivity) tvMore.getContext()).getSupportFragmentManager(), "购买中..");
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
            popEnterPassword.showAtLocation(tvMore, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
        });
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_lianghao_group, parent);
    }
} 