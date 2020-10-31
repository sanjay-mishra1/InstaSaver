package com.example.instasaver.extra;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instasaver.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ImgLinkFragment extends BottomSheetDialogFragment {
    private final String img;
    private final String name;

    public ImgLinkFragment(String img,String name){
        this.img=img;
        this.name=name;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_link_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView=view.findViewById(R.id.textview);
        textView.setText(img);
        view.findViewById(R.id.copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)getActivity(). getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Insta post", img+"->"+name);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
        });
    }
}
