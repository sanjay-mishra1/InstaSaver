package com.example.instasaver.extra;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.instasaver.JsonExtractor;
import com.example.instasaver.R;
import com.example.instasaver.RecyclerUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class PendingPostActivity extends AppCompatActivity {

    private RecyclerUI.FilterAdapter adapter;
    private ArrayList<HashMap<String,Object>> postList;
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Pending downloads");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.white), PorterDuff.Mode.SRC_ATOP);
        requestQueue= Volley.newRequestQueue(this);
        postList=new ArrayList<>();
        initializeFromPreference();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    void initializeFromPreference(){
        SharedPreferences preferences=getSharedPreferences("PENDING",MODE_PRIVATE);
        Map<String, ?> allEntries =sortByValue((HashMap<String, Long>) preferences.getAll()) ;
        Log.e("Failed Downloads","->"+allEntries);
        int index=0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
           try {//String postLink= (String) entry.getValue();
                String postLink= (String) entry.getKey();

                Log.e("Link","->"+postLink);
               if (postLink.startsWith("https")) {
                   if (postLink.contains("?"))
                        postLink = postLink.substring(0, postLink.indexOf("?"));

                   HashMap<String, Object> map = new HashMap<>();
                   map.put("postLink", postLink);
                   postList.add(map);
                   sendRequest(postLink, index);
                   index++;
               }
           }catch (Exception e){e.printStackTrace();}
        }
        if (!postList.isEmpty())
            findViewById(R.id.deletePost).setVisibility(View.VISIBLE);
        adapter= new RecyclerUI.FilterAdapter(postList,getSupportFragmentManager(),this);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new RecyclerUI.GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
    // function to sort hashmap by values
    public HashMap<String, Long> sortByValue(HashMap<String, Long> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Long> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        // put data from sorted list to hashmap
        HashMap<String, Long> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Long> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
    public int dpToPx(int dp) {
        Resources r =  getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    private void sendRequest(final String url,int index) {
        String temp = url + "?__a=1";
        Log.e("VolleyURL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(temp, null,
                response -> {
                    try {
                        HashMap<String, Object> hashMap = new HashMap<>(Objects.requireNonNull(JsonExtractor.jsonToMap(response)));
                        HashMap<String, Object> data = (HashMap<String, Object>) ((HashMap) (hashMap.get("graphql"))).get("shortcode_media");
                        fetchData(data, url,index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley", "Notification Error response " + error.getMessage())
        );

        requestQueue.add(jsonObjectRequest);
    }
    private void fetchData(HashMap<String, Object> data, String url, int index) {
        HashMap<String,Object> owner= (HashMap<String, Object>) data.get("owner");
        String  profilename= (String) owner.get("username");
        String preview = null;
        ArrayList<String> media=new ArrayList<>();
        try {
            ArrayList<HashMap<String, HashMap<String, String>>> postMedia = (ArrayList<HashMap<String, HashMap<String, String>>>) ((HashMap)
                    (Objects.requireNonNull(data.get("edge_sidecar_to_children")))).get("edges");
            for (HashMap<String, HashMap<String, String>> link : postMedia) {
                media.add(link.get("node").get("display_url"));
            }
            preview=(String) data.get("display_url");
        }catch (Exception video){
            //for video
            video.printStackTrace();
            try {if (data.get("video_url") !=null)
            {   preview=((String) data.get("display_url"));
                media.add((String) data.get("video_url"));
            }
            else{
                preview=((String) data.get("display_url"));
                media.add((String) data.get("display_url"));
            }
            }catch (Exception ignored){
            }
        }
        HashMap<String,Object>map=postList.get(index);
        map.put("img",media.get(0));
        map.put("name",profilename);
        map.put("data",data);
        map.put("postLink",url);
        map.put("preview",preview);
        postList.set(index,map);
        adapter.notifyItemChanged(index);
        Log.e("Preview","name->"+profilename+"\nPreview->"+preview);
    }

    public void deleteAllPost(View view) {
        SharedPreferences preferences=getSharedPreferences("PENDING",MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.apply();
        postList.clear();
        adapter.notifyDataSetChanged();
        ((FloatingActionButton)view).hide();
    }
}
