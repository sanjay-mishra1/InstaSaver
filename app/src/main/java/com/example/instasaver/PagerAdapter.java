package com.example.instasaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;

public class PagerAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final Context context;
    ArrayList<String>images;
    public PagerAdapter(ArrayList<String>images, Context context){
        this.images=images;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder( LayoutInflater.from(context).inflate(R.layout.image_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       ZoomageView imageView= holder.itemView.findViewById(R.id.imageview);
        Glide.with(holder.itemView).load(images.get(position)).into(imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
