package com.example.chris.mcc_2017_g19;


public class UserObject {
    private String username;
    private String group;

    public UserObject() {

    }

    public UserObject(String name) {
        this.username = name;
        this.group = null;
    }

    public String getGroup() {
        return this.group;
    }
}
