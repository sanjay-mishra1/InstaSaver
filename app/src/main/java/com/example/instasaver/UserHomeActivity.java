package com.example.instasaver;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instasaver.extra.PendingPostActivity;
import com.example.instasaver.extra.MyClipbord;
import com.example.instasaver.extra.Settings;

import java.util.Objects;

public class UserHomeActivity extends AppCompatActivity {
    private static final int STORE_IMAGE = 101;
    WebView webView;
    boolean isFirst=true;
    boolean isLoaded=false;
    String lastUrl="";
    public   boolean  isUserImageLoaded=false;
    public   boolean  isMineImage=true;
    private String username;
    private String loadLink;
    private String userimg;
    private  boolean extract=false;
    private  boolean isAuto=true;
    private TextView textView;
    private MyClipbord clipbord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("screen", "UserHomeActivity started");
        if (requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORE_IMAGE)) {
            initialize();
        }


    }
   void initialize(){
       clipbord = new MyClipbord(this);
       username=getSharedPreferences("SHARED_DATA",MODE_PRIVATE).getString("username","");
       userimg=getSharedPreferences("SHARED_DATA",MODE_PRIVATE).getString("userimage","no");
       assert username != null;
       if (username.isEmpty()){
           Intent intent= new Intent(this,Login.class);
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
           startActivity(intent);
       }else
       {
           Toolbar toolbar = findViewById(R.id.toolbar);
           setSupportActionBar(toolbar);
           webView = findViewById(R.id.webview);
           findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);
           loadLink="https://www.instagram.com/";
           loadUrl(loadLink);
           textView=findViewById(R.id.profilename);
           textView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   showDropDownList((TextView) view);
               }
           });
           textView.setText(username+" 's home");
           if (!Objects.requireNonNull(userimg).equals("no"))
           {      Log.e("UserHomeActivity","img loaded=>"+username);
               isUserImageLoaded=true;
               Glide.with(this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                       .load(userimg).into((ImageView) findViewById(R.id.profileimage));
           }

       }
    }
    private boolean requestPermission(Activity context, String permission, int value)  {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[]{permission},
                    value);

        }
        return hasPermission;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORE_IMAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                }
                break;
            }


        }
    }
    public void retryClicked(View view) {
        webView.loadUrl(loadLink);
        findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);
        findViewById(R.id.errorScreen).setVisibility(View.GONE);
    }
    void showDropDownList(TextView view){

        Log.e("Camera","Inside start_camera_dialog");
        final View dialogView = View.inflate( this, R.layout.settings_img_more_options, null);
        final Dialog dialog = new Dialog(Objects.requireNonNull(this),R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        final TextView title=dialogView.findViewById(R.id.dialogTitle);
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );

        dialog.setContentView(dialogView);

        Glide.with(this).load(userimg).apply(RequestOptions.circleCropTransform()).into((ImageView) dialogView.findViewById(R.id.profileimage));
        TextView textView1=dialogView.findViewById(R.id.profilename);
        textView1.setText(new StringBuilder().append("@").append(username).append("'s home").toString());
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();

                    return true;
                }

                return false;
            }
        });
        dialogView.findViewById(R.id.two_two).setOnClickListener(v -> {
            imgClicked(null);
            dialog.dismiss();

        });
        dialogView.findViewById(R.id.one).setOnClickListener(v -> {
            loadLink="https://www.instagram.com/";//+username+"/saved/";
            isMineImage=true;
            lastUrl=loadLink;
            extract=true;
            isAuto=true;
            loadUrl(loadLink);
            textView.setText(username);
            dialog.dismiss();
            Glide.with(UserHomeActivity.this).load(userimg).apply(RequestOptions.circleCropTransform()).into((ImageView) findViewById(R.id.profileimage));
            findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);

        });
        dialogView.findViewById(R.id.two_one).setOnClickListener(v -> {
            Intent intent=new Intent(UserHomeActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.two).setOnClickListener(view1 -> {
            showEditTextDialog("Paste Account Link","profile");
            dialog.dismiss();
//            title.setText("Paste Account Link");
//            dialogView. findViewById(R.id.optionLinear).setVisibility(View.GONE);
//            dialogView.findViewById(R.id.enterLinkLiner).setVisibility(View.VISIBLE);
//            dialogView.findViewById(R.id.loadAccountButton).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view1) {
//                    EditText editText=dialogView.findViewById(R.id.accountLink);
//                    loadLink=editText.getText().toString();
//                    Intent intent=new Intent(UserHomeActivity.this,MainActivity.class);
//                    intent.putExtra("url",loadLink);
//                    startActivity(intent);
//                    finish();
//                    dialog.dismiss();
//
//                }
//            });
        });
        dialogView.findViewById(R.id.three).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditTextDialog("Paste Post Link","post");
                dialog.dismiss();
//                title.setText("Paste Post Link");
//                dialogView. findViewById(R.id.optionLinear).setVisibility(View.GONE);
//                dialogView.findViewById(R.id.enterLinkLiner).setVisibility(View.VISIBLE);
//                dialogView.findViewById(R.id.loadAccountButton).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        EditText editText=dialogView.findViewById(R.id.accountLink);
//                        String loadLink=editText.getText().toString();
//                        if (loadLink.contains("igshid")){
//                            loadLink=loadLink.substring(0,loadLink.indexOf("?"));
//                        }
//                        Intent intent = new Intent(UserHomeActivity.this, NewSeePostActivity.class);
//                        intent.putExtra("url", loadLink);
//                        intent.putExtra("isAuto", false);
//                        startActivity(intent);
//
//                    }
//                });
            }
        });
        dialog.show();
    }

    void showEditTextDialog(String title,String action){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater=this.getLayoutInflater();
        View layout=inflater.inflate(R.layout.input_text_dialog_layout,null);
        alert.setView(layout);
        AlertDialog alertDialog =alert.show();

        ((TextView)layout.findViewById(R.id.dialogTitle)).setText(title);
        EditText editText=layout.findViewById(R.id.accountLink);
        layout.findViewById(R.id.loadAccountButton).setOnClickListener(v -> {
            switch (action){
                case "post":loadPost(editText);
                    break;
                case "profile":loadProfile(editText);
                    break;
            }
            alertDialog.dismiss();
        });

    }

    private void loadProfile(EditText editText) {
            String link=getValidLink(editText.getText().toString().trim(),false);
            if (link!=null){
            loadLink = link;

            Intent intent = new Intent(UserHomeActivity.this, MainActivity.class);
            intent.putExtra("url", loadLink);
            startActivity(intent);
            finish();
        }else Toast.makeText(this,"Invalid profile link",Toast.LENGTH_LONG).show();
    }
    String getValidLink(String url,boolean isPost){
        if (url.startsWith("https://www.instagram.com"))
            return url;
        else if (!isPost)
            return "https://www.instagram.com/"+url;
        return null;
    }
    private void loadPost(EditText editText) {
        String link=getValidLink(editText.getText().toString().trim(),true);
        if (link!=null){
            loadLink = link;
            if (loadLink.contains("igshid")) {
                loadLink = loadLink.substring(0, loadLink.indexOf("?"));
            }
            Intent intent = new Intent(UserHomeActivity.this, NewSeePostActivity.class);
            intent.putExtra("url", loadLink);
            intent.putExtra("isAuto", false);
            startActivity(intent);
        }else Toast.makeText(this,"Invalid post link",Toast.LENGTH_LONG).show();
    }


    void loadUrl(String url){
        webView.setWebViewClient(new Browse());
        if (Build.VERSION.SDK_INT>21){
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        }else{
            CookieManager.getInstance().setAcceptCookie(true);
        }
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(url);

    }

    @Override
    protected void onStart() {
        super.onStart();
        isLoaded=false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoaded=false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isLoaded=false;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (webView.canGoBack())
        {   Log.e("back","Sending back");
            webView.goBack();
        }
        else{Log.e("back","finishing");
        clipbord.removeClipListener();
        finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clipbord.removeClipListener();
    }

    public void logoutClicked(View view) {
        SharedPreferences sharedPreferences= getSharedPreferences("SHARED_DATA",MODE_PRIVATE);
        SharedPreferences.Editor edit=sharedPreferences.edit();
        edit.clear();
        edit.apply();
        Intent intent= new Intent(this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        webView.clearFormData();
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        CookieManager cookieManager= CookieManager.getInstance();
        cookieManager.removeSessionCookies(null);
        cookieManager.removeAllCookies(null);
        startActivity(intent);
    }

    public void imgClicked(View view) {
        startActivity(new Intent(this, PendingPostActivity.class));
    }

    public void settingsClicked(View view) {
        startActivity(new Intent(this, Settings.class));
    }

    private class Browse extends WebViewClient {
//        String url;
        String finalUrl;
        int count;

//        Browse(String url) {
//            this.url = url;
//        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (error.getErrorCode()!=-1 && error.getErrorCode()!=-2) {
                    findViewById(R.id.progressRelative).setVisibility(View.GONE);
                    findViewById(R.id.errorScreen).setVisibility(View.VISIBLE);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("LoginThread", "Error received " + error.getErrorCode());
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e("url", "page started url=>" + url);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e("url", "page finished url=>" + url);

            if (url.endsWith("comments/")){
                if (view.canGoBack())
                {   clipbord.storeDownload(url.replace("/comments/",""));
                Toast.makeText(UserHomeActivity.this,"Added into pending post",Toast.LENGTH_SHORT).show();
                    view.goBack();
                }

            }
            count++;
            finalUrl = url;
            if (url.equals(loadLink)||extract) {
                hideStuffs();
            }

        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e("url","page override url=>"+view.getUrl());
            //view.loadUrl(url);

            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private void hideStuffs() {
        extract=false;
        Log.e("main","hiding stuffs");
        Handler handler = new Handler();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                webView.evaluateJavascript("javascript:(function(){var img=''; " +
                        "try{img=document.getElementsByTagName('img')[0].src;"+
                        "document.getElementsByTagName('nav')[0].remove();" +
                        "document.getElementsByTagName('nav')[0].remove();" +
                        "document.getElementsByClassName(' HVbuG')[0].remove();" +
                        "document.getElementsByClassName('-vDIg')[0].remove();" +
                        "document.getElementsByClassName('_3dEHb')[0].remove();" +
                        "document.getElementsByClassName('fx7hk')[0].remove();" +
                        "document.getElementsByClassName('_6auzh')[0].remove();" +
                        "document.getElementsByTagName('footer')[0].remove();" +
                        "" +//zGtbP
                        "}catch(error){}" +
                        "try{document.getElementsByClassName('ku8Bn')[0].remove();}catch(error){console.log(error);}" +
                        "try{document.getElementsByClassName('Z_Gl2')[0].remove();}catch(error){console.log(error);}" +
                        "return (img);})();" +
                        "", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        findViewById(R.id.progressRelative).setVisibility(View.GONE);
                        s = s.replace("\"", "");
                        if (!isUserImageLoaded || !isMineImage) {
                            if (!s.isEmpty()) {
                                if(isMineImage) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("SHARED_DATA", MODE_PRIVATE);
                                    SharedPreferences.Editor edit = sharedPreferences.edit();

                                    userimg =s.replace("\"", "");
                                    edit.putString("userimage", userimg);
                                    edit.apply();
                                }else {
                                    extractName();
                                }
                                Log.e("UserHomeActivity", "img=>" + s);
                                isUserImageLoaded = true;
                                Glide.with(UserHomeActivity.this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                                        .load(s.replace("\"", "")).into((ImageView) findViewById(R.id.profileimage));
                            } else Log.e("UserHomeActivity", "img received empty s=>" + s);

                            isMineImage=true;
                        }else Log.e("Hiding stuffs","u="+isUserImageLoaded+" mine="+isMineImage);
                    }
                });
            }
        }, 2000);
    }

    private void extractName() {
        try {
            String name = loadLink.substring(loadLink.indexOf(".com") + 5, loadLink.indexOf("?"));
            lastUrl = "https://www.instagram.com/" + name + "/";
            loadLink = lastUrl;
            //isMineImage=true;
            textView.setText(name);
        }catch (Exception ignored){}
    }

    private void extractuserImg(String img) {
        // if (!isUserImageLoaded){
        webView.evaluateJavascript("javascript:(function(){" +
                "var img='';" +
                "try{document.getElementsByTagName('img')[0].src;" +
                "}catch(error){console.log('error'+error);" +
                "}" +
                "return (img);})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                s=s.replace("\"","");
                if (!s.isEmpty()){
                    SharedPreferences sharedPreferences= getSharedPreferences("SHARED_DATA",MODE_PRIVATE);
                    SharedPreferences.Editor edit=sharedPreferences.edit();
                    edit.putString("userimage",s.replace("\"",""));
                    edit.apply();
                    Log.e("UserHomeActivity","img=>"+s);
                    isUserImageLoaded=true;
                    Glide.with(UserHomeActivity. this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                            .load(s.replace("\"","")).into((ImageView) findViewById(R.id.profileimage));
                }else Log.e("UserHomeActivity","img received empty s=>"+s);
            }
        });
        // }else Log.e("UserHomeActivity","imgloaded");

    }
}

