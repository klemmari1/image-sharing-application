package com.example.chris.mcc_2017_g19.AlbumsView;


/*
Class for storing the images that are showed in the album grid
 */
public class ItemObject {

    private String title;
    private String wholeName;
    private String imageResource;
    private String cloud;
    private String number;

    public ItemObject(String title, String wname, String imageResource, String cloud, String number) {
        this.title = title;
        this.wholeName = wname;
        this.imageResource = imageResource;
        this.cloud = cloud;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public String getWholeName() {
        return wholeName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageResource() {
        return imageResource;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
