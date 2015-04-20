package com.pleek.app.bean;

import com.parse.ParseObject;

/**
 * Created by tiago on 20/04/2015.
 */
public class Emoji extends Overlay {

    private String urlPhoto;
    private String name;
    private int priorite;

    public Emoji(ParseObject parseObject) {
        if (parseObject == null) return;

        id = parseObject.getObjectId();
        urlPhoto = parseObject.getParseFile("image").getUrl();
        name = parseObject.getString("name");
        priorite = parseObject.getInt("priorite");

        this.parseObject = parseObject;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriorite() {
        return priorite;
    }

    public void setPriorite(int priorite) {
        this.priorite = priorite;
    }
}
