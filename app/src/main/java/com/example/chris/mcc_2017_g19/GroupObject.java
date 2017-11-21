package com.example.chris.mcc_2017_g19;


import java.util.ArrayList;
import java.util.List;

public class GroupObject {
    public String name;
    public String creator;
    public List<UserObject> groupMembers;

    public GroupObject() {

    }

    public GroupObject(String name, String creator) {
        this.name = name;
        this.creator = creator;
        this.groupMembers = new ArrayList<UserObject>();
    }
}
