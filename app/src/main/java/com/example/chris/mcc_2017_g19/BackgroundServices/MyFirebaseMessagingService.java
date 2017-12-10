package com.example.chris.mcc_2017_g19.BackgroundServices;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.chris.mcc_2017_g19.MainActivity;
import com.example.chris.mcc_2017_g19.R;
import com.example.chris.mcc_2017_g19.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

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

                //If group is deleted
                if(jsonObject.has("deleted_group")){
                    String deleted_group = jsonObject.getString("deleted_group");
                    sendNotification("Group deleted!", "Your group was deleted!");
                }

                //If an user leaves the group
                else if(jsonObject.has("left_user")){
                    String left_user = jsonObject.getString("left_user");
                    sendNotification("User left!", "User " + left_user + " left from the group!");
                }

                //If a new photo was added to the group
                else if(jsonObject.has("photographer")){
                    final String photographer = jsonObject.getString("photographer");

                    DatabaseReference databaseReference = Utils.getDatabase().getReference();
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = firebaseUser.getUid();
                    DatabaseReference userReference = databaseReference.child("users").child(uid);
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            //1. get group id from database: users/<userid>/group
                            String groupID = (String) userSnapshot.child("group").getValue();
                            String userName =  (String) userSnapshot.child("name").getValue();
                            Log.d(TAG,"found groupID in the begining of syncImageFolder(): " + groupID);

                            //2. get all image ids from groups/groupID/images

                            if (groupID != null && !userName.equals(photographer)) {
                                //Send push notification
                                sendNotification("New image!", "New image from " + photographer);

                                //Start sync service
                                Intent it = new Intent(getApplicationContext(), SyncImagesService.class);
                                it.putExtra("groupID", groupID);
                                startService(it);
                                Log.d(TAG,"Data MSG in. (no new data nessesarily)");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getMessage());
                        }
                    });
                }
            }
            catch (JSONException e) {
                Log.d(TAG,"json roblem",e);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            sendNotification("Error", "Notification payload (this should never happen):  " + remoteMessage.getNotification().getBody());
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
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}