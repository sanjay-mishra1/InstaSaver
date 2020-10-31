package com.example.instasaver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instasaver.extra.PendingPostActivity;
import com.example.instasaver.extra.Settings;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
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
    static public HashMap<String,Object>senderMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("screen","mainactivity started");
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
                textView=findViewById(R.id.profilename);
            if(getIntent().getStringExtra("url")==null)
            {
                loadLink="https://www.instagram.com/"+username+""+"/saved/";
                             textView.setText(username+" 's saved");
                if (!Objects.requireNonNull(userimg).equals("no"))
                {      Log.e("mainactivity","img loaded=>"+username);
                    isUserImageLoaded=true;
                    Glide.with(this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                            .load(userimg).into((ImageView) findViewById(R.id.profileimage));
                }
            }
            else {
                loadLink= getIntent().getStringExtra("url");
                Log.e("Recieved",""+loadLink);
                isMineImage=false;
                extractName();
                extract=true;
                isAuto=false;
//                isFirst=true;
            }
            loadUrl(loadLink);
             textView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     showDropDownList((TextView) view);
                 }
             });


        }
    }



    void showDropDownList(TextView view){

        Log.e("Camera","Inside start_camera_dialog");
        final View dialogView = View.inflate( this, R.layout.settings_img_more_options, null);
        final Dialog dialog = new Dialog(Objects.requireNonNull(this),R.style.Dialog1);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
         lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        final TextView title=dialogView.findViewById(R.id.dialogTitle);

        dialog.setContentView(dialogView);

        Glide.with(this).load(userimg).apply(RequestOptions.circleCropTransform()).into((ImageView) dialogView.findViewById(R.id.profileimage));
        TextView textView1=dialogView.findViewById(R.id.profilename);
        textView1.setText(new StringBuilder().append("@").append(username).append("'s saved").toString());
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
        Glide.with(this).load(R.drawable.ic_baseline_home_24)
                .into((ImageView) dialogView.findViewById(R.id.activity_img));
        dialogView.findViewById(R.id.two_one).setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,UserHomeActivity.class);
            startActivity(intent);
            finish();
            dialog.dismiss();

        });
        dialogView.findViewById(R.id.two_two).setOnClickListener(v -> {
            imgClicked(null);
            dialog.dismiss();

        });
        ((TextView)dialogView.findViewById(R.id.activity_txt)).setText("User Home");
        dialogView.findViewById(R.id.one).setOnClickListener(v -> {
            loadLink="https://www.instagram.com/"+username+"/saved/";
            isMineImage=true;
            lastUrl=loadLink;
            extract=true;
            isAuto=true;
            loadUrl(loadLink);
            textView.setText(username+" 's saved");
            dialog.dismiss();
            Glide.with(MainActivity.this).load(userimg).apply(RequestOptions.circleCropTransform()).into((ImageView) findViewById(R.id.profileimage));
            findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);

        });
        dialogView.findViewById(R.id.two).setOnClickListener(view1 -> {
            showEditTextDialog("Paste Account Link","self");
            dialog.dismiss();
//            title.setText("Paste Account Link");
//            dialogView. findViewById(R.id.optionLinear).setVisibility(View.GONE);
//            dialogView.findViewById(R.id.enterLinkLiner).setVisibility(View.VISIBLE);
//            dialogView.findViewById(R.id.loadAccountButton).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view1) {
//                    EditText editText=dialogView.findViewById(R.id.accountLink);
//                    loadLink=editText.getText().toString();
//                    isMineImage=false;
//                    extractName();
//                    extract=true;
//                    loadUrl(loadLink);
//                    dialog.dismiss();
//                    Glide.with(MainActivity.this).load(R.drawable.ic_person_add_black_24dp).apply(RequestOptions.circleCropTransform()).into((ImageView) findViewById(R.id.profileimage));
//                    isAuto=false;
//                    findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);
//
//                }
//            });


         });
        dialogView.findViewById(R.id.three).setOnClickListener(view12 -> {
            showEditTextDialog("Paste Post Link","new");
            dialog.dismiss();
//            title.setText("Paste Post Link");
//            dialogView. findViewById(R.id.optionLinear).setVisibility(View.GONE);
//            dialogView.findViewById(R.id.enterLinkLiner).setVisibility(View.VISIBLE);
//            dialogView.findViewById(R.id.loadAccountButton).setOnClickListener(view121 -> {
//                EditText editText=dialogView.findViewById(R.id.accountLink);
//                String loadLink=editText.getText().toString();
//                if (loadLink.contains("igshid")){
//                    loadLink=loadLink.substring(0,loadLink.indexOf("?"));
//                }
//                Intent intent = new Intent(MainActivity.this, NewSeePostActivity.class);
//                intent.putExtra("url", loadLink);
//                intent.putExtra("isAuto", false);
//                startActivity(intent);
//            });
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
                case "self":loadSelfAction(editText);
                            break;
                case "new":loadNewAction(editText);
                            break;
            }
            alertDialog.dismiss();
        });

    }

    private void loadNewAction(EditText editText) {
        String link=getValidLink(editText.getText().toString().trim(),true);
        if (link!=null){
            loadLink = link;
        if (loadLink.contains("igshid")){
            loadLink=loadLink.substring(0,loadLink.indexOf("?"));
        }
        Intent intent = new Intent(MainActivity.this, NewSeePostActivity.class);
        intent.putExtra("url", loadLink);
        intent.putExtra("isAuto", false);
        startActivity(intent);
        }else Toast.makeText(this,"Invalid post link",Toast.LENGTH_LONG).show();

    }
    String getValidLink(String url,boolean isPost){
        if (url.startsWith("https://www.instagram.com"))
            return url;
        else if (!isPost)
            return "https://www.instagram.com/"+url;
        return null;
    }
    private void loadSelfAction(EditText editText) {
        String link=getValidLink(editText.getText().toString().trim(),false);
        if (link!=null){
            loadLink = link;
        isMineImage=false;
        extractName();
        extract=true;
        loadUrl(loadLink);
        Glide.with(MainActivity.this).load(R.drawable.ic_person_add_black_24dp).apply(RequestOptions.circleCropTransform()).into((ImageView) findViewById(R.id.profileimage));
        isAuto=false;
        findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);
        }else Toast.makeText(this,"Invalid profile link",Toast.LENGTH_LONG).show();

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

    public void retryClicked(View view) {
        webView.loadUrl(loadLink);
        findViewById(R.id.progressRelative).setVisibility(View.VISIBLE);
        findViewById(R.id.errorScreen).setVisibility(View.GONE);
    }
    private class Browse extends WebViewClient {
        String url;
        String finalUrl;
        int count;

        Browse(String url) {
            this.url = url;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (error.getErrorCode()!=-1) {
                    findViewById(R.id.progressRelative).setVisibility(View.GONE);
                    findViewById(R.id.errorScreen).setVisibility(View.VISIBLE);
                }
            }
            Log.e("LoginThread", "Error received " + error);
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
            count++;
            finalUrl = url;
            if (url.equals(loadLink)||extract) {
                hideStuffs();
             }
                if (isFirst) {
                    isFirst = false;
                    if (!url.equals(loadLink)) {
                        if (!url.contains("igshid")) {
                            view.stopLoading();
                            Intent intent = new Intent(MainActivity.this, NewSeePostActivity.class);
                            intent.putExtra("url", url);
                            intent.putExtra("isAuto", isAuto);
                            lastUrl = url;
                            startActivity(intent);
                        } else hideStuffs();
                    }
                } else
                {
                    if (!url.equals(loadLink) & !isLoaded & !lastUrl.equals(url)) {
//                        view.stopLoading();
                        lastUrl = url;
                        isLoaded = true;
                        if (!url.contains("igshid")) {
                            Intent intent = new Intent(MainActivity.this, NewSeePostActivity.class);
                            intent.putExtra("url", finalUrl);
                            intent.putExtra("isAuto", isAuto);
                            startActivity(intent);
                            if (webView.canGoBack())
                                webView.goBack();
                        } else hideStuffs();
                    } else {
                        Log.e("Mainactivity", "same url=>" + url + " \nlast=>" + lastUrl);
                        //view.stopLoading();
                    }
                }

        }










        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e("url","page override url=>"+url);
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
                        Log.e("mainactivity", "img=>" + s);
                        isUserImageLoaded = true;
                        Glide.with(MainActivity.this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                                .load(s.replace("\"", "")).into((ImageView) findViewById(R.id.profileimage));
                    } else Log.e("mainactivity", "img received empty s=>" + s);

                    isMineImage=true;
                }else Log.e("Hiding stuffs","u="+isUserImageLoaded+" mine="+isMineImage);
            }
        });
            }
        }, 2000);
    }

    private void extractName() {
       try {
           String   username;
           if (loadLink.contains("?"))
             username = loadLink.substring(loadLink.indexOf(".com") + 5, loadLink.indexOf("?"));
           else username = loadLink.substring(loadLink.indexOf(".com") + 5);
           if (username.endsWith("/"))
               username=username.substring(0,username.lastIndexOf("/"));
           lastUrl = "https://www.instagram.com/" + username+ "/";
           loadLink = lastUrl;
           textView.setText(username);
       }catch (Exception e){e.printStackTrace();}
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
                        Log.e("mainactivity","img=>"+s);
                        isUserImageLoaded=true;
                        Glide.with(MainActivity. this).applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                                .load(s.replace("\"","")).into((ImageView) findViewById(R.id.profileimage));
                    }else Log.e("mainactivity","img received empty s=>"+s);
                }
            });
       // }else Log.e("mainactivity","imgloaded");

    }
}

