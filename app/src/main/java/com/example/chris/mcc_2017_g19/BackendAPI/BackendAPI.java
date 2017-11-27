package com.example.chris.mcc_2017_g19.BackendAPI;

/**
 * Created by Chris on 27.11.2017.
 */

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BackendAPI {

    private OkHttpClient client;
    private String backendUrl = "127.0.0.1";
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public BackendAPI() {
        client = new OkHttpClient();
    }

    //Helper Functions
    private String getRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    private String postRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    private String deleteRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    //API functions
    public String joinGroup(String groupID, String userID){
        String url = backendUrl + "/groups/" + groupID + "/members";
        String json = "{'user_id': '" + userID + "'}";
        try{
            return postRequest(url, json);
        }
        catch (Exception e){
        }
        return null;
    }

    public String createGroup(String groupName, String userID){
        String url = backendUrl + "/groups";
        String json = "{'group_name': '" + groupName + "'," +
                "'user_id': '" + userID + "'}";
        try{
            return postRequest(url, json);
        }
        catch (Exception e){
        }
        return null;
    }

    public String deleteGroup(String group_id){
        String url = backendUrl + "/groups";
        String json = "{'group_id': '" + group_id + "'}";
        try{
            return deleteRequest(url, json);
        }
        catch (Exception e){
        }
        return null;
    }
}