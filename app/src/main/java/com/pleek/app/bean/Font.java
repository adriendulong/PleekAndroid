package com.pleek.app.bean;

/**
 * Created by tiago on 20/04/2015.
 */
public class Font extends Overlay {

    private String mName;
    private int mColor;
    private String id;

    public Font(String name, int color) {
        this.mName = name;
        this.mColor = color;
        this.id = name + color;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
