package com.imagetopdf.converter.activity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.imagetopdf.converter.R;
import com.imagetopdf.converter.Utils.Helper;


public class PrivacyPolicyActivity extends Activity {
    private static Dialog loading_dialog;
    public static Activity privacy_policy_activity;
    String dialog_message = "Fetching Privacy & Policy";
    protected WebView privacy_web_view;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        SetView();
    }

    private void SetView() {
        setContentView(R.layout.activity_privacy_policy);
        privacy_policy_activity = this;
        Helper.is_show_open_ad = false;
        ShowLoadingDialog(this.dialog_message);
        this.privacy_web_view = findViewById(R.id.privacy_web_view);
        this.privacy_web_view.setWebViewClient(new MyWebViewClient());
        this.privacy_web_view.getSettings().setUseWideViewPort(true);
        this.privacy_web_view.getSettings().setLoadWithOverviewMode(true);
        this.privacy_web_view.getSettings().setSupportZoom(true);
        this.privacy_web_view.getSettings().setBuiltInZoomControls(true);
        this.privacy_web_view.loadUrl(FastSave.getInstance().getString("PRIVACY_POLICY",""));
    }


    
    public class MyWebViewClient extends WebViewClient {
        private MyWebViewClient() {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            webView.loadUrl(str);
            return true;
        }

        @Override
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            super.onPageStarted(webView, str, bitmap);
        }

        @Override
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            if (PrivacyPolicyActivity.this.privacy_web_view.getProgress() == 100) {
                PrivacyPolicyActivity.DismissLoadingDialog();
            }
        }
    }

    public void LoadingDialog(String str) {
        loading_dialog = new Dialog(this, R.style.TransparentBackground);
        loading_dialog.requestWindowFeature(1);
        loading_dialog.setContentView(R.layout.dialog_loading);
        TextView textView = loading_dialog.findViewById(R.id.dialog_loading_txt_message);
        textView.setText(str);
        loading_dialog.show();
    }

    private void ShowLoadingDialog(String str) {
        LoadingDialog(str);
    }


    public static void DismissLoadingDialog() {
        Dialog dialog = loading_dialog;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        BackScreen();
    }

    private void BackScreen() {
        Helper.is_show_open_ad = true;
        finish();
    }
}
