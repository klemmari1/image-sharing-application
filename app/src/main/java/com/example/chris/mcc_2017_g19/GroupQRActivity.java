package com.example.chris.mcc_2017_g19;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.google.firebase.auth.FirebaseUser;


public class GroupQRActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private static final String TAG = "GroupQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = Utils.getDatabase().getReference();

        DatabaseReference userRef = databaseReference.child("users").child(firebaseUser.getUid());
        userRef.keepSynced(true);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String group_id = (String) dataSnapshot.child("group").getValue();
                if(group_id != null){
                    DatabaseReference groupRef = databaseReference.child("groups").child(group_id);
                    groupRef.keepSynced(true);
                    groupRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GroupObject groupObj = dataSnapshot.getValue(GroupObject.class);
                            if(groupObj != null){
                                String token = groupObj.getToken();
                                System.out.println(groupObj);
                                try{
                                    ImageView imageView = (ImageView) findViewById(R.id.qr_image);
                                    Bitmap bitmap = getBitmap(token, 750);
                                    imageView.setImageBitmap(bitmap);
                                }
                                catch (Exception e){
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private Bitmap getBitmap(String str, int dim) throws WriterException {
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = barcodeWriter.encode(str, BarcodeFormat.QR_CODE, dim, dim);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int[] pixelArray = createPixelArray(bitMatrix);
        Bitmap bitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixelArray, 0, dim, 0, 0, dim, dim);
        return bitmap;
    }

    private int[] createPixelArray(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixelArray = new int[width * height];
        for (int j=0; j < height; ++j) {
            int index = j * width;
            for (int i=0; i < width; ++i) {
                pixelArray[index + i] = bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE;
            }
        }
        return pixelArray;
    }
}