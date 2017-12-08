package com.example.chris.mcc_2017_g19;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView gallery;
    ImageView groupManagement;
    ImageView settings;
    ImageView takepicture;

    private String imagePath;

    private FirebaseUser firebaseUser;
    private UserObject userObj;
    private GroupObject userGroupObj;
    private DatabaseReference databaseReference;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 2;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    static final int MY_PERMISSIONS_REQUEST_QR = 4;
    static final int REQUEST_CREATE_GROUP = 5;
    private static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = Utils.getDatabase().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
        userReference.keepSynced(true);

        //testing notification stuff (fcm)
        String token = FirebaseInstanceId.getInstance().getToken();

        userReference.child("deviceTokens").child(token).setValue(1);
        Log.d(TAG,"token token token:" + token);


        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userObj = snapshot.getValue(UserObject.class);
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
                final String userGroup = userObj.getGroup();
                if (userGroup == null) {
                    errorToast("Join or create a group to take pictures");
                } else {
                    cameraButtonAction(userGroup);
                }
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
                                setButtonStatus(false);
                                Intent intent = new Intent(MainActivity.this, GroupCreationActivity.class);
                                startActivityForResult(intent, REQUEST_CREATE_GROUP);
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
                startActivity(intent);
            }
        }
    }


    private void cameraButtonAction(String group) throws IllegalArgumentException {
        DatabaseReference userGroupReference = databaseReference.child("groups").child(group);
        userGroupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userGroupObj = dataSnapshot.getValue(GroupObject.class);
                if (!userGroupObj.isExpired())
                    TakePictureIntent();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void errorToast(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }


    private void TakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        setButtonStatus(false);
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setButtonStatus(true);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);
                // We check the permission at Runtime
                //
            } else {
                new SensibleDataTask().execute();
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
                    new SensibleDataTask().execute();
                } else {
                    Toast.makeText(this, "Please grant permissions to use the app", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
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

    public class SensibleDataTask extends AsyncTask<Void, Void, Bitmap> {

        Integer result = 0;

        @Override
        protected Bitmap doInBackground(Void... params) {
            //Create the Barcode detector and detect barcode
            Bitmap resized = getLowResolutionBitmap(0.4);

            BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE | Barcode.EAN_13).build();
            Frame frame = new Frame.Builder().setBitmap(resized).build();
            SparseArray<Barcode> barcodes = new SparseArray<>();
            if(detector.isOperational()){
                barcodes = detector.detect(frame);
                detector.release();
            }
            result = barcodes.size();
            System.out.println("Barcodes found: " + result);
            if(result == 0) {
                // If the image has no sensitive data, TODO: Call method to store in Firebase + Google App Engine

            } else {
                // The image has one sensitive data, check here to know what is a sensitive data:
                // https://developers.google.com/vision/android/barcodes-overview

                // Call the method to store image in private folder
                SaveImage("/Private");
            }

            removeTempPicture();

            return resized;
            //return Bitmap.createScaledBitmap(bit, width, height, true);
        }

        @Override
        protected void onPostExecute(Bitmap bit) {
            Integer check = 0;
            if (result.equals(check)) {
                //It is not showing the toast, I don't know why (But it is entering this :
                Toast.makeText(MainActivity.this, "Image has been added to your shared folder!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "SENSIBLE DATA! Image has been added to private folder", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void SaveImage(String folder) {
        Bitmap finalBitmap = getImageBitmap();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/OrganizerApp" + folder);

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

    private void removeTempPicture(){
        File file_to_delete = new File(imagePath);
        if (file_to_delete.exists()) {
            if (file_to_delete.delete()) {
                System.out.println("file Deleted :" + file_to_delete.getPath());
            } else {
                System.out.println("file not Deleted :" + file_to_delete.getPath());
            }
        }
    }

    private Bitmap getImageBitmap(){
        try{
            Uri imageUri = Uri.fromFile(new File(imagePath));
            return(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
        }
        catch(Exception e){
        }
        return null;
    }

    private Bitmap getLowResolutionBitmap(double factor){
        Bitmap bitmap = getImageBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*factor), (int)(bitmap.getHeight()*factor), true);
        return resized;
    }

    private void setButtonStatus(boolean status){
        findViewById(R.id.gallery).setEnabled(status);
        findViewById(R.id.photo).setEnabled(status);
        findViewById(R.id.group).setEnabled(status);
        findViewById(R.id.setting).setEnabled(status);
    }
}
