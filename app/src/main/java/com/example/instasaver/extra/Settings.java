package com.example.instasaver.extra;

import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instasaver.R;

import java.io.File;


public class Settings  extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        Switch aSwitch=findViewById(R.id.clipboard);
        TextView addressText=findViewById(R.id.storeAddress);
         sharedPreferences=getSharedPreferences("settings",MODE_PRIVATE);
        String path=sharedPreferences.getString("download","/Downloads");
        addressText.setText(path);

        findViewById(R.id.addresscard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent,
                        101);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            try {
                Log.e("Received tree",""+data.getData());
            }catch (Exception e){e.printStackTrace();}
            SharedPreferences.Editor edit=sharedPreferences.edit();
            String folderLocation = String.valueOf(data.getData());
            Log.i( "folderLocation", folderLocation );
            edit.putString("download", folderLocation);
            edit.apply();
        }
    }

}
