package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class FullImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        // get intent data
        Intent i = getIntent();

        String getPhotoUrl = i.getExtras().getString("image");
        File file = new File(getPhotoUrl);
        System.out.println(getPhotoUrl);


        ImageView getIvPhoto = (ImageView) findViewById(R.id.full_image_view);

        Picasso.with(this) //
                .load(file) //
                .into(getIvPhoto);


    }
}
