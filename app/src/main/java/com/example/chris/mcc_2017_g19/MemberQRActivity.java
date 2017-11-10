package com.example.chris.mcc_2017_g19;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Chris on 6.11.2017.
 */

public class MemberQRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        //To be removed
        ImageView imageView = (ImageView) findViewById(R.id.qr_image);
        Picasso.with(this).load("https://lh3.googleusercontent.com/u9CquezfCFyXtTqr9Ls_PnLmKibDJK2ZKiuZGkgNc-M6qlxIWpK_Q-Buki9cgHBVXOc=w300").into(imageView);
    }
}
