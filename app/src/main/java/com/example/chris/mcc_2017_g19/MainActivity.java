package com.example.chris.mcc_2017_g19;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {

    ImageView gallery;
    ImageView groupManagement;
    ImageView settings;
    ImageView takepicture;

    Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 2;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Called any time data is added to database reference
                Log.d(TAG, "Value is: " + snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        gallery = (ImageView) findViewById(R.id.gallery);
        gallery.setClickable(true);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
                startActivity(intent);
            }
        });

        groupManagement = (ImageView) findViewById(R.id.group);
        groupManagement.setClickable(true);
        groupManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGroupManagementActivity();
            }
        });

        settings = (ImageView) findViewById(R.id.setting);
        settings.setClickable(true);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Move to activity for settings
            }
        });

        //Click on the photo image and open the camera
        takepicture = (ImageView) findViewById(R.id.photo);
        takepicture.setClickable(true);
        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TakePictureIntent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                //TODO Do we need (onCompletion) listeners for these kinds of situations?
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            case R.id.action_read_qr:
                //TODO start activity for QR reading
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void selectGroupManagementActivity() {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Class activityClass;
                UserObject user = snapshot.child(firebaseUser.getUid()).getValue(UserObject.class);
                if (user.getGroup() == null)
                    activityClass = GroupCreationActivity.class;
                else
                    activityClass = GroupStatusActivity.class;
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
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


            //TODO: Here I need to check if the image contains sensible data
            // If yes, store in local
            // If no, store inside the group ... ... ...

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);

                // We check the permission at Runtime
                //
                return;
            } else {

                new SensibleDataTask().execute(bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new SensibleDataTask().execute(bitmap);
                } else {
                    // permission denied, boo!
                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    //TODO: Check if image is sensible or not;

    public class SensibleDataTask extends AsyncTask<Bitmap, Void, Bitmap> {

        Bitmap bit;
        Integer result = 0;

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            this.bit = bitmaps[0];

            //Create the Barcode detector and detect barcode
            BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE | Barcode.EAN_13).build();
            Frame frame = new Frame.Builder().setBitmap(bit).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);

            if (barcodes.size() != 1) {
                System.out.println("This is res: " + result);
                // If the image has no sensitive data, TODO: Call method to store in Firebase + Google App Engine

            } else {
                result = 1;
                // The image has one sensitive data, check here to know what is a sensitive data:
                // https://developers.google.com/vision/android/barcodes-overview

                // Call the method to store image in private folder
                SaveImage(bit);
            }

            return bit;
            //return Bitmap.createScaledBitmap(bit, width, height, true);
        }

        @Override
        protected void onPostExecute(Bitmap bit) {
            Integer check = 0;
            if (result == check) {
                //It is not showing the toast, I don't know why (But it is entering this :
                Toast.makeText(MainActivity.this, "Image has been added to your shared folder!", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(MainActivity.this, "SENSIBLE DATA! Image has been added to private folder", Toast.LENGTH_LONG);
            }
        }


        private void SaveImage(Bitmap finalBitmap) {

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/OrganizerApp");

            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            //Creating a unique name for the picture
            Random generator = new Random();
            int n = 1000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".jpg";

            File file = new File(myDir, fname);

            try {
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"Image/*"}, null);
                System.out.println(file);

                //TODO: This part has to be solved. Images are losing quality and apparently there is no solution for it
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
