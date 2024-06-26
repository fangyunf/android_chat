package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.view.actionsheet.ActionSheet;

import retrofit2.Call;
import retrofit2.Response;

public class DialogAlertUtil {
    public static void showAlert(String content, DialogAlertUtilCallBack callBack, FragmentManager fragmentManager) {
        CommonChoiceDialog dialog = new CommonChoiceDialog();
        dialog
                .setTitleStr("温馨提示")
                .setContentStr(content)
                .setNegativeStr("取消")
                .setPositiveStr("确定")
                .setConfirmListener(
                        new ChoiceListener() {
                            @Override
                            public void onPositive() {
                                callBack.clickType(1);
                            }

                            @Override
                            public void onNegative() {

                                callBack.clickType(0);
                            }
                        })
                .show(fragmentManager);
    }

    public interface DialogAlertUtilCallBack {
        void clickType(int type);
    }

    public  static void showSheetView(Context context, FragmentManager fragmentManager,String[] titles, DialogAlertUtilCallBack callBack) {
        ActionSheet.createBuilder(context, fragmentManager)
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles(titles)
                .setCancelableOnTouchOutside(true)
                .setListener(new ActionSheet.ActionSheetListener() {
                    @Override
                    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
                        callBack.clickType(0);
                    }

                    @Override
                    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
                        callBack.clickType(index + 1);

                    }
                }).show();
    }
}
