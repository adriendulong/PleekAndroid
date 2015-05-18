package com.pleek.app.bean;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nicolas on 31/12/14.
 */
public class Piki extends VideoBean
{
    private String name;
    private String firstName;
    private int nbReact;
    private int nbRecipient;
    private String urlPiki;
    private String urlReact1;
    private String urlReact2;
    private String urlReact3;
    private Date createdAt;
    private Date updatedAt;
    private List<String> frinedsId;
    private boolean isPublic;
    private boolean isBest;

    public Piki(ParseObject parseObject) {
        if(parseObject == null) return;

        id = parseObject.getObjectId();
        name = parseObject.containsKey("user") ? parseObject.getParseUser("user").getUsername() : "NULL";
        firstName = parseObject.containsKey("user") ? parseObject.getParseUser("user").getString("name") : "";
        nbReact = parseObject.getInt("nbReaction");
        frinedsId = parseObject.getList("recipients");
        if(frinedsId == null) frinedsId = new ArrayList<String>();
        nbRecipient = frinedsId.size();

        if (parseObject.getParseFile("video") == null) {
            urlPiki = parseObject.getParseFile("smallPiki") != null ? parseObject.getParseFile("smallPiki").getUrl() : null;//TODO : CRASH #12
        } else {
            urlPiki = parseObject.getParseFile("previewImage") != null ? parseObject.getParseFile("previewImage").getUrl() : null;
        }
        urlReact1 = parseObject.getParseFile("react1") != null ? parseObject.getParseFile("react1").getUrl() : null;
        urlReact2 = parseObject.getParseFile("react2") != null ? parseObject.getParseFile("react2").getUrl() : null;
        urlReact3 = parseObject.getParseFile("react3") != null ? parseObject.getParseFile("react3").getUrl() : null;
        updatedAt = parseObject.getUpdatedAt();
        createdAt = parseObject.getCreatedAt();
        isPublic = parseObject.getBoolean("isPublic");
        this.parseObject = parseObject;


        urlVideo = parseObject.getParseFile("video") != null ? parseObject.getParseFile("video").getUrl() : null;

        isBest = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbReact() {
        return nbReact;
    }

    public int getNbRecipient()
    {
        return nbRecipient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNbReact(int nbReact) {
        this.nbReact = nbReact;
    }

    public String getUrlPiki() {
        return urlPiki;
    }

    public void setUrlPiki(String urlPiki) {
        this.urlPiki = urlPiki;
    }

    public String getUrlReact1() {
        return urlReact1;
    }

    public void setUrlReact1(String urlReact1) {
        this.urlReact1 = urlReact1;
    }

    public String getUrlReact2() {
        return urlReact2;
    }

    public void setUrlReact2(String urlReact2) {
        this.urlReact2 = urlReact2;
    }

    public String getUrlReact3() {
        return urlReact3;
    }

    public void setUrlReact3(String urlReact3) {
        this.urlReact3 = urlReact3;
    }

    public boolean thereIsReact1() {
        return urlReact1 != null && !urlReact1.trim().isEmpty();
    }
    public boolean thereIsReact2() {
        return urlReact2 != null && !urlReact2.trim().isEmpty();
    }
    public boolean thereIsReact3() {
        return urlReact3 != null && !urlReact3.trim().isEmpty();
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public boolean iamOwner()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null || parseObject == null || parseObject.getParseUser("user") == null) return false; //fix : crash #155
        return currentUser.getObjectId().equals(parseObject.getParseUser("user").getObjectId());
    }

    public List<String> getFrinedsId()
    {
        return frinedsId;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isBest() {
        return isBest;
    }

    public void setBest(boolean isBest) {
        this.isBest = isBest;
    }
}