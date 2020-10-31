package com.example.instasaver;

import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instasaver.extra.ImgLinkFragment;
import com.example.instasaver.extra.VideoPlayerActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;


class ViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    View view;
    ViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view=itemView;
        imageView=view.findViewById(R.id.img);
    }

    void setImg(final ArrayList<String> img,ArrayList<String> previewImg, final Context context,int position) {

        String preview = null;
        if (!previewImg.isEmpty()) {
            preview = previewImg.get(position);
            Glide.with(view).applyDefaultRequestOptions(RequestOptions.noTransformation()).load(preview).into(imageView);
        }else Glide.with(view).applyDefaultRequestOptions(RequestOptions.noTransformation()).load(img.get(position)).into(imageView);
        if (img.get(position).contains(".mp4"))
            itemView.findViewById(R.id.video_media).setVisibility(View.VISIBLE);
        String finalPreview = preview;
        imageView.setOnClickListener(view -> {
            Intent intent;
            if (img.get(position).contains(".mp4"))
            {
                intent = new Intent(context, VideoPlayerActivity.class);
                  intent.putExtra("preview", finalPreview);
                intent.putExtra("video", img.get(position));
            }else {
                intent = new Intent(context, ImageViewerActivity.class);
                intent.putStringArrayListExtra("list", img);
                if (!previewImg.isEmpty())
                intent.putStringArrayListExtra("preview_list", previewImg);
            }
            intent.putExtra("position", position);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            (context).startActivity(intent);
            Log.e("Viewholder","From first setImg method");
        });
    }
    void setImg(String img, String preview, HashMap<String, Object> data, String url, Context context, String name) {
        if (img!=null) {
            if (preview!=null)
            Glide.with(view).load(preview).into(imageView);
            else
            Glide.with(view).load(img).into(imageView);
            if (img.contains(".mp4"))
                itemView.findViewById(R.id.video_media).setVisibility(View.VISIBLE);
            imageView.setOnClickListener(view -> {
                Intent intent;
                if (img.contains(".mp4")){
                    NewSeePostActivity.profilename=name;
                     intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("video", img);
                    intent.putExtra("preview", preview);
                }else {
                    Log.e("CLickListener", "img-> " + img + "\ndata-> " + data);
                    Log.i("url-> ", url + "\ncontext->" + context);
                     intent = new Intent(context, NewSeePostActivity.class);
                    MainActivity.senderMap = data;

                    intent.putExtra("isAuto", false);
                    intent.putExtra("url", url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                (context).startActivity(intent);
                Log.e("Viewholder","From second setImg method");
            });
        }
    }
    public void setLongPress(String s, FragmentManager manager, String name) {
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BottomSheetDialogFragment fragment=new ImgLinkFragment(s,name);
                fragment.show(manager,"");
                return false;
            }
        });
    }

    public void setName(String name) {
        if (name!=null){
            ((TextView)itemView.findViewById(R.id.profilename)).setText(name);
        }
    }
}
