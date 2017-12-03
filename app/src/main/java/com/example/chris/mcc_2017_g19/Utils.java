package com.example.chris.mcc_2017_g19;


import com.google.firebase.database.FirebaseDatabase;

public class Utils {
    private static FirebaseDatabase firebaseDatabase;

    public static FirebaseDatabase getDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }
}