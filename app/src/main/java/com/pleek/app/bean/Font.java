package com.pleek.app.bean;

/**
 * Created by tiago on 20/04/2015.
 */
public class Font extends Overlay {

    private String mName;

    public Font(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }
}
