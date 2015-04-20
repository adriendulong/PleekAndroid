package com.pleek.app.bean;

import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Created by tiago on 20/04/2015.
 */
public class Overlay implements Serializable {

    protected String id;
    protected ParseObject parseObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }
}
