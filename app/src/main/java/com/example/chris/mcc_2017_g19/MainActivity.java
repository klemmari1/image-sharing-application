package com.example.chris.mcc_2017_g19;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView gallery;
    ImageView settings;
    ImageView takepicture;

    Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 2;

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


        //temporary solution for signout button (just for testing) CAN BE REMOVED
        settings = (ImageView) findViewById(R.id.setting);
        settings.setClickable(true);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });
        //-----------------------------------------------------------------------

        //Click on the photo image and open the camera
        settings = (ImageView) findViewById(R.id.photo);
        settings.setClickable(true);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            TakePictureIntent();

            }
        });

    }

    private void TakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {


            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //save image to an imageview
            //mImageView.setImageBitmap(imageBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // convert byte array to Bitmap
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);


            //TODO: It would be better to check here if permission of WRTIE_EXTERNAL_CAMERA has been given

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);

                // We check the permission at Runtime
                //
                return;
            }
            else{
                SaveImage(bitmap);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    //File write logic here
                    SaveImage(bitmap);

                } else {

                    // permission denied, boo!

                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }




    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();

        File myDir = new File(root + "/OrganizerApp");

        if(!myDir.exists()){
            myDir.mkdirs();
        }

        //Creating a unique name for the picture
        Random generator = new Random();
        int n = 1000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";

        File file = new File(myDir,fname);


        try {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]  {file.getPath()} , new String[]{"Image/*"}, null);
            System.out.println(file);
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }


}
