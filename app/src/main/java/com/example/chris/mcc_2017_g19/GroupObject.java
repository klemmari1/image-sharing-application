package com.example.chris.mcc_2017_g19;


import java.util.ArrayList;
import java.util.List;

public class GroupObject {
    private static String name;
    private static String creator;
    private static String token;
    private static String expiration;
    private static List<String> members = new ArrayList<>();

    public GroupObject() {

    }

    public static String getName() {
        return name;
    }

    public static String getCreator() {
        return creator;
    }

    public static String getToken() {
        return token;
    }

    public static String getExpiration() {
        return expiration;
    }

    public static List<String> getMembers(){ return members; }

    public static void setName(String groupName){
        name = groupName;
    }

    public static void setCreator(String creatorName){
        creator = creatorName;
    }

    public static void setToken(String groupToken){
        token = groupToken;
    }

    public static void setExpiration(String groupExpiration){
        expiration = groupExpiration;
    }

    public static void setMembers(List<String> memberList){
        members = memberList;
    }
}
