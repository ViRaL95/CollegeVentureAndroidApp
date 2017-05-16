package com.example.varun.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * The following DisplayProject class was used to retrieve the bitmap that was sent to it which was
 * retrieved from Amazon s3 and was then displayed on an imageview
 */
public class DisplayProject extends AppCompatActivity {
private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_project);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        Bitmap bitmap=(Bitmap)bundle.get("imagename");
        imageView=(ImageView) findViewById(R.id.displayImage);
        imageView.setImageBitmap(bitmap);

    }
}
