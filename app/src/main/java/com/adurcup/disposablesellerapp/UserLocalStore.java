package com.adurcup.disposablesellerapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kshivang on 01/07/16.
 * This is content provider android used for providing login detail
 */
public class UserLocalStore {

    private static final String KEY_IS_LOGGEDIN = "loggedIn";
    private static final String SP_NAME= "userDetails";

    SharedPreferences userLocalDatabase;
    SharedPreferences.Editor spEditor;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
        spEditor = userLocalDatabase.edit();
        spEditor.apply();
    }

    public void storeUserData(String ApiKey, String Name, String Email, String Contact) {
        spEditor = userLocalDatabase.edit();
        spEditor.putString(Constant.KEY_API_KEY, ApiKey);
        spEditor.putString(Constant.KEY_NAME, Name);
        spEditor.putString(Constant.KEY_EMAIL, Email);
        spEditor.putString(Constant.KEY_CONTACT, Contact);
        spEditor.putInt(KEY_IS_LOGGEDIN, Constant.FLAG_LOGGED_IN);
        spEditor.apply();
    }

    public String getLoggedInUser(String Parameter) {
       return userLocalDatabase.getString(Parameter, null);
    }


    public int getUserLoggedIn() {
        return userLocalDatabase.getInt(KEY_IS_LOGGEDIN, Constant.FLAG_LOGGED_OUT);
    }

    public void clearUserData() {
        spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }

    public void setUserStatus(String Contact, int Purpose) {
        spEditor = userLocalDatabase.edit();
        spEditor.putString(Constant.KEY_CONTACT, Contact);
        spEditor.putInt(KEY_IS_LOGGEDIN, Purpose);
        spEditor.apply();
    }

}
