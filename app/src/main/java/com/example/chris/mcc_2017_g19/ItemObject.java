package com.example.chris.mcc_2017_g19;

/**
 * Created by alessiospallino on 08/11/2017.
 */

public class ItemObject {

    private String title;
    private String imageResource;
    private String cloud;

    public ItemObject(String title, String imageResource, String cloud) {
        this.title = title;
        this.imageResource = imageResource;
        this.cloud = cloud;
    }

    public String getTitle() {
        return title;
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
}
