package com.yaoxin.appbase.navbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.yaoxin.appbase.R;
import com.yaoxin.appbase.utils.DensityUtils;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.ICallBack;

public class NavToolbar extends FrameLayout {

    private TextView titleView;
    private TextView subTitleView;
    private LinearLayout leftContainer;
    private LinearLayout titleContainer;
    private LinearLayout rightContainer;
    private ImageView underDivider;

    private int menuPaddingStart;
    private int menuPaddingEnd;
    private int menuLeftPadding;
    private int menuRightPadding;
    private CharSequence titleText;
    private int titleColor;
    private int titleSize;
    private CharSequence subTitleText;
    private int subTitleColor;
    private int subTitleSize;
    private int menuTextSize;// sp
    private int menuTextColor;
    private int backIconRes;
    private int closeIconRes;
    private int actionIconRes;
    private CharSequence actionText;
    private int underDividerHeight;
    private int underDividerColor;
    private boolean enableUnderDivider;
    private boolean supportTranslucentStatusBar;
    private int toolbarHeight;
    private int mTopMargin = 22;

    private ImageButton backButton;
    private ImageButton closeButton;
    private ImageButton actionButton;
    private TextView textButton;

    public NavToolbar(Context context) {
        this(context, null);
    }

    public NavToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array =
                getContext().obtainStyledAttributes(attrs, R.styleable.PascToolbar, defStyleAttr, 0);

        // 如果没设置背景，则取主题色作为背景
        Drawable background = getBackground();
        if (background == null) {
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(getResources().getColor(R.color.pasc_primary));
            setBackground(colorDrawable);
        }

        toolbarHeight = array.getDimensionPixelSize(R.styleable.PascToolbar_android_height, DensityUtils.dp2px(44));
        if (toolbarHeight == ViewGroup.LayoutParams.MATCH_PARENT
                || toolbarHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            throw new IllegalArgumentException("高度必须为一个指定的值，不能为 MATCH_PARENT 或者 WRAP_CONTENT ");
        }

        titleSize =
                array.getDimensionPixelSize(R.styleable.PascToolbar_title_text_size, DensityUtils.dp2px(18));
        titleColor =
                array.getColor(R.styleable.PascToolbar_title_text_color, Color.parseColor("#333333"));
        subTitleSize = array.getDimensionPixelSize(R.styleable.PascToolbar_sub_title_text_size,
                DensityUtils.dp2px(13));
        subTitleColor = array.getColor(R.styleable.PascToolbar_sub_title_text_color,
                Color.parseColor("#333333"));

