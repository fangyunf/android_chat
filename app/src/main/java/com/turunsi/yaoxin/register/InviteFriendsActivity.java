package com.turunsi.yaoxin.register;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityInviteFriendsBinding;
import com.turunsi.yaoxin.main.mine.DownLoadBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity implements View.OnClickListener {
    private ActivityInviteFriendsBinding binding;
    private Handler handler = new Handler();
    private String inviteCode = ""; // 邀请码或邀请链接
    private String downloadUrl = ""; // 下载链接

    public static void start(Context context) {
        Intent intent = new Intent(context, InviteFriendsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInviteFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityInviteFriendsNav);
        binding.activityInviteFriendsNav.addCloseImageButton().setOnClickListener(this);
        binding.btnInviteNow.setOnClickListener(this);

        initData();
        requestDownloadUrl();
    }

    private void initData() {
//        // 获取用户信息
//        UserBean userInfo = DataUtil.getUserInfo();
//        if (userInfo != null) {
//            // 如果有邀请码，使用邀请码；否则使用用户ID
//            inviteCode = TextUtils.isEmpty(userInfo.inviteCode) ? userInfo.userId : userInfo.inviteCode;
//
//            // 显示账号数据
//            if (!TextUtils.isEmpty(userInfo.memberCode)) {
//                binding.llAccountData.setVisibility(View.VISIBLE);
//                binding.tvAccountValue.setText(userInfo.memberCode);
//            }
//        }
//
//        // 自动复制邀请码到剪贴板
//        copyInviteCode();
    }

    /**
     * 请求下载链接
     */
    private void requestDownloadUrl() {
        HttpUtil.apiW().customer_about(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        try {
                            DownLoadBean downLoadBean = new Gson().fromJson(body.data.toString(), DownLoadBean.class);
                            if (downLoadBean != null && downLoadBean.linkUrl != null) {
                                for (DownLoadBean tempBean : downLoadBean.linkUrl) {
                                    // 根据当前设备类型获取对应的下载链接（Android设备获取ANDROID链接）
                                    if (tempBean.appType != null && tempBean.appType.equals("ANDROID")) {
                                        downloadUrl = tempBean.downloadUrl;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        // 失败时不做处理
                    }
                });
    }

    private void copyInviteCode() {
        if (TextUtils.isEmpty(inviteCode)) {
            return;
        }

        // 获取剪切板管理器
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建ClipData对象并将文本复制到剪切板
        ClipData clip = ClipData.newPlainText("邀请码", inviteCode);
        clipboard.setPrimaryClip(clip);

        // 显示复制成功提示
        binding.tvCopyStatus.setVisibility(View.VISIBLE);

        // 3秒后隐藏提示
        handler.postDelayed(() -> {
            binding.tvCopyStatus.setVisibility(View.GONE);
        }, 3000);
    }

    /**
     * 分享下载链接
     */
    private void shareDownloadUrl() {
        // 如果没有下载链接，使用邀请码
        String shareText = "";
        if (!TextUtils.isEmpty(downloadUrl)) {
            shareText = "邀请您下载使用，下载链接：" + downloadUrl;
            if (!TextUtils.isEmpty(inviteCode)) {
                shareText += "\n邀请码：" + inviteCode;
            }
        } else if (!TextUtils.isEmpty(inviteCode)) {
            shareText = "邀请您下载使用，邀请码：" + inviteCode;
        } else {
            ToastUtils.toastMsg("分享内容为空");
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "邀请好友下载使用");

        try {
            startActivity(Intent.createChooser(shareIntent, "分享到"));
        } catch (Exception e) {
            ToastUtils.toastMsg("分享失败");
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityInviteFriendsNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.btnInviteNow) {
            // 调用系统分享功能
            shareDownloadUrl();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
