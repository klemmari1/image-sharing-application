package com.example.chris.mcc_2017_g19;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
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

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = firebaseUser.getUid();
        BackendAPI api = new BackendAPI();
        api.getGroupToken(user_id, new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                // Handle exception
            }
            @Override
            public void onSuccess(final String token) {
                try {
                    GroupQRActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try{
                                ImageView imageView = (ImageView) findViewById(R.id.qr_image);
                                imageView.setImageBitmap(createBarcode(token));
                            }
                            catch (Exception e){
                            }
                        }
                    });
                } catch (Exception e) {
                }
            }
        });
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