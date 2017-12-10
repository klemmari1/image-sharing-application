package com.example.chris.mcc_2017_g19.BackendAPI;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/*
This class is used to call the cloud back end for various group related actions
 */
public class BackendAPI {

    private OkHttpClient client;
    private String backendUrl = "https://mcc-fall-2017-g19.appspot.com";
    private String idToken;
    private static final String TAG = "BackendAPI";

    public BackendAPI() {
        client = new OkHttpClient();
    }

    //Helper Functions
    private void postRequest(String url, RequestBody body, HttpCallback cb) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        executeRequest(request, cb);
    }


    private void deleteRequest(String url, RequestBody body, HttpCallback cb) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        executeRequest(request, cb);
    }

    private void executeRequest(Request request, final HttpCallback cb){
        client.newCall(request).enqueue(new Callback() {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(Call call, final IOException exception) {
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cb.onFailure(null, exception);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if (!response.isSuccessful()) {
                                cb.onFailure(response.body().string(), null);
                                return;
                            }
                            cb.onSuccess(response.body().string());
                        }
                        catch(Exception e){
                        }
                    }
                });
            }
        });
    }

    /*
    Gets the user token to send with the requests
     */
    private void getIdToken(final HttpCallback cb){
        if(idToken == null){
            try{
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.getToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    idToken = task.getResult().getToken();
                                    cb.onSuccess(idToken);
                                }
                            }
                        });
            }
            catch (Exception e){
            }
        }
        else{
            cb.onSuccess(idToken);
        }
    }

    //API functions
    public void joinGroup(final String token, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }

            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/groups/join";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_token", idToken)
                        .addFormDataPart("token", token)
                        .build();
                try{
                    postRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }

    public void createGroup(final String groupName, final String expirationTimestamp, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }

            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/groups";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_token", idToken)
                        .addFormDataPart("group_name", groupName)
                        .addFormDataPart("group_expiration", expirationTimestamp)
                        .build();
                try{
                    postRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }

    public void deleteGroup(final String group_id, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }

            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/groups";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("group_id", group_id)
                        .addFormDataPart("id_token", idToken)
                        .build();
                try{
                    deleteRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }

    public void leaveGroup(final String group_id, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }

            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/groups/" + group_id + "/members";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_token", idToken)
                        .addFormDataPart("group_id", group_id)
                        .build();
                try{
                    deleteRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }

    public void uploadImage(final String group_id, final String filename, final String maxQuality, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }

            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/upload_image";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_token", idToken)
                        .addFormDataPart("groupID", group_id)
                        .addFormDataPart("filename", filename)
                        .addFormDataPart("maxQuality", maxQuality)
                        .build();
                try{
                    postRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }


    public void updateDeviceToken(final String device_token, final HttpCallback cb){
        getIdToken(new BackendAPI.HttpCallback() {

            @Override
            public void onFailure(String response, Exception exception) {
                cb.onFailure(response, exception);
            }
            @Override
            public void onSuccess(String response) {
                String url = backendUrl + "/updateDeviceToken";
                final RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("userToken", idToken)
                        .addFormDataPart("deviceToken", device_token)
                        .build();
                try{
                    postRequest(url, requestBody, cb);
                }
                catch (Exception e){
                }
            }
        });
    }


    public interface HttpCallback  {
        public void onFailure(String response, Exception exception);

        public void onSuccess(String response);
    }
}