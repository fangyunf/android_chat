package com.yaoxin.appbase.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.yaoxin.appbase.databinding.ActivityBaseWebviewBinding;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.HashMap;

public class BaseWebViewActivity extends BaseActivity{

    ActivityBaseWebviewBinding binding;
    int type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type1 = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");


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
        if (type == 3) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);

            HashMap map = new HashMap<>();
            map.put("url", Constant.BASE_URL_H5);
            map.put("token", DataUtil.getToken());
            String token = new Gson().toJson(map);
            try {

                String aesToken = AESUtil.aesEncrypt(token);
                String phone = DataUtil.getUserInfo().phoneNo;
                url = url + "?token=" + aesToken +"&phone=" + phone;
                webView.loadUrl(url);
            } catch (Exception e) {

            }

        }

        binding.activityBaseWebviewNav.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
