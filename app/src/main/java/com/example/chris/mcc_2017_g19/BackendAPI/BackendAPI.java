package com.example.chris.mcc_2017_g19.BackendAPI;

/**
 * Created by Chris on 27.11.2017.
 */

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BackendAPI {

    private OkHttpClient client;
    private String backendUrl = "https://mcc-fall-2017-g19.appspot.com";
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public BackendAPI() {
        client = new OkHttpClient();
    }

    //Helper Functions
    private void getRequest(String url, HttpCallback cb) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        executeRequest(request, cb);
    }


    private void postRequest(String url, RequestBody body, HttpCallback cb) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        executeRequest(request, cb);
    }


    private void deleteRequest(String url, String json, HttpCallback cb) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        executeRequest(request, cb);
    }

    private void executeRequest(Request request, final HttpCallback cb){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException exception) {
                cb.onFailure(null, exception);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    cb.onFailure(response.body().string(), null);
                    return;
                }
                cb.onSuccess(response.body().string());
            }
        });
    }

    //API functions
    public String joinGroup(String userID, String token, HttpCallback cb){
        String url = backendUrl + "/users/" + userID + "/group";
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("token", token)
                .build();
        try{
            postRequest(url, requestBody, cb);
        }
        catch (Exception e){
        }
        return null;
    }


    public String createGroup(String groupName, String userID, HttpCallback cb){
        String url = backendUrl + "/groups";
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("group_name", groupName)
                .addFormDataPart("user_id", userID)
                .build();
        try{
            postRequest(url, requestBody, cb);
        }
        catch (Exception e){
        }
        return null;
    }

    public String deleteGroup(String group_id, HttpCallback cb){
        String url = backendUrl + "/groups";
        String json = "{'group_id': '" + group_id + "'}";
        try{
            deleteRequest(url, json, cb);
        }
        catch (Exception e){
        }
        return null;
    }

    public String getGroup(String groupID, HttpCallback cb){
        String url = backendUrl + "/groups/" + groupID;
        try{
            getRequest(url, cb);
        }
        catch (Exception e){
        }
        return null;
    }

    public String getUserGroup(String userID, HttpCallback cb){
        String url = backendUrl + "/users/" + userID + "/group";
        try{
            getRequest(url, cb);
        }
        catch (Exception e){
        }
        return null;
    }

    public String getGroupToken(String userID, HttpCallback cb){
        String url = backendUrl + "/users/" + userID + "/token";
        try{
            getRequest(url, cb);
        }
        catch (Exception e){
        }
        return null;
    }

    public interface HttpCallback  {
        public void onFailure(String response, Exception exception);

        public void onSuccess(String response);
    }
}