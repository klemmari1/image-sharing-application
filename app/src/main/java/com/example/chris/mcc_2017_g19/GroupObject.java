package com.example.chris.mcc_2017_g19;


import java.util.HashMap;
import java.util.Map;

public class GroupObject {
    private String name;
    private String creator;
    private String token;
    private String expiration;
    private Map<String,String> members = new HashMap<>();

    public GroupObject() {

    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getToken() {
        return token;
    }

    public String getExpiration() {
        return expiration;
    }

    public Map<String,String> getMembers(){ return members; }

    public void setName(String groupName){
        name = groupName;
    }

    public void setCreator(String creatorName){
        creator = creatorName;
    }

    public void setToken(String groupToken){
        token = groupToken;
    }

    public void setExpiration(String groupExpiration){
        expiration = groupExpiration;
    }

    public void setMembers(Map<String,String> memberList){
        members = memberList;
    }
}
