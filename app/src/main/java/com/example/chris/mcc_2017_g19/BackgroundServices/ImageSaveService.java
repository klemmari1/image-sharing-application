package com.example.chris.mcc_2017_g19.BackgroundServices;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.example.chris.mcc_2017_g19.Connectivity.Connectivity;
import com.example.chris.mcc_2017_g19.GroupObject;
import com.example.chris.mcc_2017_g19.UserObject;
import com.example.chris.mcc_2017_g19.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Random;


/*
This service handles saving the image from the camera to local storage and firebase storage.
It also updates the database.
 */
public class ImageSaveService extends IntentService {

    private FirebaseUser firebaseUser;
    private UserObject userObj;
    private DatabaseReference databaseReference;

    private String imagePath;
    private String maxQuality;

    public ImageSaveService(){
        super("ImageSaveService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets image path of the image captured with the camera
        imagePath = intent.getStringExtra("imagePath");

        databaseReference = Utils.getDatabase().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
        userReference.keepSynced(true);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userObj = snapshot.getValue(UserObject.class);
                if(userObj != null && userObj.getGroup() != null)
                    checkSensiblePictures();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }


    private void checkSensiblePictures() {
        //Create the Barcode detector and detect barcode
        Bitmap bitmap = getImageBitmap();
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE | Barcode.EAN_13).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = new SparseArray<>();
        if(detector.isOperational()){
            barcodes = detector.detect(frame);
            detector.release();
        }
        int result = barcodes.size();
        System.out.println("Barcodes found: " + result);
        if(result == 0) {
            CheckSettings();
        } else {
            //Creating a unique name for the picture
            Random generator = new Random();
            int n = 1000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".jpg";

            // Call the method to store image in private folder
            SaveImage("Private", fname);
        }

        if (result == 0) {
            Toast.makeText(ImageSaveService.this, "Image is being added to your shared folder!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ImageSaveService.this, "SENSIBLE DATA! Image is being added to private folder", Toast.LENGTH_LONG).show();
        }
    }

    /*
    Save image locally
     */
    private void SaveImage(String folder, String filename) {
        File myDir = new File(Utils.getAlbumsRoot(getApplicationContext()) +  File.separator + folder);

        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        try{
            File file_a = new File(imagePath);

            if(file_a.renameTo(new File(myDir + File.separator + filename))){
                System.out.println("Temp image moved successfully!");
            }else{
                System.out.println("Error!");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    Checks for quality settings and then calls the upload function
     */
    private void CheckSettings() {
        Bitmap FirebaseBitmap = getImageBitmap();

        if(Connectivity.isConnectedMobile(getApplicationContext())){

            //Check which one is the setting (low/high/full) and tranform the bitmap
            if(getLTESettings().toLowerCase().contains("high")){

                //Check if the image has been taken in landscape or not:
                if(FirebaseBitmap.getWidth() < FirebaseBitmap.getHeight()){
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 960, 1280, false);
                }else{
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 1280, 960, false);
                }
                maxQuality = "high";

            }else if(getLTESettings().toLowerCase().contains("low")){

                //Check if the image has been taken in landscape or not:
                if(FirebaseBitmap.getWidth() < FirebaseBitmap.getHeight()){
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 480, 640, false);
                }else{
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 640, 480, false);
                }
                maxQuality = "low";

            }else{
                maxQuality = "full";
            }
        }

        if(Connectivity.isConnectedWifi(getApplicationContext())){
            //Check which one is the setting (low/high/full) and tranform the bitmap
            if(getWIFISettings().toLowerCase().contains("high")){

                //Check if the image has been taken in landscape or not:
                if(FirebaseBitmap.getWidth() < FirebaseBitmap.getHeight()){
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 960, 1280, false);
                }else{
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 1280, 960, false);
                }
                maxQuality = "high";

            }else if(getWIFISettings().toLowerCase().contains("low")){

                //Check if the image has been taken in landscape or not:
                if(FirebaseBitmap.getWidth() < FirebaseBitmap.getHeight()){
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 480, 640, false);
                }else{
                    FirebaseBitmap = Bitmap.createScaledBitmap(FirebaseBitmap, 640, 480, false);
                }
                maxQuality = "low";

            }else{
                maxQuality = "full";
            }
        }
        uploadImageFirebase(FirebaseBitmap);
    }

    /*
    Uploads the image to firebase storage and updates the database.
    Finally saves the image locally to the group folder.
     */
    public void uploadImageFirebase(Bitmap FirebaseBitmap){
        //Get a reference from our storage:
        FirebaseStorage storage = FirebaseStorage.getInstance();

        Long tsLong = System.currentTimeMillis()/1000;
        final String imagename = tsLong.toString();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://mcc-fall-2017-g19.appspot.com/" + userObj.getGroup())
                .child(imagename + ".jpg");

        //Upload to Firebase using puBytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FirebaseBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Call the backend API and send data
                BackendAPI api = new BackendAPI();
                api.uploadImage(userObj.getGroup(), imagename + ".jpg", maxQuality, new BackendAPI.HttpCallback() {
                    @Override
                    public void onFailure(String response, Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(final String response) {
                        try {
                            if(!response.toLowerCase().contains("error")){
                                DatabaseReference userGroupReference = databaseReference.child("groups").child(userObj.getGroup());
                                userGroupReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        GroupObject groupObj = dataSnapshot.getValue(GroupObject.class);
                                        if(groupObj != null)
                                            SaveImage(groupObj.getName() + "_" + userObj.getGroup(), response + ".jpg");
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(ImageSaveService.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ImageSaveService.this, response, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e){
                            Toast.makeText(ImageSaveService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
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


    public final String getWIFISettings(){

        String WIFIpicturevalue =PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("WIFIpicturevalue","");

        return WIFIpicturevalue;
    }


    public final String getLTESettings(){

        String LTEpicturevalue = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("LTEpicturevalue","");

        return LTEpicturevalue;
    }

}
