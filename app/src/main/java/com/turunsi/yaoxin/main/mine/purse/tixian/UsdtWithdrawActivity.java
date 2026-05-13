package com.turunsi.yaoxin.main.mine.purse.tixian;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityUsdtWithdrawBinding;
import com.turunsi.yaoxin.main.mine.purse.usdt.BindUsdtEntryActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/**
 * USDT 提现（TRC20），界面与费率、汇率规则见 {@link R.string#usdt_withdraw_notes}。
 */
public class UsdtWithdrawActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 从 {@link PurseTiXianActivity} 等传入的提现金额（元），如 "500"
     */
    public static final String EXTRA_INITIAL_CNY_YUAN = "initial_cny_yuan";

    private static final double USDT_WITHDRAW_MIN_YUAN = 500;
    /**
     * 提现费率：按提现金额（元）比例
     */
    private static final double USDT_WITHDRAW_FEE_RATE = 0.05;
    /**
     * 展示与换算用：1 USDT ≈ 7 元人民币
     */
    private static final double USDT_CNY_RATE = 7.0;
    private static final String RATE_LABEL = "1USDT ≈ 7.00 元";

    /**
     * {@code bindCard/userZFB} 仅查 USDT 绑定列表，与 {@link com.turunsi.yaoxin.main.mine.purse.usdt.BindUsdtEntryActivity} 一致
     */
    private static final int BIND_CARD_QUERY_USDT = 5;

    private ActivityUsdtWithdrawBinding binding;
    private ImageButton navCloseButton;
    private UserBean usdtPayBean;
    private int resumeCount;

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount > 1) {
            loadUsdtBind();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsdtWithdrawBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityUsdtWithdrawNav);
        navCloseButton = binding.activityUsdtWithdrawNav.addCloseImageButton();
        navCloseButton.setOnClickListener(this);

        binding.activityUsdtWithdrawRateTv.setText(RATE_LABEL);
        binding.activityUsdtWithdrawRebindTv.setOnClickListener(this);

        binding.activityUsdtWithdrawMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityUsdtWithdrawMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        binding.activityUsdtWithdrawMoneyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                int dotCount = input.length() - input.replace(".", "").length();
                if (dotCount > 1) {
                    int firstDotIndex = input.indexOf(".");
                    StringBuilder cleanedInput = new StringBuilder(input.substring(0, firstDotIndex + 1));
                    for (int i = firstDotIndex + 1; i < input.length(); i++) {
                        if (input.charAt(i) != '.') {
                            cleanedInput.append(input.charAt(i));
                        }
                    }
                    binding.activityUsdtWithdrawMoneyEt.setText(cleanedInput.toString());
                    binding.activityUsdtWithdrawMoneyEt.setSelection(cleanedInput.length());
                }
                refreshFeeAndArrivalUi();
            }
        });

        binding.activityUsdtWithdrawBtn.setOnClickListener(this);
        applyInitialAmountFromExtras();
        refreshFeeAndArrivalUi();
    }

    private void applyInitialAmountFromExtras() {
        if (extras == null) {
            return;
        }
        String raw = extras.getString(EXTRA_INITIAL_CNY_YUAN);
        if (TextUtils.isEmpty(raw)) {
            return;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return;
        }
        try {
            if (Double.parseDouble(t) <= 0) {
                return;
            }
        } catch (NumberFormatException e) {
            return;
        }
        binding.activityUsdtWithdrawMoneyEt.setText(t);
        binding.activityUsdtWithdrawMoneyEt.setSelection(t.length());
    }

    @Override
    protected void _requestData() {
        loadUsdtBind();
    }

    private void refreshFeeAndArrivalUi() {
        String textStr = getTextStr(binding.activityUsdtWithdrawMoneyEt).trim();
        double amountYuan = 0;
        if (!textStr.isEmpty()) {
            try {
                amountYuan = Double.parseDouble(textStr);
            } catch (NumberFormatException ignored) {
            }
        }
        double feeYuan = amountYuan * USDT_WITHDRAW_FEE_RATE;
        double netCny = amountYuan - feeYuan;
        if (netCny < 0) {
            netCny = 0;
        }
        double usdtArrival = netCny / USDT_CNY_RATE;
        binding.activityUsdtWithdrawFeeTv.setText(String.format(Locale.US, "%.2f", feeYuan));
        binding.activityUsdtWithdrawArrivalTv.setText(String.format(Locale.US, "%.2f", usdtArrival));
    }

    private void loadUsdtBind() {
        RegisterBean q = new RegisterBean();
        q.type = BIND_CARD_QUERY_USDT;
        HttpUtil.apiW().bindCard_userZFB(q).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                usdtPayBean = null;
                binding.activityUsdtWithdrawAddressTv.setText(R.string.usdt_withdraw_address_unbound);
                if (body == null || body.data == null) {
                    return;
                }
                Type type = new TypeToken<List<UserBean>>() {
                }.getType();
                List<UserBean> list = new Gson().fromJson(body.data.toString(), type);
                usdtPayBean = pickUsdtBindRecord(list);
                if (usdtPayBean != null) {
                    String u = usdtPayBean.usdt != null ? usdtPayBean.usdt.trim() : "";
                    binding.activityUsdtWithdrawAddressTv.setText(u.isEmpty() ? "--" : u);
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                usdtPayBean = null;
                binding.activityUsdtWithdrawAddressTv.setText(R.string.usdt_withdraw_address_unbound);
            }
        });
    }

    /**
     * 与 {@link BindUsdtEntryActivity} {@code pickBindRecord} 一致：取列表最后一条。
     */
    private static UserBean pickUsdtBindRecord(List<UserBean> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * 与 {@link PurseTiXianActivity} 支付宝/微信提现一致：优先 {@link UserBean#id}，否则 {@link UserBean#cardId}。
     */
    private static String resolveUserUsdtId(UserBean b) {
        if (b == null) {
            return "";
        }
        if (b.id != 0) {
            return String.valueOf(b.id);
        }
        return b.cardId != null ? b.cardId.trim() : "";
    }

    @Override
    public void onClick(View v) {
        if (v == navCloseButton) {
            finish();
        } else if (v == binding.activityUsdtWithdrawRebindTv) {
            BindUsdtEntryActivity.start(BindUsdtEntryActivity.class, UsdtWithdrawActivity.this, null);
        } else if (v == binding.activityUsdtWithdrawBtn) {
            String textStr = getTextStr(binding.activityUsdtWithdrawMoneyEt).trim();
            if (textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            double amountYuan;
            try {
                amountYuan = Double.parseDouble(textStr);
            } catch (NumberFormatException e) {
                ToastUtils.toastMsg("请输入正确金额");
                return;
            }
            if (amountYuan < USDT_WITHDRAW_MIN_YUAN) {
                ToastUtils.toastMsg("USDT提现金额500元起");
                return;
            }
            if (usdtPayBean != null) {
                showWithdrawPasswordPop(textStr);
            } else {
                fetchUsdtBindThenWithdraw(textStr);
            }
        }
    }

    private void showWithdrawPasswordPop(String textStr) {
        PopEnterPassword pop = new PopEnterPassword(UsdtWithdrawActivity.this, new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                tiXianClick(password);
            }
        }, textStr);
        pop.showAtLocation(binding.activityUsdtWithdrawLl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void fetchUsdtBindThenWithdraw(String textStr) {
        RegisterBean q = new RegisterBean();
        q.type = BIND_CARD_QUERY_USDT;
        HttpUtil.apiW().bindCard_userZFB(q).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (body == null || body.data == null) {
                    ToastUtils.toastMsg("请先绑定USDT地址");
                    BindUsdtEntryActivity.start(BindUsdtEntryActivity.class, UsdtWithdrawActivity.this, null);
                    return;
                }
                Type type = new TypeToken<List<UserBean>>() {
                }.getType();
                List<UserBean> list = new Gson().fromJson(body.data.toString(), type);
                UserBean last = pickUsdtBindRecord(list);
                if (last == null) {
                    ToastUtils.toastMsg("请先绑定USDT地址");
                    BindUsdtEntryActivity.start(BindUsdtEntryActivity.class, UsdtWithdrawActivity.this, null);
                    return;
                }
                usdtPayBean = last;
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    private void tiXianClick(String pwd) {
        String inputMoney = getTextStr(binding.activityUsdtWithdrawMoneyEt).trim();
        if (inputMoney.isEmpty()) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }
        if (usdtPayBean == null) {
            ToastUtils.toastMsg("请先绑定USDT地址");
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.payPassword = pwd;
        bean.amount = NumberUtil.formartUploadMoney(inputMoney);
        bean.type = 5;
        bean.zfbNo = "";
        bean.name = "";
        bean.zfbUrl = usdtPayBean.usdt;
        bean.userUsdtId = usdtPayBean.id + "";
        HttpUtil.apiW().withdraw_withdrawDeposit(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg("提现成功");
                finish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }
}
