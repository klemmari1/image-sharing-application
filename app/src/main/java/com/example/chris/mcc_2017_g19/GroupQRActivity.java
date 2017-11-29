package com.example.chris.mcc_2017_g19;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

/**
 * Created by Chris on 6.11.2017.
 */

public class GroupQRActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private static final String TAG = "GroupQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String group_id = getIntent().getStringExtra("GROUP_ID");
        DatabaseReference groupRef = databaseReference.child("groups").child(group_id);
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupObject groupObj = dataSnapshot.getValue(GroupObject.class);
                System.out.println(groupObj);
                try{
                    displayQR(createBarcode(groupObj.getToken()));
                }
                catch (Exception e){
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    private void displayQR(Bitmap qr) {
        ImageView groupNameField = (ImageView) findViewById(R.id.qr_image);
        groupNameField.setImageBitmap(qr);
    }


    Bitmap createBarcode(String txtInput) throws WriterException {
        int map_width = 750;
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(txtInput,
                    BarcodeFormat.QR_CODE, map_width, map_width, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, map_width, 0, 0, w, h);
        return bitmap;
    }
}
