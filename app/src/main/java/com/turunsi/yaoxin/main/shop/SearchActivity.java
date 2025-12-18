package com.turunsi.yaoxin.main.shop;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivitySearchBinding;
import com.turunsi.yaoxin.main.shop.manager.SearchHistoryManager;
import com.yaoxin.appbase.utils.DensityUtils;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.List;

/**
 * 商品搜索页面
 */
public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private SearchHistoryManager historyManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityProductDetailNav);
        binding.activityProductDetailNav.setBackIconClickListener(() -> finish());
        historyManager = new SearchHistoryManager(this);

        initViews();
        loadSearchHistory();
    }

    private void initViews() {
        // 搜索框
        binding.activitySearchEditText.setHint("搜索商品关键字");
        binding.activitySearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        binding.activitySearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 可以在这里实现实时搜索
            }
        });

        // 取消按钮
        binding.activitySearchCancelBtn.setOnClickListener(v -> finish());

        // 历史记录标签容器
        binding.activitySearchHistoryTagsContainer.removeAllViews();
    }

    private void loadSearchHistory() {
        List<String> history = historyManager.getSearchHistory();
        updateHistoryTags(history);
    }

    private void updateHistoryTags(List<String> historyList) {
        // 清空现有标签
        binding.activitySearchHistoryTagsContainer.removeAllViews();

        if (historyList == null || historyList.isEmpty()) {
            return;
        }

        int tagSpacing = DensityUtils.dp2px(12);
        int screenWidth = DensityUtils.getScreenWidth(this);
        int maxWidth = screenWidth - DensityUtils.dp2px(32); // 左右各16dp
        int tagHeight = DensityUtils.dp2px(36);

        LinearLayout currentRow = null;
        int currentRowWidth = 0;

        for (String historyItem : historyList) {
            TextView tagView = createHistoryTag(historyItem);

            // 测量标签宽度（使用 EXACTLY 模式确保准确测量）
            int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(tagHeight, View.MeasureSpec.EXACTLY);
            tagView.measure(widthSpec, heightSpec);
            int tagWidth = tagView.getMeasuredWidth();

            // 检查是否需要换行
            if (currentRow == null || currentRowWidth + tagWidth + tagSpacing > maxWidth) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (currentRowWidth > 0) {
                    rowParams.topMargin = tagSpacing;
                }
                currentRow.setLayoutParams(rowParams);
                binding.activitySearchHistoryTagsContainer.addView(currentRow);
                currentRowWidth = 0;
            }

            // 添加标签到当前行
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    tagHeight);
            if (currentRowWidth > 0) {
                params.leftMargin = tagSpacing;
            }
            tagView.setLayoutParams(params);
            currentRow.addView(tagView);

            currentRowWidth += tagWidth + (currentRowWidth > 0 ? tagSpacing : 0);
        }
    }

    private TextView createHistoryTag(String title) {
        TextView tagView = new TextView(this);
        tagView.setText(title);
        tagView.setTextColor(getResources().getColor(R.color.black));
        tagView.setTextSize(14);
        tagView.setPadding(
                DensityUtils.dp2px(12),
                DensityUtils.dp2px(8),
                DensityUtils.dp2px(12),
                DensityUtils.dp2px(8)
        );
        tagView.setBackgroundResource(R.drawable.bg_search_history_tag);
        tagView.setClickable(true);
        tagView.setFocusable(true);

        tagView.setOnClickListener(v -> {
            String tagText = ((TextView) v).getText().toString();
            binding.activitySearchEditText.setText(tagText);
            binding.activitySearchEditText.setSelection(tagText.length());
            performSearch();
        });

        return tagView;
    }

    private void performSearch() {
        String searchText = binding.activitySearchEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(searchText)) {
            // 保存搜索历史
            historyManager.addSearchHistory(searchText);
            loadSearchHistory();

            // 执行搜索
            // TODO: 实现搜索逻辑
            // 可以跳转到搜索结果页面，或者在当前页面显示搜索结果

            // 隐藏键盘
            View view = this.getCurrentFocus();
            if (view != null) {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}

