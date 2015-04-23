package com.pleek.app.bean;

import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Created by tiago on 23/04/2015.
 */
public class LikeReact implements Serializable {

    private String id;
    private ParseObject parseObject;
    private String idReact = "";

    public LikeReact(ParseObject obj) {
        if (obj == null) return;

        id = obj.getObjectId();

        ParseObject react = obj.containsKey("react") ? obj.getParseObject("react") : null;
        if (react != null) idReact = react.getObjectId();

        this.parseObject = obj;
    }

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

    public String getIdReact() {
        return idReact;
    }

    public void setIdReact(String idReact) {
        this.idReact = idReact;
    }
}
