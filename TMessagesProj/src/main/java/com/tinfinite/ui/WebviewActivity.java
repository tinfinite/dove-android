package com.tinfinite.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Browser;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;

/**
 * Created by PanJiafang on 15/5/12.
 */
public class WebviewActivity extends ActionBarActivity {

    private ActionBar actionBar;
    private WebView webView;
    private ProgressBar progressBar;

    private int item_other = 2;
    private int item_copy = 3;
    private int item_share = 4;

    private String url;

    public static Bundle createBundle(String url){
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        actionBar = (ActionBar) findViewById(R.id.webview_actionbar);
        webView = (WebView) findViewById(R.id.webview_webview);
        progressBar = (ProgressBar) findViewById(R.id.webview_prograssbar);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Web");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1)
                    finish();
                else if(id == item_other){
                    if(StringUtils.isEmpty(url))
                        return;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                    startActivity(intent);
                } else if(id == item_copy){
                    if(StringUtils.isEmpty(url))
                        return;
                    try {
                        if (Build.VERSION.SDK_INT < 11) {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(url);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("label", url);
                            clipboard.setPrimaryClip(clip);
                        }
                        Toast.makeText(WebviewActivity.this, LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                } else if(id == item_share){
                    if(StringUtils.isEmpty(url))
                        return;
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    startActivity(Intent.createChooser(intent, LocaleController.getString("", R.string.ShareFile)));
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItems = menu.addItem(1, R.drawable.ic_ab_other);

        menuItems.addSubItem(item_other, LocaleController.getString("", R.string.OpenInOtherBrowser), 0);
        menuItems.addSubItem(item_copy, LocaleController.getString("", R.string.CopyLink), 0);
        menuItems.addSubItem(item_share, LocaleController.getString("", R.string.ShareFile), 0);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        if(Build.VERSION.SDK_INT > 18)
            settings.setUseWideViewPort(true);
        else{
            settings.setSupportZoom(true);
        }

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if(newProgress == 100)
                    progressBar.setVisibility(View.GONE);
                else
                    progressBar.setVisibility(View.VISIBLE);
            }
        });

        if(getIntent() != null)
            url = getIntent().getStringExtra("url");

        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }
}
