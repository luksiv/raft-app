package com.latenightpenguin.groupdj;

import org.json.JSONObject;

/**
 * Created by User on 2018-03-20.
 */

public class User {

    private String mId;
    private String mDisplayName;
    private String mEmail;
    private String mCountry;

    @Override
    public String toString() {
        return String.format("ID : %s\nDisplay name : %s\nEmail : %s\nCountry : %s",
                mId, mDisplayName, mEmail, mCountry);
    }

    public User(String id, String displayName, String email, String country){
        mId = id;
        mDisplayName = displayName;
        mEmail = email;
        mCountry = country;
    }

    public String getId() {
        return mId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getCountry() {
        return mCountry;
    }
}
