package com.example.chris.mcc_2017_g19.BackgroundServices;

import android.util.Log;

import com.example.chris.mcc_2017_g19.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        //This actually triggers after installation but fails since there's no firebaseUser object yet
        //solution: get the token in mainactivity to firebase
        try {
            databaseReference = Utils.getDatabase().getReference();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference userReference = databaseReference.child("users").child(firebaseUser.getUid());
            //setvalue 2 here, check if this actually works if 2's in firebase..
            userReference.child("deviceTokens").child(token).setValue(2);
            Log.d(TAG,"token token token:" + token);
        }
        catch (Exception e) {
                Log.d(TAG,e.getMessage());
            }

    }
}