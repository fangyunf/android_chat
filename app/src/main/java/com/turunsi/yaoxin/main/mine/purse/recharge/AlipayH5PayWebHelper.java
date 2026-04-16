package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;

/**
 * 支付宝 H5 支付：WebView 加载收银台 URL，由 {@link PayTask#payInterceptorWithUrl} 拦截调起支付宝。
 * 供 {@link H5PayActivity} 全屏展示或 {@link PurseRechargeActivity} 1×1 隐藏 WebView 复用同一套逻辑。
 */
public final class AlipayH5PayWebHelper {

    private AlipayH5PayWebHelper() {
    }

    public static void configure(final WebView webView, final Activity activity) {
        if (webView == null || activity == null) {
            return;
        }
        WebSettings settings = webView.getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMinimumFontSize(settings.getMinimumFontSize() + 8);
        settings.setAllowFileAccess(false);
        settings.setTextSize(WebSettings.TextSize.NORMAL);
        webView.setVerticalScrollbarOverlay(true);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                        if (TextUtils.isEmpty(url)) {
                            return true;
                        }
                        String lowerUrl = url.toLowerCase();
                        if (lowerUrl.startsWith("alipays://")
                                || lowerUrl.startsWith("alipay://")
                                || lowerUrl.startsWith("intent://")) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                activity.startActivity(intent);
                            } catch (Exception ignored) {
                            }
                            return true;
                        }
                        if (!(lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://"))) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                activity.startActivity(intent);
                            } catch (Exception ignored) {
                            }
                            return true;
                        }
                        final PayTask task = new PayTask(activity);
                        boolean isIntercepted =
                                task.payInterceptorWithUrl(
                                        url,
                                        true,
                                        new H5PayCallback() {
                                            @Override
                                            public void onPayResult(final H5PayResultModel result) {
                                                final String returnUrl = result.getReturnUrl();
                                                if (!TextUtils.isEmpty(returnUrl)) {
                                                    activity.runOnUiThread(
                                                            new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    view.loadUrl(returnUrl);
                                                                }
                                                            });
                                                }
                                            }
                                        });
                        if (!isIntercepted) {
                            view.loadUrl(url);
                        }
                        return true;
                    }
                });
    }

    public static void destroyWebView(WebView webView) {
        if (webView == null) {
            return;
        }
        webView.stopLoading();
        webView.setWebViewClient(null);
        webView.removeAllViews();
        try {
            webView.destroy();
        } catch (Throwable ignored) {
        }
    }
}
