package com.example.chris.mcc_2017_g19;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.chris.mcc_2017_g19.AlbumsView.AlbumsActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView gallery;
    ImageView groupManagement;
    ImageView settings;
    ImageView takepicture;

    Bitmap bitmap;
    private String imagePath;

    private FirebaseUser firebaseUser;
    private UserObject userObj;
    private DatabaseReference databaseReference;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 2;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    static final int MY_PERMISSIONS_REQUEST_QR = 4;
    private static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = Utils.getDatabase().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
        userReference.keepSynced(true);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userObj = snapshot.getValue(UserObject.class);
                Toast.makeText(MainActivity.this, "Welcome " + userObj.getName(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });


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
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
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
                MainActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void selectGroupManagementActivity() {
        if (userObj != null){
            if (userObj.getGroup() == null){
                //Setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View customTitle = View.inflate(MainActivity.this, R.layout.dialog_title, null);
                builder.setCustomTitle(customTitle);
                builder.setTitle("Choose an action");

                //Add options
                List<String> actions = new ArrayList<String>();
                actions.add("JOIN A GROUP");
                actions.add("CREATE A GROUP");
                DialogAdapter da = new DialogAdapter(this, actions);
                builder.setAdapter(da, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //Yes button pressed: Join a group
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_QR);
                                } else {
                                    Intent intent = new Intent(MainActivity.this, QrReaderActivity.class);
                                    startActivity(intent);
                                }
                                break;
                            case 1:
                                //No button pressed: Create group
                                Intent intent = new Intent(MainActivity.this, GroupCreationActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                });
                //Create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else{
                Intent intent = new Intent(MainActivity.this, GroupStatusActivity.class);
                intent.putExtra("GROUP_ID", userObj.getGroup());
                startActivity(intent);
            }
        }
    }


    private void TakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {


            //this bundle is giving back an image with bad resolution
            //TODO:this bundle is giving back an image with bad resolution
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //SaveImage(imageBitmap);
            //save image to an imageview
            //mImageView.setImageBitmap(imageBitmap);
            /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // convert byte array to Bitmap
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);*/

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);

                // We check the permission at Runtime
                //
                return;
            } else {

                new SensibleDataTask().execute(imageBitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new SensibleDataTask().execute(bitmap);
                } else {
                    Toast.makeText(this, "Please grant permissions to use the app", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(this, "Please grant permissions to use the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_QR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, QrReaderActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                break;
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
        String fname = "Image-" + n + ".jpeg";

        File file = new File(myDir, fname);


        try {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"Image/*"}, null);
            System.out.println(file);

            //TODO: This part has to be solved. Images are losing quality and apparently there is no solution for it
            //FileOutputStream out = new FileOutputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(out.toByteArray());
            fo.close();

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
