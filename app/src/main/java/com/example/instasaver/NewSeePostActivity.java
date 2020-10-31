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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class NewSeePostActivity extends AppCompatActivity {
    private   final int STORE_IMAGE = 103;
    WebView webView;
    ArrayList<String>media;
    private DownloadManager downloadManager;
    private RecyclerView recyclerView;
    private RecyclerUI.FilterAdapter adapter;
    boolean isSaved=false;
    public static String profilename;
    private boolean isAuto;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String url;
    private boolean pageFailed=false;
    private String profileimage;
    private ArrayList<String> videoPreviewImgList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_post);
        swipeRefreshLayout=findViewById(R.id.swiprefresh);
        requestQueue= Volley.newRequestQueue(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        media=new ArrayList<>();
        videoPreviewImgList=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerview);
        adapter= new RecyclerUI.FilterAdapter(media,videoPreviewImgList,this,getSupportFragmentManager());
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

        swipeRefreshLayout.setOnRefreshListener(() -> onRefreshDone());
    }
    void receive(){
        swipeRefreshLayout.setRefreshing(true);
        Intent intent=getIntent();
        url=intent.getStringExtra("url");
        isAuto=intent.getBooleanExtra("isAuto",true);
        HashMap<String,Object> data = MainActivity.senderMap;
        if (data==null)
            sendRequest(url);
        else {
            swipeRefreshLayout.setRefreshing(false);
            fetchData(data);
            MainActivity.senderMap=null;
        }
    }

    private void sendRequest(final String url) {
        String temp=url+"?__a=1";
        Log.e("VolleyURL",url);
        pageFailed=false;
        if (findViewById(R.id.saved).getVisibility()==View.VISIBLE) {
            findViewById(R.id.saved).setVisibility(View.GONE);
        }
        loadUrl(url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(temp, null,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    findViewById(R.id.save_progress).setVisibility(View.VISIBLE);
                    try {
                        HashMap<String, Object> hashMap = new HashMap<>(Objects.requireNonNull(JsonExtractor.jsonToMap(response))) ;
                        HashMap<String,Object> data =(HashMap<String, Object>) ((HashMap)( hashMap.get("graphql"))).get("shortcode_media") ;
                        fetchData(data);
                    } catch (Exception e) {
                        findViewById(R.id.save_progress).setVisibility(View.VISIBLE);
                        pageFailed=true;
                        swipeRefreshLayout.setRefreshing(false);
//                            Toast.makeText(NewSeePostActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        findViewById(R.id.save_progress).setVisibility(View.VISIBLE);
                        pageFailed=true;
                        swipeRefreshLayout.setRefreshing(false);
//                        Toast.makeText(NewSeePostActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
                        Log.e("Vooley","Notification Error response "+error.getMessage());
                    }

                }
        );

        requestQueue.add(jsonObjectRequest);

    }

    private void fetchData(HashMap<String, Object> data) {
        try {
            HashMap<String, Object> owner = (HashMap<String, Object>) data.get("owner");
            profilename = (String) owner.get("username");
            profileimage = (String) owner.get("profile_pic_url");
            int time = (int) data.get("taken_at_timestamp");
            long likes;
            try {
                likes = (long) ((HashMap) (Objects.requireNonNull(data.get("edge_media_preview_like"))))
                        .get("count");
            } catch (Exception e) {
                likes = (int) ((HashMap) (Objects.requireNonNull(data.get("edge_media_preview_like")))).get("count");
            }
            boolean isSave = (boolean) data.get("viewer_has_saved");
            //ArrayList<String> allMedia=new ArrayList<>();
            try {
                ArrayList<HashMap<String, HashMap<String, Object>>> postMedia = (ArrayList<HashMap<String, HashMap<String, Object>>>) ((HashMap)
                        (Objects.requireNonNull(data.get("edge_sidecar_to_children")))).get("edges");
                for (HashMap<String, HashMap<String, Object>> link : postMedia) {
//                    String i= (String) link.get("node").get("display_url");
                   String originalData= fetchBestDataAvailable(link);
                    Log.e("Best_Media",""+originalData);
                    media.add(originalData);
                    videoPreviewImgList.add((String) link.get("node").get("display_url"));
                }
            } catch (Exception video) {
                //for video
                video.printStackTrace();
                try {
                    if (data.get("video_url") != null)
                    {  videoPreviewImgList.add((String) data.get("display_url"));
                        media.add((String) data.get("video_url"));
                    }
                    else{
                        videoPreviewImgList.add((String) data.get("display_url"));
                        media.add((String) data.get("display_url"));
                    }
                } catch (Exception ignored) {
                }
            }
            TextView textView = findViewById(R.id.profilename);
            textView.setText(profilename);
            TextView timeTextView = findViewById(R.id.postTime);
            timeTextView.setText(timestampToTime(time));
            TextView likeTextView = findViewById(R.id.postLikes);
            likeTextView.setText("   " + getFormatedAmount(likes) + " likes");
            likeTextView.setVisibility(View.VISIBLE);
            Glide.with(NewSeePostActivity.this)
                    .applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                    .load(profileimage).into((ImageView) findViewById(R.id.profileimage));

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new RecyclerUI.GridSpacingItemDecoration(2, dpToPx(10), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            Log.e("Data","Received and fetched \n"+media);
        }catch (Exception e){e.printStackTrace();}
    }

    private String fetchBestDataAvailable(HashMap<String, HashMap<String, Object>> link) {
        Log.e("Data_of_post",""+link);
        try {
            if ((boolean)link.get("node").get("is_video"))

         return (String) link.get("node").get("video_url");
            else return (String) link.get("node").get("display_url");
        }catch (Exception e){

        }
        return null;
    }

    private String getFormatedAmount(long amount){
        return ""+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
    private String timestampToTime(int time) {
        Timestamp stamp = new Timestamp(time);
        Date date = new Date(time*1000L);
        DateFormat f = new SimpleDateFormat("MMM dd-yyyy");
        return f.format(date);
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
//        if(isAuto)
        {
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
                Log.e("Backpress","issaved false, executing script");
                webView.evaluateJavascript("javascript:(function(){" +
                        "document.getElementsByClassName('wmtNn')[0].getElementsByTagName('button')[0].click();" +
                        "" +
                        "return ('done');})();", null);
            }else  Log.e("Backpress","issaved true");
        }else{
            if (isSaved) {Log.e("Backpress","auto true issaved true");
                webView.evaluateJavascript("javascript:(function(){" +
                        "document.getElementsByClassName('wmtNn')[0].getElementsByTagName('button')[0].click();" +
                        "" +
                        "return ('done');})();", null);
            }else Log.e("Backpress","auto false issaved not true");
        }
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRefreshDone() {
        try {
            media.clear();
            adapter.notifyDataSetChanged();
            Log.e("refresh","sended");
            sendRequest(url);
        }catch (Exception e){e.printStackTrace();}

    }
    public void downloadProfileImage(View view) {
        Intent intent=new Intent(this,ImageViewerActivity.class);
        intent.putExtra("profile",true);
        intent.putExtra("img",profileimage);
        if (profileimage!=null)
            startActivity(intent);
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
            Log.e("URL","Page "+url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(pageFailed)
            {   Log.e("Fetch","From webview");
                activatemediaExtractor();
            }else Log.e("Fetch","From volley");
            webView.evaluateJavascript("javascript:(function(){" +
                    "var data=document.getElementsByClassName('wmtNn')[0].innerHTML.includes('Remove');" +
                    "return (data);})();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.e("dataNew",s);
                }
            });

            webView.evaluateJavascript(script2, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.e("WebViewdata",s);
                    try {
                        String data[]=s.split(",");

                        if(data[4].equals("true") ){
                            isSaved=true;
                            isAuto=false;
                            Glide.with(NewSeePostActivity.this).load(getResources().getDrawable(R.drawable.ic_bookmark_done)).into((ImageView) findViewById(R.id.saved));
                        }
                        findViewById(R.id.save_progress).setVisibility(View.GONE);
                        findViewById(R.id.saved).setVisibility(View.VISIBLE);

                    }catch (Exception ignored){}
                }
            });
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
                    if (data[1]!=null)
                        profileimage=data[1].replace("\"","");
                    Glide.with(NewSeePostActivity.this)
                            .applyDefaultRequestOptions(RequestOptions.circleCropTransform())
                            .load(profileimage).into((ImageView) findViewById(R.id.profileimage));
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
                        Glide.with(NewSeePostActivity.this).load(getResources().getDrawable(R.drawable.ic_bookmark_done)).into((ImageView) findViewById(R.id.saved));
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
                String stringBuilder2 = NewSeePostActivity.this.getPackageName() +
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

