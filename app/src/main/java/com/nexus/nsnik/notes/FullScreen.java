package com.nexus.nsnik.notes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class FullScreen extends AppCompatActivity {

    ImageView fullScreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        initilize();
    }

    private void initilize() {
        fullScreenImage = (ImageView)findViewById(R.id.fullScreenImage);
    }
}
