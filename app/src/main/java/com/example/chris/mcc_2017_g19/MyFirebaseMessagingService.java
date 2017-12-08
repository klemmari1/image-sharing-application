package com.example.chris.mcc_2017_g19;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


    UserObject userObj;
    DatabaseReference databaseReference;

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]


        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            try {
                //receive groupID and filename from received data-notification
                JSONObject jsonObject = new JSONObject(remoteMessage.getData());
                String filename = jsonObject.getString("filename");
                String groupID = jsonObject.getString("groupID");

                String photographer = jsonObject.getString("photographer");


                sendNotification("New image from " + photographer);
                syncImageFolder();
                Log.d(TAG,"Data MSG in. (no new data nessesarily)");
            }
            catch (JSONException e) {
                Log.d(TAG,"json roblem",e);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            sendNotification("Notification payload (this should never happen):  " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("FCM Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    //TODO in syncImageFolder(): access control between android and firebase
    public void syncImageFolder() {
        //get remote image ids: 1. get group id of current user, 2. get all image ids

        databaseReference = Utils.getDatabase().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();

        DatabaseReference userReference = databaseReference.child("users").child(uid);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                //1. get group id from database: users/<userid>/group
                final String groupID = (String) userSnapshot.child("group").getValue();
                Log.d(TAG,"found groupID in the begining of syncImageFolder(): " + groupID);

                //2. get all image ids from groups/groupID/images

                if (groupID != null) {
                    DatabaseReference imagesReference = databaseReference.child("groups").child(groupID);

                    imagesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot groupSnapshot) {

                            //get local imageIDs first:
                            final String groupName = (String) groupSnapshot.child("name").getValue();
                            List<String> localImageIdList = getLocalImageIDs(groupID,groupName);

                            List<String> remoteImageIdList = new ArrayList<String>();
                            for (DataSnapshot imageSnapshot: groupSnapshot.child("images").getChildren()) {
                                remoteImageIdList.add(imageSnapshot.getKey());
                                Log.d(TAG,"added to remote image list: " + imageSnapshot.getKey() + ":"+groupID);
                            }

                            //loop through remote imageIDs and download if new found
                            for (final String remoteImageID : remoteImageIdList) {
                                if (localImageIdList.contains(remoteImageID) != true) {
                                    Log.d(TAG,"Found new remote image to be synced! remoteImageID: wasnt found locally " + remoteImageID);

                                    //get url based on quality min(localMaxQ,remoteMaxQ)
                                    ////get max quality as int
                                    String remoteMaxQ = (String) groupSnapshot.child("images").child(remoteImageID).child("maxQuality").getValue();
                                    Log.d(TAG, "remoteMaxQ: " + remoteMaxQ);
                                    //TODO: get local max Quality from Alessio / Kristian
                                    String localMaxQ = "high";
                                    String finalQ;
                                    if (qualityAsInt(localMaxQ) >= qualityAsInt(remoteMaxQ)) {
                                        finalQ = remoteMaxQ;
                                    }
                                    else {
                                        finalQ = localMaxQ;
                                    }

                                    //get url for the image
                                    String url = (String) groupSnapshot.child("images").child(remoteImageID).child((String) finalQ + "URL").getValue();

                                    //download from url as bitmap
                                    //Bitmap newBitmap = getBitmapFromURL(url); this doesnt work, mainthread röplem

                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    final StorageReference httpsReference = storage.getReferenceFromUrl(url);

                                    final long MAX_SIZE = 50*1024*1024;

                                    String photoOwnerID = (String) groupSnapshot.child("images").child(remoteImageID).child("userID").getValue();

                                    DatabaseReference photographerUserRef = databaseReference.child("users").child(photoOwnerID);
                                    photographerUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot userSnapshot) {

                                            String photoOwner = (String) userSnapshot.child("name").getValue();
                                            long hasFaces = (long) groupSnapshot.child("images").child(remoteImageID).child("hasFaces").getValue();
                                            String fname = remoteImageID + "_" + photoOwner + "_" + hasFaces + "_.jpg";
                                            String path =  "PhotoOrganizer/Albums/" + groupName + "_" + groupID;
                                            File sdCardRoot = Environment.getExternalStorageDirectory();
                                            File directory = new File(sdCardRoot, path);

                                            try {
                                                File newFile = new File(directory, fname);
                                                httpsReference.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                        Log.d(TAG,"Downloaded an image! /w ID: " + remoteImageID);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.d(TAG, e.getMessage());
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG,"error in getting photographer name" + databaseError.getDetails() + databaseError.getMessage());
                                        }
                                    });
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG,"database error: " + databaseError.getDetails() + databaseError.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    public List<String> getLocalImageIDs(String groupID, String groupName) {

        File sdCardRoot = Environment.getExternalStorageDirectory();
        String path = "/PhotoOrganizer/Albums/" + groupName + "_" + groupID;
        File yourDir = new File(sdCardRoot + path);

        Log.d(TAG,"yourDIr path is: "+ yourDir.getAbsolutePath());

        if (!yourDir.exists()) {
            yourDir.mkdirs();
            Log.d(TAG,"Now a directory should be created");
        }

        List<String> IDs = new ArrayList<String>();
        String filename;
        try {
            Log.d(TAG, "local image id list:");
            for (File f : yourDir.listFiles()) {
                if (f.isFile()) {
                    filename = f.getName();
                    //1234_a.b@c.com_0_.jpg
                    IDs.add(filename.split("_")[0]);
                    //IDs.add(filename);

                    Log.d(TAG, filename.split("_")[0]);
                }
            }

        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
        }

        return IDs;
    }

    public int qualityAsInt(String someQuality) {
        if (someQuality.equals("low")) {
            return 0;
        }
        else if (someQuality.equals("high")) {
            return 1;
        }
        else if (someQuality.equals("full")) {
            return 2;
        }
        else {
            Log.d(TAG,"ERROR IN qualityAsInt function: someQuality: " + someQuality);
            return 0;
        }
    }


}