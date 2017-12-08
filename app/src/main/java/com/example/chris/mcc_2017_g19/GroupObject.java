package com.example.chris.mcc_2017_g19;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

    public boolean isExpired() {
        Calendar groupCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf.format(groupCalendar.getTime());
        try {
            groupCalendar.setTime(sdf.parse(this.expiration));
        } catch (ParseException pe){
            pe.printStackTrace();
        }

        Calendar currentTimeCalendar = Calendar.getInstance();
        if (groupCalendar.compareTo(currentTimeCalendar) < 0)
            return true;
        return false;
    }
}
