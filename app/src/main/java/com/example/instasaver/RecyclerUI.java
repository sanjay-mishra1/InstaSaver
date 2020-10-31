package com.example.instasaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

  public class RecyclerUI {
    private   FilterAdapter adapter;
    private Context context;
    private List<String> PostList;
    private List<String> urls;

       RecyclerUI(Context context, RecyclerView recyclerView, ArrayList<String> urls){
        this.context=context;
        PostList=new ArrayList<>();
       // adapter= new FilterAdapter(PostList);
        this.urls=urls;



//         RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
//         recyclerView.setLayoutManager(mLayoutManager);
//         recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
//         recyclerView.setItemAnimator(new DefaultItemAnimator());
//         recyclerView.setAdapter(adapter);

         setRecyclerUI( );

    }
    private void setRecyclerUI(){
         for (String data:urls)
        {   if (!data.equals(",stop_loop"))
            PostList.add(data);
            adapter.notifyDataSetChanged();
        }
     }

    public static class FilterAdapter extends RecyclerView.Adapter<ViewHolder> {
        private  ArrayList<String> videoPreviewList;
        private  FragmentManager manager;
        List<String> dataList;
        ArrayList<HashMap<String, Object>> postList;;
        Context context;
        boolean fromPending;
        private int layout;
        private SharedPreferences preference;

        public FilterAdapter(ArrayList<String> data,ArrayList<String> videoPreviewList, Context context, FragmentManager manager){
            this.dataList=data;
            fromPending=false;
            this.context=context;
            this.manager=manager;
            this.videoPreviewList=videoPreviewList;
            layout=R.layout.img_layout;
        }

        public FilterAdapter(ArrayList<HashMap<String, Object>> postList, FragmentManager manager, Context context) {
            this.postList=postList;
            this.context=context;
            this.manager=manager;
            fromPending=true;
            layout=R.layout.img_layout_with_name;
            preference= context.getSharedPreferences("PENDING",Context.MODE_PRIVATE);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder( LayoutInflater.from(context).inflate(layout,viewGroup,false));

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            if (fromPending)
            {
                HashMap<String, Object> imgs=postList.get(position);
                String img= (String) imgs.get("img");
                String postLink= (String) imgs.get("postLink");

                if (img!=null)
                {   String name=(String)imgs.get("name");
                    viewHolder.setName(name);
                    String preview=null;
                    try {
                    preview= (String) imgs.get("preview");
                    }catch (Exception e){
                        Log.e("Preview-Error","Not found "+name+" pos "+postLink);
                    }
                    viewHolder.setImg(img,preview, (HashMap<String, Object>) imgs.get("data"),postLink,context,name);
                }
                viewHolder.setLongPress(postLink, manager,(String)imgs.get("name"));
                viewHolder.itemView.findViewById(R.id.delete).setOnClickListener(v -> {
                    try {
                        SharedPreferences.Editor editor=preference.edit();
                        editor.remove(postLink);
                        editor.apply();
                        int index = postList.indexOf(imgs);
                        postList.remove(index);
                        notifyItemRemoved(index);
                    }catch (Exception e){e.printStackTrace();}
                });
            }else{
                String img=dataList.get(position);
                viewHolder.setLongPress(img, manager, NewSeePostActivity.profilename);
                viewHolder.setImg((ArrayList<String>) dataList,videoPreviewList,context,position);
            }

        }

        @Override
        public int getItemCount() {
            if (dataList==null&&postList==null)
                return 0;
            else
            {
                if (fromPending)
                   return postList.size();
                else
                return (int) dataList.size();
            }
        }
    }
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

          public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            }
            else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */

}
