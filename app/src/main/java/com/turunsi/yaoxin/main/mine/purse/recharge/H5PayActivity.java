package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class H5PayActivity extends Activity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = null;
        try {
            extras = getIntent().getExtras();
        } catch (Exception e) {
            finish();
            return;
        }
        if (extras == null) {
            finish();
            return;
        }
        String url = null;
        try {
            url = extras.getString("url");
        } catch (Exception e) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(url)) {
            // 测试H5支付，必须设置要打开的url网站
            new AlertDialog.Builder(H5PayActivity.this).setTitle("警告")
                    .setMessage("必须配置需要打开的url 站点，请在PayDemoActivity类的h5Pay中配置")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    }).show();

        }
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout, params);

        mWebView = new WebView(this);
        params.weight = 1;
        mWebView.setVisibility(android.view.View.VISIBLE);
        layout.addView(mWebView, params);

        AlipayH5PayWebHelper.configure(mWebView, this);
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlipayH5PayWebHelper.destroyWebView(mWebView);
        mWebView = null;
    }
}
