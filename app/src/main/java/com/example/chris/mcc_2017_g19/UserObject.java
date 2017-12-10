package com.example.chris.mcc_2017_g19;


public class UserObject {
    private String name;
    private String group;
    private String id;

    public UserObject() {

    }

    public UserObject(String username) {
        name = username;
        group = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public void setName(String username){
        name = username;
    }

    public void setId(String userId){
        id = userId;
    }

    public void setGroup(String groupId){
        group = groupId;
    }
}
