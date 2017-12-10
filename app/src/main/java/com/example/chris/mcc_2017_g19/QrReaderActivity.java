package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.example.chris.mcc_2017_g19.BackgroundServices.SyncImagesService;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView barcodeScanner;

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
            if(Utils.isNetworkAvailable(getApplicationContext())){
                final String token = rawResult.getText();
                //Send token to back end for validity check and handling the joining
                BackendAPI api = new BackendAPI();
                api.joinGroup(token, new BackendAPI.HttpCallback() {
                    @Override
                    public void onFailure(String response, Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(String response) {
                        if(!response.contains("INVALID")){
                            try {
                                Intent it = new Intent(getApplicationContext(), SyncImagesService.class);
                                it.putExtra("groupID", token.split(":")[0]);
                                startService(it);

                                Intent groupStatus = new Intent(QrReaderActivity.this, GroupStatusActivity.class);
                                startActivity(groupStatus);
                                QrReaderActivity.this.finish();
                            } catch (Exception e){
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(QrReaderActivity.this, response, Toast.LENGTH_SHORT).show();
                            barcodeScanner.resumeCameraPreview(QrReaderActivity.this);
                        }
                    }
                });
            }
            else
                Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            barcodeScanner.resumeCameraPreview(this);
        }
    }
}
