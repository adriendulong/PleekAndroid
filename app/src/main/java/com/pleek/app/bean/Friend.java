package com.pleek.app.bean;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pleek.app.R;

import java.io.Serializable;

//CLASS
public class Friend implements Comparable<Friend>, Serializable {
    public String username;
    public String name;
    public String phoneNumber;
    public String parseId;
    public int sectionLabel;
    public int image;

    public Friend(ParseUser user, int sectionLabel)
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        this.sectionLabel = sectionLabel;
        this.image = R.drawable.picto_added;
        this.username = user.getUsername();
        this.parseId = user.getObjectId();
    }

    public Friend(ParseObject parseObject)
    {
        this.username = parseObject.getString("username");
        this.name = parseObject.getString("name");
        this.parseId = parseObject.getObjectId();
    }

    public Friend(String name, int image)
    {
        this.name = name;
        this.image = image;
    }

    public Friend(String name, int sectionLabel, int image)
    {
        this.name = name;
        this.sectionLabel = sectionLabel;
        this.image = image;
    }

    @Override
    public int compareTo(Friend friend)
    {
        if (friend != null) {
            if (sectionLabel != friend.sectionLabel) {
                if (sectionLabel == R.string.friends_section_on) return -1;
                return 1;
            } else if (name != null) {
                return name.compareTo(friend.name);
            } else if (username != null) {
                return username.compareTo(friend.username);
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Friend) || (name == null && username == null)) return false;
        Friend f = ((Friend) o);
        return sectionLabel == f.sectionLabel && (
                (name != null && name.equals(f.name))
             || (username != null && username.equals(f.username))
        );
    }
}