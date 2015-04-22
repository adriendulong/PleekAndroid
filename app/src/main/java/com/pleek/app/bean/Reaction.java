package com.pleek.app.bean;

import android.content.Context;
import android.graphics.Bitmap;

import com.goandup.lib.utile.L;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by nicolas on 31/12/14.
 */
public class Reaction extends VideoBean
{
    public enum Type {
        PHOTO,
        VIDEO,
        TEXTE,
        EMOJI,
        UNKNOW
    }

    private String urlPhoto;
    private String nameUser;
    private String userId;
    private Bitmap tmpPhoto;//use between create and save to Parse
    private boolean hasLiked = false;
    public Type type = Type.UNKNOW;

    public Reaction(String nameUser, Bitmap tmpPhoto)
    {
        this.id = UUID.randomUUID().toString();//tmp random id
        this.nameUser = nameUser;
        this.tmpPhoto = tmpPhoto;
    }

    public Reaction(ParseObject parseObject)
    {
        if(parseObject == null) return;

        id = parseObject.getObjectId();
        if(parseObject.getParseFile("photo") != null)
        {
            urlPhoto = parseObject.getParseFile("photo").getUrl();
        }
        else if(parseObject.getParseFile("previewImage") != null)
        {
            urlPhoto = parseObject.getParseFile("previewImage").getUrl();
        }
        urlVideo = parseObject.getParseFile("video") != null ? parseObject.getParseFile("video").getUrl() : null;

        ParseObject user = parseObject.containsKey("user") ? parseObject.getParseObject("user") : null;
        if(user != null) nameUser = user.containsKey("username") ? user.getString("username") : null;
        if(user != null) userId = user.getObjectId();
        if(nameUser == null) nameUser = "@none";

        this.parseObject = parseObject;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public Type getType()
    {
        return type != null ? type : Type.UNKNOW;
    }

    public String getTypeStr()
    {
        if(type == Type.PHOTO) return "Photo";
        else if(type == Type.VIDEO) return "Video";
        else if(type == Type.TEXTE) return "Text";
        else if(type == Type.EMOJI) return "Emoji";
        return "Unknow";
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Bitmap getTmpPhoto()
    {
        return tmpPhoto;
    }

    public void setTmpPhoto(Bitmap tmpPhoto)
    {
        this.tmpPhoto = tmpPhoto;
    }

    public byte[] getTmpPhotoByte()
    {
        if(tmpPhoto == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        tmpPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);//fix : crash #34
        return stream.toByteArray();
    }

    public boolean isTmpReaction()
    {
        return urlPhoto == null || urlPhoto.trim().isEmpty();
    }

    public boolean iamOwner()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null || parseObject == null || parseObject.getParseUser("user") == null) return false;//fix : crash #18
        return currentUser.getObjectId().equals(parseObject.getParseUser("user").getObjectId());
    }

    @Override
    public boolean equals(Object o)
    {
        return o != null && o instanceof Reaction && id.equals(((Reaction)o).getId());
    }

    @Override
    public String toString()
    {
        return "Reaction("+id+")";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }
}