package com.yaoxin.appbase.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.yaoxin.appbase.databinding.ActivityBaseWebviewBinding;

public class BaseWebViewActivity extends BaseActivity{

    ActivityBaseWebviewBinding binding;
    int type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type1 = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");


        if (type1 != null) {
            type = Integer.parseInt(type1);
        }
        binding = ActivityBaseWebviewBinding.inflate(getLayoutInflater());
        if (title != null) {
            binding.activityBaseWebviewNav.getTitleView().setText(title);
        }
        setContentView(binding.getRoot());
        WebView webView = binding.activityBaseWebviewWebview;
        webView.getSettings().setJavaScriptEnabled(true); // 启用 JavaScript（如果需要）
        if (type == 1) {

            webView.loadUrl("file:///android_asset/tiaokuan.html");
        }
        if (type == 2) {

            webView.loadUrl("file:///android_asset/xieyi.html");
        }

        binding.activityBaseWebviewNav.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
