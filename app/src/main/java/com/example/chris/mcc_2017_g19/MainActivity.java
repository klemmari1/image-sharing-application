package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView gallery;
    ImageView groupManagement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gallery = (ImageView)findViewById(R.id.gallery);
        gallery.setClickable(true);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
                startActivity(intent);
            }
        });

        groupManagement = (ImageView)findViewById(R.id.group);
        groupManagement.setClickable(true);
        groupManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupManagementActivity.class);
                startActivity(intent);
            }
        });
    }
}
