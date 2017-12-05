package com.example.chris.mcc_2017_g19;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.BackgroundSync.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class GroupQRActivity extends AppCompatActivity {

    private static final String TAG = "GroupQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        try{
            Bitmap bitmap = getBitmap(GroupObject.getToken(), 750);
            ImageView imageView = (ImageView) findViewById(R.id.qr_image);
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e){
        }

        final DatabaseReference databaseReference = Utils.getDatabase().getReference();
        DatabaseReference tokenReference = databaseReference.child("groups").child(UserObject.getGroup()).child("token");

        tokenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                try{
                    String token = (String)snapshot.getValue();
                    Bitmap bitmap = getBitmap(token, 750);
                    ImageView imageView = (ImageView) findViewById(R.id.qr_image);
                    imageView.setImageBitmap(bitmap);
                }
                catch (Exception e){
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
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
