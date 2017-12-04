package com.example.chris.mcc_2017_g19.BackgroundSync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootWithFirebase extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(FirebaseBackgroundService.class.getName()));
    }

}
