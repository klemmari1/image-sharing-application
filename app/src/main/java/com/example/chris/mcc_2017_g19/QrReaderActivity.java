package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView barcodeScanner;
    private static final String TAG = "QrReaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        barcodeScanner = new ZXingScannerView(this);
        setContentView(barcodeScanner);

        barcodeScanner.setResultHandler(this);
        barcodeScanner.startCamera();

    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeScanner.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        try{
            String token = rawResult.getText();
            final String group_id = token.split(":")[0];
            BackendAPI api = new BackendAPI();
            api.joinGroup(UserObject.getId(), token, new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Log.d(TAG, "Error: " + response + " " + exception);
                }

                @Override
                public void onSuccess(String response) {
                    if(!response.contains("INVALID")){
                        try {
                            Intent groupStatus = new Intent(QrReaderActivity.this, GroupStatusActivity.class);
                            groupStatus.putExtra("GROUP_ID", group_id);
                            startActivity(groupStatus);
                            QrReaderActivity.this.finish();
                        } catch (Exception e){
                            Toast.makeText(QrReaderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(QrReaderActivity.this, "INVALID BARCODE", Toast.LENGTH_SHORT).show();
                        barcodeScanner.resumeCameraPreview(QrReaderActivity.this);
                    }
                }
            });
        }
        catch (Exception e){
            Toast.makeText(QrReaderActivity.this, "NETWORK ERROR", Toast.LENGTH_SHORT).show();
            barcodeScanner.resumeCameraPreview(this);
        }
    }
}
