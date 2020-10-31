package com.example.instasaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
 import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
    WebView webView;
    String username="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        webView=findViewById(R.id.webview);
        loadUrl("https://www.instagram.com/accounts/login/?source=auth_switcher");
    }
    void loadUrl(String url){


        webView.setWebViewClient(new Browse(url));
        if (Build.VERSION.SDK_INT>21){
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        }else{
            CookieManager.getInstance().setAcceptCookie(true);
        }
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(url);

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();

    }

    private class Browse extends WebViewClient {
        String url;
        Browse(String url){
            this.url=url;
        }
         @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            Log.e("LoginThread","Error received "+error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e("start",url);


        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            //webView.stopLoading();
            Log.e("finished",url);





        }
         @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
           // view.loadUrl(url);
             Log.e("overrride","started");
             view.evaluateJavascript("javascript:(function(){" +
                     "var username='';" +
                     "try{" +
                     "username=document.getElementsByClassName('_2hvTZ pexuQ zyHYP').username.value;" +
                     "}catch(error){" +
                     "" +
                     "}" +
                     "return (username);})();", new ValueCallback<String>() {
                 @Override
                 public void onReceiveValue(String s) {
                     Log.e("data","start data=>"+s);
                     s=s.replace("\"","");
                     if (!s.trim().equals("")) {
                         username=s;
                        webView.stopLoading();
                        SharedPreferences sharedPreferences= getSharedPreferences("SHARED_DATA",MODE_PRIVATE);
                        SharedPreferences.Editor edit=sharedPreferences.edit();
                        edit.putString("username",username.replace("\"","").trim().toLowerCase());
                        edit.apply();
                        Intent intent= new Intent(Login.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                     }
                 }});

             return super.shouldOverrideUrlLoading(view, request);
        }
    }

}
