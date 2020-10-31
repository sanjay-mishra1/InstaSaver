package com.example.instasaver;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class SeePostActivity extends AppCompatActivity {
    private   final int STORE_IMAGE = 103;
    WebView webView;
    ArrayList<String>media;
    boolean mediaLoaded=false;
    private DownloadManager downloadManager;
    private RecyclerView recyclerView;
    private RecyclerUI.FilterAdapter adapter;
    boolean isSaved=false;
    public static String profilename;
    private boolean isAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        media=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerview);
        adapter= new RecyclerUI.FilterAdapter(media,null,getApplicationContext(),getSupportFragmentManager());
        webView=findViewById(R.id.webview);
        receive();
        downloadManager =(DownloadManager)  getSystemService(Context.DOWNLOAD_SERVICE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAll();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    void receive(){
        Intent intent=getIntent();
        isAuto=intent.getBooleanExtra("isAuto",true);



        loadUrl(intent.getStringExtra("url"));
    }

    private void loadUrl(String url) {
        webView.setWebViewClient(new  Browse(url));
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

    public void savedClicked(View view) {
        if(isAuto) {
            if (isSaved) {
                isSaved = false;
                Glide.with(this).load(getResources().getDrawable(R.drawable.ic_bookmark_border_not_done)).into((ImageView) view);
            } else {
                isSaved = true;
                Glide.with(this).load(getResources().getDrawable(R.drawable.ic_bookmark_done)).into((ImageView) view);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isAuto) {
            if (!isSaved) {
                webView.evaluateJavascript("javascript:(function(){" +
                        "document.getElementsByClassName('wmtNn')[0].getElementsByTagName('button')[0].click();" +
                        "" +
                        "return ('done');})();", null);
            }
        }else{
            if (isSaved) {
                webView.evaluateJavascript("javascript:(function(){" +
                        "document.getElementsByClassName('wmtNn')[0].getElementsByTagName('button')[0].click();" +
                        "" +
                        "return ('done');})();", null);
            }
        }
//        attachmentDownloadCompleteReceive.abortBroadcast();
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            // This ID represents the Home or Up button.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void downloadProfileImage(View view) {

    }

    private class Browse extends WebViewClient {
        String url;
        Browse(String url){
            this.url=url;
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            activatemediaExtractor();





        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, request);
        }
    }
   String script1= "javascript:(function(){" +
            "var data=document.getElementsByClassName('FPmhX notranslate nJAzx')[0].title;" +
            "data=data+','+document.getElementsByTagName('img')[0].src;" +
            "var result=document.getElementsByTagName('video')[0]==null;" +
            "data=data+','+result;" +
            "data=data+','+document.getElementsByClassName('wmtNn')[0].innerHTML.includes('Remove');" +
            "return (data);})();";
    String script2="javascript:(function(){" +
            "var data=document.getElementsByClassName('e1e1d')[0].textContent;" +
            "data=data+','+document.getElementsByTagName('img')[0].src;" +
            "var result=document.getElementsByTagName('video')[0]==null;" +
            "data=data+','+result;" +
            "data=data+','+document.getElementsByTagName('time')[0].title.replace(/,/g,'-');" +
            "data=data+','+document.getElementsByClassName('wmtNn')[0].innerHTML.includes('Remove');" +
            "try{data=data+','+document.getElementsByClassName('Nm9Fw')[0].textContent.replace(/,/g,'-');}" +
            "catch(error){data+','+document.getElementsByClassName('vcOH2')[0].textContent.replace(/,/g,'-');}" +
            "return (data);})();";
    void activatemediaExtractor(){
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new RecyclerUI.GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        webView.evaluateJavascript(script2, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.e("data",s);
                try {
                    TextView textView=findViewById(R.id.profilename);
                    TextView timeTextView=findViewById(R.id.postTime);
                    TextView likeTextView=findViewById(R.id.postLikes);

                    String data[]=s.split(",");
                    Log.e("data",Arrays.toString(data));
                    profilename=data[0].replace("\"","").replace("Verified","");
                    String time=data[3].replace("\"","").replace("-",",");
                    if (!profilename.equals("null"))
                        textView.setText(profilename);
                    Glide.with(SeePostActivity.this)
                            .applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                            .load(data[1].replace("\"","")).into((ImageView) findViewById(R.id.profileimage));
                    if (!time.equals("null"))
                        timeTextView.setText(time);
                    else timeTextView.setVisibility(View.GONE);

                    if (data[2].equals("true")) {
                        getImg(true, "work");
                    }
                    else {
                        getImgAndVideo(true,true, "work");
                    }
                    if(!isAuto && data[4].equals("true") ){
                        isSaved=true;
                        Glide.with(SeePostActivity.this).load(getResources().getDrawable(R.drawable.ic_bookmark_done)).into((ImageView) findViewById(R.id.saved));
                    }

                    if (data[5].contains("Like") && data[5].contains("and"))
                        likeTextView.setText(String.format("   %s likes", data[5].substring(data[5].indexOf("and") + 3)).replace("\"","").replace("-",",").replace("others",""));
                    else likeTextView.setText(String.format("   %s", data[5].replace("\"", "").replace("-", ",")));
                    likeTextView.setVisibility(View.VISIBLE);
                }catch (Exception ignored){}
            }
        });
//        getImg(true,"work");

    }

    private int dpToPx(int dp) {
        Resources r =  getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    void getImg(final boolean isFirst, String output){
        int index;
        if (isFirst){
            index=0;
        }else index=1;
        //if (  !output.isEmpty()) {
        webView.evaluateJavascript("javascript:(function(){" +
                "var img='';" +
                "try{img=document.getElementsByClassName('FFVAD')[" + index + "].src;" +
                "document.getElementsByClassName('    coreSpriteRightChevron')[0].click();}" +
                "catch(error){img=img+',stop_loop'}" +
                "return (img);})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String s) {
                Log.e("output", s);


                try{if (s.length()>20)
                {   if (s.contains("stop_loop"))
                    media.add(s.replace(",stop_loop","").replace("\"",""));
                else
                    media.add(s.replace("\"",""));
                    adapter.notifyDataSetChanged();
                }
                }catch (Exception ignored){}
                if (!s.contains("stop_loop")) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            getImg(false, "work");
                        }
                    }, 1000);
                }
            }
        });


    }

    void getImgAndVideo(final boolean isFirstImg,final boolean isFirstVideo, String output){
        final int index;
        int video;
        if (isFirstImg){
            index=0;
        }else index=1;
        if (isFirstVideo){
            video=0;
        }else video=1;
        //if (  !output.isEmpty()) {
        webView.evaluateJavascript("javascript:(function(){" +
                "var data='';" +
                "        try{" +
                "           data=document.getElementsByClassName('FFVAD')[" + index + "].src; " +
                "           document.getElementsByClassName('    coreSpriteRightChevron')[0].click();} " +
                "       catch(error){" +
                "      try{" +
                "        data=document.getElementsByClassName('_5wCQW')[" + video + "].getElementsByTagName('video')[0].src;" +
                "       document.getElementsByClassName('    coreSpriteRightChevron')[0].click();" +
                "  }catch(error){" +
                "   " +
                "   data=data+',stop_loop';}" +
                "  }" +
                "       " +
                "return (data);})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String s) {
                Log.e("output", s);

                if (!media.contains(s)) {
                    try {
                        if (s.length() > 20) {
                            if (s.contains("stop_loop"))
                                media.add(s.replace(",stop_loop", "").replace("\"", ""));
                            else
                                media.add(s.replace("\"", ""));


                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception ignored) {
                    }
                    if (!s.contains("stop_loop")) {

                        Handler handler = new Handler();


                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                boolean isimg = isFirstImg;
                                boolean isvideo = isFirstVideo;
                                if (!s.contains(".mp4"))
                                    isimg = false;
                                else isvideo = false;
                                getImgAndVideo(isimg, isvideo, "work");
                            }
                        }, 2000);
                    }else{
                        Log.e("Media",media.toString());
                    }
                }else{
                    Log.e("Contains img","s "+s);
                    if (index==1){
                        boolean isimg = isFirstImg;
                        boolean isvideo = isFirstVideo;
                        if (!s.contains(".mp4"))
                            isimg = false;
                        else isvideo = false;
                        getImgAndVideo(isimg, isvideo, "work");
                    }
                }
            }
        });


    }

    void downloadAll(){
        if (requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, STORE_IMAGE)) {
            if (profilename == null)
                profilename = "";
            Toast.makeText(this,"Downloading "+media.size()+" images",Toast.LENGTH_SHORT).show();
            for (String url : media) {
                try {
                    download(url, profilename + "_" + url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("?")));
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void download(String url, String name) {

        Uri Download_Uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        this.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle(name);
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Downloading");
        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        //Enqueue a new download and same the referenceId
        downloadManager.enqueue(request);
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

                    downloadAll();



                }
                break;
            }


        }
    }
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                openDownloadedAttachment(context, downloadId);
            }
        }
    };

    private void openDownloadedAttachment(final Context context, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                try{
                    openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType);
                }catch (Exception e){e.printStackTrace();}
            }
        }
        cursor.close();
    }

    private void openDownloadedAttachment(final Context context, Uri attachmentUri, final String attachmentMimeType) {
        if (attachmentUri != null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE.equals(attachmentUri.getScheme())) {
                // FileUri - Convert it to contentUri.
                File file = new File(attachmentUri.getPath());
                String stringBuilder2 = SeePostActivity.this.getPackageName() +
                        ".provider";
                attachmentUri = FileProvider.getUriForFile(this, stringBuilder2, file);
            }

            Intent openAttachmentIntent = new Intent(Intent.ACTION_VIEW);
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType);
            openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(openAttachmentIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, context.getString(R.string.unable_to_open_file), Toast.LENGTH_LONG).show();
            }
        }

    }
}
