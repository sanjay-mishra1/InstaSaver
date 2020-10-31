package com.example.instasaver.extra;

import android.content.Context;
import android.media.MediaController2;
import android.os.Build;
import android.widget.MediaController;

import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;

import com.example.instasaver.R;

public class CustomMediaController extends MediaController {
    public CustomMediaController(Context context) {
        super(new ContextThemeWrapper(context, R.style.Theme_MusicPlayer));
    }
}