        final CharSequence title = array.getText(R.styleable.PascToolbar_title);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        } else {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                CharSequence activityTitle = activity.getTitle();
                if (!TextUtils.isEmpty(activityTitle)) {
                    setTitle(activityTitle);
                }
            }
        }

        final CharSequence subTitle = array.getText(R.styleable.PascToolbar_sub_title);
        if (!TextUtils.isEmpty(subTitle)) {
            setSubTitle(subTitle);
        }

        backIconRes = array.getResourceId(R.styleable.PascToolbar_back_icon, R.mipmap.ic_back_black);
        closeIconRes = array.getResourceId(R.styleable.PascToolbar_close_icon, 0);
        actionIconRes = array.getResourceId(R.styleable.PascToolbar_action_icon, 0);
        actionText = array.getText(R.styleable.PascToolbar_action_text);
        menuRightPadding = DensityUtils.dp2px(10);
        menuLeftPadding = DensityUtils.dp2px(6);
        menuPaddingStart = menuPaddingEnd = DensityUtils.dp2px(15);
        menuTextSize =
                array.getDimensionPixelSize(R.styleable.PascToolbar_menu_text_size, DensityUtils.dp2px(15));
        menuTextColor =
                array.getColor(R.styleable.PascToolbar_menu_text_color, Color.parseColor("#333333"));

        enableUnderDivider = array.getBoolean(R.styleable.PascToolbar_enable_under_divider, true);
        underDividerColor =
                array.getColor(R.styleable.PascToolbar_under_divider_color, Color.parseColor("#E0E0E0"));
        underDividerHeight =
                array.getDimensionPixelSize(R.styleable.PascToolbar_under_divider_height, 1); //默认1px
        if (enableUnderDivider) {
            enableUnderDivider(true);
        }

        supportTranslucentStatusBar = array.getBoolean(R.styleable.PascToolbar_support_translucent_status_bar, false);

        if (backIconRes != 0) {
            setBackIcon(backIconRes);

        }
        if (closeIconRes != 0) {
            setCloseIcon(closeIconRes);
        }

        if (actionIconRes != 0) {
            setActionIcon(actionIconRes);
        }
        if (!TextUtils.isEmpty(actionText)) {
            setActionText(actionText.toString());
        }

        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = heightMeasureSpec;
        if (DeviceUtils.isMui5()) {
            int topMarginPx = DensityUtils.dp2px(mTopMargin);
            height = height + topMarginPx;
//      this.setPadding(getLeft() , getTop() + topMarginPx+30, getRight(),
//              0);
        }
        super.onMeasure(widthMeasureSpec, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void enableUnderDivider(boolean enableUnderDivider) {
        if (underDivider == null) {
            underDivider = new ImageView(getContext());
            ViewGroup.LayoutParams lp = new LayoutParams(MATCH_PARENT, underDividerHeight);
            ((LayoutParams) lp).gravity = Gravity.BOTTOM;
            underDivider.setBackgroundColor(underDividerColor);
            addView(underDivider, lp);
        }
        underDivider.setVisibility(enableUnderDivider ? VISIBLE : GONE);
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (titleView == null) {
                titleView = new AppCompatTextView(getContext());
                titleView.setSingleLine();
                titleView.setEllipsize(TextUtils.TruncateAt.END);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
                titleView.setTextColor(titleColor);
                titleView.setMaxEms(13);
                titleView.setGravity(Gravity.CENTER);
                LayoutParams lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                lp.gravity = Gravity.CENTER;
                makeSureTitleContainer();
                titleContainer.addView(titleView, lp);
            }
            titleView.setText(title);
        }
        this.titleText = title;
    }

    /**
     * 确保标题容器实例化
     *
     * @return 返回菜单容器
     */
    private LinearLayout makeSureTitleContainer() {
        if (titleContainer == null) {
            titleContainer = new LinearLayout(getContext());
            titleContainer.setOrientation(LinearLayout.VERTICAL);
            titleContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            LayoutParams lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp.gravity = Gravity.CENTER;
            addView(titleContainer, lp);
        }
        return titleContainer;
    }

    /**
     * 确保左菜单容器实例化
     *
     * @return 返回菜单容器
     */
    private LinearLayout makeSureLeftContainer() {
        if (leftContainer == null) {
            leftContainer = new LinearLayout(getContext());
            leftContainer.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams lp = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            lp.gravity = Gravity.LEFT;
            addView(leftContainer, lp);
        }
        return leftContainer;
    }

    /**
     * 确保右菜单容器实例化
     *
     * @return 返回菜单容器
     */
    private LinearLayout makeSureRightContainer() {
        if (rightContainer == null) {
            rightContainer = new LinearLayout(getContext());
            rightContainer.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams lp = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            lp.gravity = Gravity.RIGHT;
            addView(rightContainer, lp);
        }
        return leftContainer;
    }

    public void setSubTitle(CharSequence subTitle) {
        if (!TextUtils.isEmpty(subTitle)) {
            if (subTitleView == null) {
                subTitleView = new AppCompatTextView(getContext());
                subTitleView.setSingleLine();
                subTitleView.setEllipsize(TextUtils.TruncateAt.END);
                subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, subTitleSize);
                subTitleView.setTextColor(subTitleColor);
                subTitleView.setMaxEms(13);
                subTitleView.setGravity(Gravity.CENTER);
                subTitleView.setText(subTitle);
                LayoutParams lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                makeSureTitleContainer();
                titleContainer.addView(subTitleView, lp);
            }
            subTitleView.setText(subTitle);
        }
        subTitleText = subTitle;
    }

    @Nullable
    public CharSequence getSubTitle() {
        return subTitleText;
    }

    @Nullable
    public CharSequence getTitle() {
        return titleText;
    }

    /**
     * 获取标题的view，可能返回空，如果没有设置子标题的话
     *
     * @return titleView
     */
    @Nullable
    public TextView getTitleView() {
        return titleView;
    }

    /**
     * 获取副标题的view，可能返回空，如果没有设置子标题的话
     *
     * @return subTitleView
     */
    @Nullable
    public TextView getSubTitleView() {
        return subTitleView;
    }

    /**
     * 获取放置标题的容器，可能返回空，如果没有设置过标题的话
     *
     * @return
     */
    @Nullable
    public LinearLayout getTitleContainer() {
        return titleContainer;
    }

    @Deprecated
    public void clearLeftMenu() {
        this.leftContainer.removeAllViews();
    }

    @Deprecated
    public void clearRightMenu() {
        this.rightContainer.removeAllViews();
    }

    public void setNavViewVisible(boolean visible) {
        if (visible) {
            this.leftContainer.setVisibility(VISIBLE);
        } else {
            this.leftContainer.setVisibility(GONE);
        }
    }

    public void setTitleViewVisible(boolean visible) {
        if (visible) {
            this.titleContainer.setVisibility(VISIBLE);
        } else {
            this.titleContainer.setVisibility(GONE);
        }
    }

    public void setActionViewVisible(boolean visible) {
        if (visible) {
            this.rightContainer.setVisibility(VISIBLE);
        } else {
            this.rightContainer.setVisibility(GONE);
        }
    }

    public void setNavView(View mView) {
        this.leftContainer.removeAllViews();
        this.leftContainer.addView(mView);

    }

    public void setTitleView(View mView) {
        this.titleContainer.removeAllViews();
        this.titleContainer.addView(mView);
    }

    public void setActionView(View mView) {
        this.rightContainer.removeAllViews();
        this.rightContainer.addView(mView);
    }

    public ImageButton setBackIcon(@DrawableRes int drawableResId) {
        makeSureLeftContainer();
        if (backButton == null) {
            backButton = generateImageButton();
            backButton.setPadding(leftContainer.getChildCount() == 0 ? menuPaddingStart : menuLeftPadding,
                    0, menuLeftPadding, 0);
            backButton.setImageResource(drawableResId);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            backButton.setLayoutParams(lp);

            leftContainer.addView(backButton);
        } else {
            backButton.setImageResource(drawableResId);
        }

        return backButton;
    }

    public void setBackIconClickListener(final ICallBack callBack) {
        if (backButton != null) {
            backButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.callBack();
                    }
                }
            });
        }
    }


    public ImageButton setCloseIcon(@DrawableRes int drawableResId) {
        makeSureLeftContainer();
        if (closeButton == null) {
            closeButton = generateImageButton();
            closeButton.setPadding(leftContainer.getChildCount() == 0 ? menuPaddingStart : menuLeftPadding,
                    0, menuLeftPadding, 0);
            closeButton.setImageResource(drawableResId);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            closeButton.setLayoutParams(lp);
            leftContainer.addView(closeButton);
        } else {
            closeButton.setImageResource(drawableResId);
        }

        return closeButton;
    }

    public void setCloseIconClickListener(final ICallBack callBack) {

        if (closeButton != null) {
            closeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.callBack();
                    }
                }
            });
        }
    }


    public ImageButton setActionIcon(@DrawableRes int drawableResId) {
        makeSureRightContainer();
        if (actionButton == null) {
            actionButton = generateImageButton();
            actionButton.setImageResource(drawableResId);
            actionButton.setPadding(menuRightPadding, 0,
                    rightContainer.getChildCount() == 0 ? menuPaddingEnd : menuRightPadding, 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            actionButton.setLayoutParams(lp);
            rightContainer.addView(actionButton, 0);
        } else {
            actionButton.setImageResource(drawableResId);
        }

        return actionButton;
    }

    public void setActionClickListener(final ICallBack callBack) {

        if (actionButton != null) {
            actionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.callBack();
                    }
                }
            });
        }
        if (textButton != null) {
            textButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.callBack();
                    }
                }
            });
        }
    }


    public TextView setActionText(String text) {
        makeSureRightContainer();
        if (textButton == null) {
            textButton = generateTextButton();
            textButton.setText(text);
            textButton.setPadding(menuRightPadding, 0,
                    rightContainer.getChildCount() == 0 ? menuPaddingEnd : menuRightPadding, 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            textButton.setLayoutParams(lp);
            rightContainer.addView(textButton, 0);
        } else {
            textButton.setText(text);
        }

        return textButton;
    }


    /**
     * 添加关闭/返回按钮
     *
     * @return
     */
    @Deprecated
    public ImageButton addCloseImageButton() {
        return addLeftImageButton(backIconRes);
    }

    @Deprecated
    public ImageButton addLeftImageButton(@DrawableRes int drawableResId) {
        makeSureLeftContainer();
        if (backButton == null) {
            backButton = generateImageButton();
            backButton.setPadding(leftContainer.getChildCount() == 0 ? menuPaddingStart : menuLeftPadding,
                    0, menuLeftPadding, 0);
            backButton.setImageResource(drawableResId);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            backButton.setLayoutParams(lp);

            leftContainer.addView(backButton);
        } else {
            backButton.setImageResource(drawableResId);
        }

        return backButton;
    }

    @Deprecated
    public TextView addLeftTextButton(String text) {
        makeSureLeftContainer();
        leftContainer.removeAllViews();
        final TextView textButton = generateTextButton();
        textButton.setText(text);
        textButton.setPadding(leftContainer.getChildCount() == 0 ? menuPaddingStart : menuLeftPadding,
                0, menuLeftPadding, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        textButton.setLayoutParams(lp);
        leftContainer.addView(textButton);
        return textButton;
    }

    @Deprecated
    public ImageButton addRightImageButton(@DrawableRes int drawableResId) {
        makeSureRightContainer();
        if (actionButton == null) {
            actionButton = generateImageButton();
            actionButton.setImageResource(drawableResId);
            actionButton.setPadding(menuRightPadding, 0,
                    rightContainer.getChildCount() == 0 ? menuPaddingEnd : menuRightPadding, 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            actionButton.setLayoutParams(lp);
            rightContainer.addView(actionButton, 0);
        } else {
            actionButton.setImageResource(drawableResId);
        }
        return actionButton;
    }

    @Deprecated
    public TextView addRightTextButton(String text) {
        makeSureRightContainer();
        if (textButton == null) {
            textButton = generateTextButton();
            textButton.setText(text);
            textButton.setPadding(menuRightPadding, 0,
                    rightContainer.getChildCount() == 0 ? menuPaddingEnd : menuRightPadding, 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            textButton.setLayoutParams(lp);
            rightContainer.addView(textButton, 0);
        } else {
            textButton.setText(text);
        }
        return textButton;
    }

    private TextView generateTextButton() {
        final TextView textButton = new TextView(getContext());
        textButton.setGravity(Gravity.CENTER);
        textButton.setClickable(true);
        textButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, menuTextSize);
        textButton.setTextColor(menuTextColor);
        textButton.setBackgroundResource(R.drawable.selector_button_common);
        return textButton;
    }

    private ImageButton generateImageButton() {
        final ImageButton imageButton = new ImageButton(getContext());
        imageButton.setBackgroundResource(R.drawable.selector_button_common);
        return imageButton;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (supportTranslucentStatusBar && Build.VERSION.SDK_INT < 20) {
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = toolbarHeight + insets.top;
            this.setLayoutParams(lp);
            this.setPadding(insets.left + getPaddingLeft(), insets.top, insets.right + getPaddingRight(),
                    0);
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        if (supportTranslucentStatusBar && Build.VERSION.SDK_INT >= 20) {
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = toolbarHeight + insets.getSystemWindowInsetTop();
            this.setLayoutParams(lp);
            this.setPadding(insets.getSystemWindowInsetLeft() + getPaddingLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight() + getPaddingRight(), 0);
        }
        return super.dispatchApplyWindowInsets(insets);
    }
}
