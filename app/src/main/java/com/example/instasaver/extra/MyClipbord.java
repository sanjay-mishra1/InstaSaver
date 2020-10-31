package com.example.instasaver.extra;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.instasaver.JsonExtractor;
import com.example.instasaver.R;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class MyClipbord  {
    private final Activity context;
    private final DownloadManager downloadManager;
    private RequestQueue requestQueue;
    private SharedPreferences preferences;
    private String post;
    private String previous ="";
    public MyClipbord(Activity context){
        this.context=context;
        requestQueue= Volley.newRequestQueue(context);
        downloadManager =(DownloadManager) context. getSystemService(Context.DOWNLOAD_SERVICE);
        preferences=context.getSharedPreferences("PENDING",MODE_PRIVATE);
        loadClipboardListener();
    }



    void loadClipboardListener(){
     try {Log.e("Clipbaord","Clip method");
         final ClipboardManager clipboardManager = (ClipboardManager)
                context. getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
         clipboardManager.addPrimaryClipChangedListener(() -> {
             Log.e("Clip","Copied");
             post=clipboardManager.getText().toString();
               try {
                   if (!previous.equals(post))
                   {   previous=post;
                       if (post.contains(".jpg"))
                       {   String name="",link;
                           if(post.contains("->"))
                           {
                               String[] data=post.split("->");name=data[1]+"_";link=data[0];
                           }else link=post;
                           download(link,name+link.substring(link.lastIndexOf("/")+1, link.lastIndexOf("?")),link);
                       }
                       else
                           downloadPost(post);

                   }
               }catch (Exception e){e.printStackTrace();}
         });
     }catch (Exception e){e.printStackTrace();}
    }

  public  void downloadPost(String img){
      try {
          if (img.startsWith("https")) {
              img = img.substring(0, img.indexOf("?"));
              sendRequest(img);
          }
      }catch (Exception e){e.printStackTrace();}
    }


    private void sendRequest(final String url) {
        String temp=url+"?__a=1";
        Log.e("VolleyURL",url);
        storeDownload(url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(temp, null,
                response -> {
                    try {
                        HashMap<String, Object> hashMap = new HashMap<>(Objects.requireNonNull(JsonExtractor.jsonToMap(response))) ;
                        HashMap<String,Object> data =(HashMap<String, Object>) ((HashMap)( hashMap.get("graphql"))).get("shortcode_media") ;
                        fetchData(data,url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley","Notification Error response "+error.getMessage())
        );

        requestQueue.add(jsonObjectRequest);

    }
   public void storeDownload(String url){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putLong(url,System.currentTimeMillis());
        editor.apply();
    }
    void removeDownload(String url){
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(url);
        editor.apply();
    }
    private void fetchData(HashMap<String, Object> data, String url) {
        HashMap<String,Object> owner= (HashMap<String, Object>) data.get("owner");
        String  profilename= (String) owner.get("username");
        ArrayList<String>media=new ArrayList<>();
        try {
            ArrayList<HashMap<String, HashMap<String, String>>> postMedia = (ArrayList<HashMap<String, HashMap<String, String>>>) ((HashMap)
                    (Objects.requireNonNull(data.get("edge_sidecar_to_children")))).get("edges");
            for (HashMap<String, HashMap<String, String>> link : postMedia) {
                media.add(link.get("node").get("display_url"));
            }
        }catch (Exception video){
            //for video
            video.printStackTrace();
            try {if (data.get("video_url") !=null)
                media.add((String) data.get("video_url"));
            else media.add((String) data.get("display_url"));
            }catch (Exception ignored){
            }
        }
        downloadAll(media,profilename,url);
    }

    void downloadAll(ArrayList<String> media, String profilename, String link){
            if (profilename == null)
                profilename = "";
            Toast.makeText(context,"Downloading "+media.size()+" images",Toast.LENGTH_SHORT).show();
            for (String url : media) {
                try {
                    download(url, profilename + "_" + url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("?")),link);
                } catch (Exception ignored) {
                }
            }
    }

    public void download(String url, String name, String link) {

        Uri Download_Uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        context.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
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
        SharedPreferences preferences=context.getSharedPreferences("settings",MODE_PRIVATE);
        String dir=(preferences.getString("download",""));
        Log.e("Download dir","->"+Environment.DIRECTORY_MOVIES);
//        request.setDestinationUri(dir);
//        request.setVisibleInDownloadsUi(true);
//        request.setDestinationInExternalFilesDir(context,dir,name);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, name);
        //Enqueue a new download and same the referenceId
        downloadManager.enqueue(request);
        removeDownload(link);
    }

    android.content.BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
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
                String stringBuilder2 = context.getPackageName() +
                        ".provider";
                attachmentUri = FileProvider.getUriForFile(context, stringBuilder2, file);
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
