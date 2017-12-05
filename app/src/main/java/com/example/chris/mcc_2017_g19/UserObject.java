package com.example.chris.mcc_2017_g19;


public class UserObject {
    private static String name;
    private static String group;
    private static String id;

    public UserObject() {

    }

    public UserObject(String username) {
        name = username;
        group = null;
    }

    public static String getId() {
        return id;
    }

    public static String getName() {
        return name;
    }

    public static String getGroup() {
        return group;
    }

    public static void setName(String username){
        name = username;
    }

    public static void setId(String userId){
        id = userId;
    }

    public static void setGroup(String groupId){
        group = groupId;
    }
}
