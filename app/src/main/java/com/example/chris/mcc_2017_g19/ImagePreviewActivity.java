package com.example.chris.mcc_2017_g19;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.BackgroundServices.ImageSaveService;

import java.io.File;
import java.io.InputStream;


/*
This class is for showing a preview of the image before actually sending it to the group
 */
public class ImagePreviewActivity extends AppCompatActivity {

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        // get intent data
        Intent i = getIntent();

        imagePath = i.getExtras().getString("imagePath");

        //Load image asynchronously
        ImageLoadTask ilt = new ImageLoadTask();
        ilt.execute(imagePath);
    }


    public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            File f = new File(urls[0]);

            //Scale image before loading to ImageView
            Uri uri = Uri.fromFile(f);

            InputStream is = null;
            InputStream is2 = null;

            try{
                is = getContentResolver().openInputStream(uri);
                is2 = getContentResolver().openInputStream(uri);
            }
            catch (java.io.FileNotFoundException e){
                e.printStackTrace();
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            BitmapFactory.decodeStream(is, null, options);

            options.inJustDecodeBounds = true;
            options.inSampleSize = calculateSamplesize(options, 1000, 1000);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeStream(is2, null, options);
        }

        protected void onPostExecute(Bitmap finalBitmap) {
            //Load image to ImageView
            ImageView photoImageView = (ImageView) findViewById(R.id.previewImage);
            photoImageView.setImageDrawable(new BitmapDrawable(getResources(), finalBitmap));
        }
    }


    private int calculateSamplesize(
            BitmapFactory.Options options, int w, int h) {

        int height = options.outHeight;
        int width = options.outWidth;
        int samplesize = 1;

        if (height > h || width > w) {

            int hH = height / 2;
            int hW = width / 2;

            while ((hH / samplesize) >= h
                    && (hW / samplesize) >= w) {
                samplesize *= 2;
            }
        }
        return samplesize;
    }

    public void onYesClick(View v){
        Intent it = new Intent(getApplicationContext(), ImageSaveService.class);
        it.putExtra("imagePath", imagePath);
        startService(it);
        this.finish();
    }

    public void onNoClick(View v){
        this.finish();
    }
}
