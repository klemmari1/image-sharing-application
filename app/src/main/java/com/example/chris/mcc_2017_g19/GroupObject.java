package com.example.chris.mcc_2017_g19;


import java.util.ArrayList;
import java.util.List;

public class GroupObject {
    public String name;
    public List<UserObject> groupMembers;

    public GroupObject() {

    }

    public GroupObject(String name) {
        this.name = name;
        this.groupMembers = new ArrayList<UserObject>();
    }
}
