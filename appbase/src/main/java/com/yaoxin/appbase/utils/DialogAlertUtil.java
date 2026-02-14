package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

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
    public interface InputAlertCallBack {
        public void inputText(String text);
    }

    public interface PopWindowCallBack {
        void onItemClick(int position, String itemText);

        void onDismiss();
    }

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

    public static void showSheetView(Context context, FragmentManager fragmentManager, String[] titles, DialogAlertUtilCallBack callBack) {
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

    public static void showInputAlert(Context context, String title, String message, InputAlertCallBack callBack) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "温馨提示" : title);
        builder.setMessage(message == null ? "" : message);

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                callBack.inputText(name);
                // 处理输入的名字，例如显示在Toast中

            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public static void showPopWindow(Context context, View anchorView, String[] data, PopWindowCallBack callback, int xOffset, int yOffset) {
        if (context == null || anchorView == null || data == null || data.length == 0) {
            return;
        }

        // 创建PopWindow
        PopupWindow popupWindow = new PopupWindow(context);

        // 创建内容视图先，测量宽度
        View contentView = createPopWindowContentView(context, data, callback, popupWindow);

        // 设置PopWindow的固定宽度，避免宽度问题
        int popWidth = (int) (200 * context.getResources().getDisplayMetrics().density);
        popupWindow.setWidth(popWidth);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // 设置背景，避免点击外部无法关闭
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 设置外部可点击关闭
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // 设置内容视图
        popupWindow.setContentView(contentView);

        // 获取锚点视图在屏幕上的绝对位置，避免位置跟随变化
        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        int absoluteX = location[0] + xOffset;
        int absoluteY = location[1] + anchorView.getHeight() + yOffset;

        // 使用绝对位置显示PopWindow，避免位置跟随view变化
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, absoluteX, absoluteY);

        // 设置消失监听
        popupWindow.setOnDismissListener(() -> {
            if (callback != null) {
                callback.onDismiss();
            }
        });
    }


    /**
     * 创建PopWindow的内容视图
     */
    private static View createPopWindowContentView(Context context, String[] data, PopWindowCallBack callback, PopupWindow popupWindow) {
        // 计算固定宽度
        int popWidth = (int) (140 * context.getResources().getDisplayMetrics().density);
        // 创建主容器，包含箭头和内容
        android.widget.LinearLayout mainContainer = new android.widget.LinearLayout(context);
        mainContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        // 强制设置固定宽度
        android.widget.LinearLayout.LayoutParams mainParams = new android.widget.LinearLayout.LayoutParams(
                popWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mainContainer.setLayoutParams(mainParams);

        // 创建箭头视图
        View arrowView = createArrowView(context);
        mainContainer.addView(arrowView);

        // 创建内容容器
        android.widget.LinearLayout contentContainer = new android.widget.LinearLayout(context);
        contentContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        contentContainer.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // 设置内容容器的布局参数，使用固定宽度
        android.widget.LinearLayout.LayoutParams contentParams = new android.widget.LinearLayout.LayoutParams(
                popWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentContainer.setLayoutParams(contentParams);

        // 设置内边距
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        contentContainer.setPadding(padding, padding, padding, padding);

        // 动态添加文本项
        for (int i = 0; i < data.length; i++) {
            TextView textView = new TextView(context);
            textView.setText(data[i]);
            textView.setTextSize(12); // 文字改小
            textView.setTextColor(0xFF333333);
            textView.setPadding(padding, padding / 2, padding, padding / 2);

            // 设置TextView的布局参数，确保宽度
            android.widget.LinearLayout.LayoutParams textParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(textParams);

            // 设置点击事件
            final int position = i;
            final String itemText = data[i];
            textView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onItemClick(position, itemText);
                }
                popupWindow.dismiss();
            });

            // 设置背景选择器（点击效果）
            textView.setBackgroundResource(android.R.drawable.list_selector_background);

            contentContainer.addView(textView);

            // 添加分割线（除了最后一项）
            if (i < data.length - 1) {
                View divider = new View(context);
                divider.setBackgroundColor(0xFFDDDDDD);
                android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                );
                divider.setLayoutParams(dividerParams);
                contentContainer.addView(divider);
            }
        }

        mainContainer.addView(contentContainer);
        return mainContainer;
    }

    /**
     * 创建箭头视图
     */
    private static View createArrowView(Context context) {
        // 创建自定义箭头视图
        View arrowView = new View(context) {
            @Override
            protected void onDraw(android.graphics.Canvas canvas) {
                super.onDraw(canvas);

                // 获取视图尺寸
                int width = getWidth();
                int height = getHeight();

                if (width == 0 || height == 0) return;

                // 创建画笔
                android.graphics.Paint paint = new android.graphics.Paint();
                paint.setAntiAlias(true);

                // 创建三角形路径（向上的箭头）
                android.graphics.Path path = new android.graphics.Path();
                path.moveTo(width / 2f, 0); // 顶点
                path.lineTo(width * 0.2f, height); // 左下角
                path.lineTo(width * 0.8f, height); // 右下角
                path.close();

                // 绘制阴影效果
                paint.setColor(0x20000000); // 半透明黑色阴影
                paint.setStyle(android.graphics.Paint.Style.FILL);
                canvas.save();
                canvas.translate(2, 2); // 阴影偏移
                canvas.drawPath(path, paint);
                canvas.restore();

                // 绘制主体三角形
                paint.setColor(0xFFFFFFFF); // 白色箭头
                paint.setStyle(android.graphics.Paint.Style.FILL);
                canvas.drawPath(path, paint);

                // 绘制边框
                paint.setColor(0xFFDDDDDD);
                paint.setStyle(android.graphics.Paint.Style.STROKE);
                paint.setStrokeWidth(1);
                canvas.drawPath(path, paint);
            }
        };

        // 设置箭头尺寸
        int arrowWidth = (int) (20 * context.getResources().getDisplayMetrics().density);
        int arrowHeight = (int) (10 * context.getResources().getDisplayMetrics().density);
        android.widget.LinearLayout.LayoutParams arrowParams = new android.widget.LinearLayout.LayoutParams(
                arrowWidth,
                arrowHeight
        );
        arrowParams.gravity = android.view.Gravity.START; // 尖头显示在左边
        // 设置下边距为负值，让箭头和内容容器无缝连接
        arrowParams.setMargins((int) (20 * context.getResources().getDisplayMetrics().density), 0, 0, -arrowHeight);
        arrowView.setLayoutParams(arrowParams);

        return arrowView;
    }

}
