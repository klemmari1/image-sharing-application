package com.example.chris.mcc_2017_g19;


import java.util.ArrayList;
import java.util.List;

public class GroupObject {
    private String name;
    private String creator;
    private List<UserObject> groupMembers;

    public GroupObject() {

    }

    public GroupObject(String name, String creator) {
        this.name = name;
        this.creator = creator;
        this.groupMembers = new ArrayList<UserObject>();
    }

    public String getName() {
        return this.name;
    }

    public String getCreator() {
        return this.creator;
    }

    public List<UserObject> getGroupMambers() {
        return this.groupMembers;
    }
}
