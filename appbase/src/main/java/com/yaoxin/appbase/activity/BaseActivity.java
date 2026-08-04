package com.yaoxin.appbase.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.yaoxin.appbase.navbar.NavToolbar;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.yaoxin.appbase.view.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * Created by will
 * on 2018/5/25.
 */
public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private long lastClick = 0;//上次点击时间
    protected Map parmas = new HashMap();
    protected List dataList = new ArrayList();
    protected Bundle extras = new Bundle();
    protected int pageIndex = 1;
    protected int pageSize = 10;

    public ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        callBackResult(data);
                    }
                }
            }
    );
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                hideKeyboard(view);
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(event);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _getParams();
        _requestData();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁止屏幕旋转
    }

    protected void _initView() {
    }

    protected void _requestData() {

    }

    protected void transtStatusBar(NavToolbar navToolbar) {
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) navToolbar.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        navToolbar.setLayoutParams(params);
        navToolbar.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
    }
    protected void callBackResult(Intent data) {

    }
    protected void _getParams() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            extras = intent.getExtras();
        }
    }

    public static void start(Class activityClass, Context context, Map params) {
        Intent intent = new Intent(context, activityClass);
        if (params != null) {
            for (Object key : params.keySet()) {
                intent.putExtra((String) key, (String) params.get(key));
            }
        }
        context.startActivity(intent);
    }

    public static void startForResult(Class activityClass, BaseActivity activity, Map params) {
        Intent intent = new Intent(activity, activityClass);
        if (params != null) {
            for (Object key : params.keySet()) {
                intent.putExtra((String) key, (String) params.get(key));
            }
        }
        activity.startActivityForResult(intent, 8);
    }


    /**
     * 显示进度条对话框
     */
    protected void showProgressDialog() {
        showProgressDialog("请稍等...");
    }

    protected void showProgressDialog(String msg) {
        LoadingDialog.showDialog(getSupportFragmentManager(), msg);
    }

    /**
     * 隐藏进度条对话框
     */
    protected void hideProgressDialog() {
        LoadingDialog.dismissDialog();
    }


    public String getTextStr(TextView tv) {
        return tv.getText().toString().trim();
    }

    public void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断是否快速点击
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public boolean isFastClick() {
        long now = System.currentTimeMillis();
        if (now - lastClick >= 1000) {
            lastClick = now;
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Constant.RC_PHOTO_PICKER_PERM
                || requestCode == Constant.RC_PHOTO_CAMERA_PERM) {
            UploadUtil.resumePendingPhotoLibrary();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Constant.RC_PHOTO_PICKER_PERM) {
            ToastUtils.toastMsg("需要相册权限才能选择收款码");
        }
    }

}
