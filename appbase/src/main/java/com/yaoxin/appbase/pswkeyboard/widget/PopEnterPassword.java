package com.yaoxin.appbase.pswkeyboard.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.yaoxin.appbase.R;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.utils.NumberUtil;


/**
 * 输入支付密码
 *
 * @author lining
 */
public class PopEnterPassword extends PopupWindow {

    /** 金额达到该值（元）及以上时显示网络费 */
    public static final double NETWORK_FEE_THRESHOLD = 20d;
    public static final String NETWORK_FEE_TEXT = "网络费0.05";

    public PasswordView pwdView;

    private View mMenuView;

    private Activity mContext;

    public PopEnterPassword(final Activity context, OnPasswordInputFinish pass, String money) {
        this(context, pass, money, null, null, false);
    }

    /**
     * @param money 金额（元）
     * @param sceneTitle 场景文案，如「发红包」；空则不显示
     * @param balanceFen 零钱余额（分）；空则不显示余额行
     * @param showNetworkFee 是否显示网络费（发红包金额≥20 时应为 true）
     */
    public PopEnterPassword(
            final Activity context,
            OnPasswordInputFinish pass,
            String money,
            String sceneTitle,
            String balanceFen,
            boolean showNetworkFee) {

        super(context);

        this.mContext = context;


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMenuView = inflater.inflate(R.layout.pop_enter_password, null);

        pwdView = (PasswordView) mMenuView.findViewById(R.id.pwd_view);
        pwdView.moneyTv.setText("￥" + NumberUtil.formartLocalMoney(money));
        applyExtraInfo(sceneTitle, balanceFen, showNetworkFee);
        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish(final String password) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // 模拟耗时的操作。
                        try {

                            Thread.sleep(500);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mContext.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                pass.inputFinish(password);
                                dismiss();

                            }
                        });
                    }

                }).start();
            }
        });

        // 监听X关闭按钮
        pwdView.getImgCancel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 监听键盘上方的返回
        pwdView.getVirtualKeyboardView().getLayoutBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        pwdView.bgRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.pop_add_ainm);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x66000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

    }

    private void applyExtraInfo(String sceneTitle, String balanceFen, boolean showNetworkFee) {
        if (pwdView.sceneTv != null) {
            if (!TextUtils.isEmpty(sceneTitle)) {
                pwdView.sceneTv.setText(sceneTitle);
                pwdView.sceneTv.setVisibility(View.VISIBLE);
            } else {
                pwdView.sceneTv.setVisibility(View.GONE);
            }
        }
        boolean showBalance = !TextUtils.isEmpty(balanceFen);
        if (pwdView.balanceLl != null) {
            pwdView.balanceLl.setVisibility(showBalance || showNetworkFee ? View.VISIBLE : View.GONE);
        }
        if (pwdView.balanceTv != null) {
            if (showBalance) {
                pwdView.balanceTv.setText("零钱余额：" + NumberUtil.formartMoney(balanceFen));
                pwdView.balanceTv.setVisibility(View.VISIBLE);
            } else {
                pwdView.balanceTv.setVisibility(View.GONE);
            }
        }
        if (pwdView.networkFeeTv != null) {
            if (showNetworkFee) {
                pwdView.networkFeeTv.setText(NETWORK_FEE_TEXT);
                pwdView.networkFeeTv.setVisibility(View.VISIBLE);
            } else {
                pwdView.networkFeeTv.setVisibility(View.GONE);
            }
        }
    }

    /** 金额（元）是否达到网络费展示门槛 */
    public static boolean shouldShowNetworkFee(String moneyYuan) {
        if (TextUtils.isEmpty(moneyYuan)) {
            return false;
        }
        try {
            return Double.parseDouble(moneyYuan) >= NETWORK_FEE_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }
}
