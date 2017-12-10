package com.example.chris.mcc_2017_g19;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumsActivity;
import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.example.chris.mcc_2017_g19.BackgroundServices.SyncImagesService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private ImageView gallery;
    private ImageView groupManagement;
    private ImageView settings;
    private ImageView takepicture;

    private String imagePath;

    private FirebaseUser firebaseUser;
    private UserObject userObj;
    private DatabaseReference databaseReference;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int MY_PERMISSIONS_REQUEST_QR = 4;
    private static final int REQUEST_CREATE_GROUP = 5;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = Utils.getDatabase().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());

        String device_token = FirebaseInstanceId.getInstance().getToken();
        if(userReference != null && device_token != null){
            userReference.child("deviceTokens").child(device_token).setValue(1);
        }

        //Check for new images everytime when loading MainActivity, if user is in a group.
        userReference.keepSynced(true);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userObj = snapshot.getValue(UserObject.class);
                if(userObj != null){
                    if(userObj.getGroup() != null){
                        Intent it = new Intent(getApplicationContext(), SyncImagesService.class);
                        it.putExtra("groupID", userObj.getGroup());
                        startService(it);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //Ask the user for permission to write on disc
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

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
                //Dont start camera if network not available
                if(Utils.isNetworkAvailable(getApplicationContext()))
                    selectGroupManagementActivity();
                else
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
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
                //Dont start camera if network not available
                if(Utils.isNetworkAvailable(getApplicationContext())){
                    setButtonStatus(false);
                    DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
                    userReference.keepSynced(true);
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            UserObject userObject = snapshot.getValue(UserObject.class);
                            if(userObject != null){
                                final String userGroup = userObject.getGroup();
                                if (userGroup == null) {
                                    errorToast("Join or create a group to take pictures");
                                    setButtonStatus(true);
                                } else {
                                    cameraButtonAction(userGroup);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            setButtonStatus(true);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
    Group management activity shows join or create group when not in a group.
    It shows group status when in a group
     */
    private void selectGroupManagementActivity() {
        setButtonStatus(false);
        DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
        userReference.keepSynced(true);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                setButtonStatus(true);
                UserObject userObject = snapshot.getValue(UserObject.class);
                if(userObject != null){
                    //User does not have a group. Query if they want to make a new group or join an existing one
                    if(userObject.getGroup() == null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        View customTitle = View.inflate(MainActivity.this, R.layout.dialog_title, null);
                        builder.setCustomTitle(customTitle);
                        builder.setTitle("Choose an action");

                        List<String> actions = new ArrayList<String>();
                        actions.add("JOIN A GROUP");
                        actions.add("CREATE A GROUP");
                        DialogAdapter da = new DialogAdapter(MainActivity.this, actions);
                        builder.setAdapter(da, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        //"Yes" button pressed: Join a group
                                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this,
                                                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_QR);
                                        } else {
                                            Intent intent = new Intent(MainActivity.this, QrReaderActivity.class);
                                            startActivityForResult(intent, REQUEST_CREATE_GROUP);
                                        }
                                        break;
                                    case 1:
                                        //"No" button pressed: Create group
                                        Intent intent = new Intent(MainActivity.this, GroupCreationActivity.class);
                                        startActivityForResult(intent, REQUEST_CREATE_GROUP);
                                        break;
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else{
                        //If user already in a group, show group status activity
                        Intent intent = new Intent(MainActivity.this, GroupStatusActivity.class);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                setButtonStatus(true);
            }
        });
    }

    /*

     */
    private void cameraButtonAction(String group) throws IllegalArgumentException {
        DatabaseReference userGroupReference = databaseReference.child("groups").child(group);
        userGroupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupObject groupObj = dataSnapshot.getValue(GroupObject.class);
                if(groupObj != null) {
                    if (!groupObj.isExpired()){
                        TakePictureIntent();
                        setButtonStatus(true);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Group expired", Toast.LENGTH_SHORT).show();
                        setButtonStatus(true);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    setButtonStatus(true);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                setButtonStatus(true);
            }
        });
    }


    private void errorToast(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }


    /*
    Checking for camera permissions
     */
    private void TakePictureIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamera();
            setButtonStatus(true);
        }
    }

    /*
    Starts the camera and gives the image file where to write to
     */
    private void startCamera() {
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


    /*
    Create image file where camera saves the image
     */
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


    /*
    This is used only for the camera's result at the moment.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);
                // We check the permission at Runtime
            } else {
                Intent it = new Intent(getApplicationContext(), ImagePreviewActivity.class);
                it.putExtra("imagePath", imagePath);
                startActivity(it);
            }
        }
    }


    /*
    Method for requesting camera and storage permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent it = new Intent(getApplicationContext(), ImagePreviewActivity.class);
                    it.putExtra("imagePath", imagePath);
                    startActivity(it);
                } else {
                    Toast.makeText(this, "Please grant permissions to use the app", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                    setButtonStatus(true);
                } else {
                    Toast.makeText(this, "Please grant permissions to use the Camera", Toast.LENGTH_SHORT).show();
                    setButtonStatus(true);
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
        }
    }

    //Buttons are set disabled so that the activities cannot be accessed many times at a time. Finally enabled when user gets back to the view.
    private void setButtonStatus(boolean status){
        findViewById(R.id.gallery).setEnabled(status);
        findViewById(R.id.photo).setEnabled(status);
        findViewById(R.id.group).setEnabled(status);
        findViewById(R.id.setting).setEnabled(status);
    }


    protected void showProgressDialog(String title, String msg) {
        progressDialog = ProgressDialog.show(this, title, msg, true);
    }


    protected void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
