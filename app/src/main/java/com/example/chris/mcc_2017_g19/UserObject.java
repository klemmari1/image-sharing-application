package com.example.chris.mcc_2017_g19;


public class UserObject {
    private String name;
    private String group;

    public UserObject() {

    }

    public UserObject(String name) {
        this.name = name;
        this.group = null;
    }

    public String getName() {
        return this.name;
    }

    public String getGroup() {
        return this.group;
    }   
}
