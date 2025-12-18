package com.turunsi.yaoxin.main.shop.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.DialogBuyConfirmBinding;
import com.turunsi.yaoxin.main.mine.address.AddressListActivity;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;
import com.turunsi.yaoxin.main.shop.ShopAddressListActivity;
import com.turunsi.yaoxin.main.shop.model.ProductModel;
import com.turunsi.yaoxin.main.shop.utils.ImageLoadUtil;
import com.yaoxin.appbase.utils.DensityUtils;

/**
 * 购买确认底部弹窗
 */
public class BuyConfirmDialog extends BottomSheetDialog {

    private DialogBuyConfirmBinding binding;
    private ProductModel productModel;
    private int selectedMonth = 1; // 默认1个月
    private AddressListBean selectedAddress;
    private OnConfirmListener confirmListener;

    public interface OnConfirmListener {
        void onConfirm(ProductModel product, int months, AddressListBean address);
    }

    public BuyConfirmDialog(@NonNull Context context, ProductModel productModel) {
        super(context, R.style.TransBottomSheetTheme);
        this.productModel = productModel;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogBuyConfirmBinding.inflate(getLayoutInflater());

        // 设置全屏宽度
        View rootView = binding.getRoot();
        setContentView(rootView);

        // 确保宽度为全屏
        if (getWindow() != null) {
            getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        setupData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 在onStart中设置BottomSheetBehavior，确保内容完整显示，不需要滑动
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.post(() -> {
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                // 设置peekHeight为内容高度，确保完整显示
                int contentHeight = binding.getRoot().getHeight();
                if (contentHeight > 0) {
                    behavior.setPeekHeight(contentHeight);
                } else {
                    // 如果高度为0，使用固定高度414dp
                    behavior.setPeekHeight(DensityUtils.dp2px(414));
                }
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                // 禁用拖拽，防止用户滑动
                behavior.setDraggable(false);
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initViews() {
        // 地址选择区域点击事件
        binding.dialogBuyConfirmAddressContainer.setOnClickListener(v -> {
//            // 跳转到地址管理页面（选择模式）
//            if (getContext() instanceof Activity) {
//                Intent intent = new Intent(getContext(), ShopAddressListActivity.class);
//                intent.putExtra("select_mode", true); // 标记为选择模式
//                ((Activity) getContext()).startActivityForResult(intent, 100);
//            }
        });

        // 租赁天数选择
        binding.dialogBuyConfirmDuration1.setOnClickListener(v -> selectDuration(1, binding.dialogBuyConfirmDuration1));
        binding.dialogBuyConfirmDuration3.setOnClickListener(v -> selectDuration(3, binding.dialogBuyConfirmDuration3));
        binding.dialogBuyConfirmDuration6.setOnClickListener(v -> selectDuration(6, binding.dialogBuyConfirmDuration6));

        // 确定按钮
        binding.dialogBuyConfirmConfirmBtn.setOnClickListener(v -> {
            if (selectedAddress == null) {
                Toast.makeText(getContext(), "请选择收货地址", Toast.LENGTH_SHORT).show();
                return;
            }

            if (confirmListener != null) {
                confirmListener.onConfirm(productModel, selectedMonth, selectedAddress);
            }
            dismiss();
        });

        // 默认选中1个月
        selectDuration(1, binding.dialogBuyConfirmDuration1);
    }

    private void setupData() {
        // 商品图片
        if (productModel.productCoverImages != null && !productModel.productCoverImages.isEmpty()) {
            String imageName = productModel.productCoverImages.get(0);
            String assetPath = "images/" + imageName;
            ImageLoadUtil.loadFromAssets(getContext(), binding.dialogBuyConfirmProductIcon, assetPath);
        }

        // 商品名称
        binding.dialogBuyConfirmProductTitle.setText(productModel.productName);

        // 商品价格
        binding.dialogBuyConfirmProductPrice.setText("¥" + (int) productModel.monthlyRentPrice + "/月");

        // 押金
        binding.dialogBuyConfirmDepositBtn.setText("押金 ¥" + (int) productModel.depositPrice);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void selectDuration(int months, TextView selectedView) {
        selectedMonth = months;

        // 重置所有按钮样式
        binding.dialogBuyConfirmDuration1.setBackgroundResource(R.drawable.bg_duration_button_normal);
        binding.dialogBuyConfirmDuration1.setTextColor(getContext().getResources().getColor(R.color.color_222222, null));

        binding.dialogBuyConfirmDuration3.setBackgroundResource(R.drawable.bg_duration_button_normal);
        binding.dialogBuyConfirmDuration3.setTextColor(getContext().getResources().getColor(R.color.color_222222, null));

        binding.dialogBuyConfirmDuration6.setBackgroundResource(R.drawable.bg_duration_button_normal);
        binding.dialogBuyConfirmDuration6.setTextColor(getContext().getResources().getColor(R.color.color_222222, null));

        // 设置选中按钮样式
        selectedView.setBackgroundResource(R.drawable.bg_duration_button_selected);
        selectedView.setTextColor(getContext().getResources().getColor(R.color.color_DC14A0, null));
    }

    /**
     * 设置选中的地址
     */
    public void setSelectedAddress(AddressListBean address) {
        this.selectedAddress = address;
        if (address != null && address.name != null && address.phone != null) {
            binding.dialogBuyConfirmNamePhoneTv.setText(address.name + "  " + address.phone);
            if (address.fullAddress != null) {
                binding.dialogBuyConfirmAddressTv.setText(address.fullAddress);
            } else {
                binding.dialogBuyConfirmAddressTv.setText("");
            }
        } else {
            binding.dialogBuyConfirmNamePhoneTv.setText("请选择地址");
            binding.dialogBuyConfirmAddressTv.setText("");
        }
    }

    /**
     * 设置确认按钮监听器
     */
    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }
}


