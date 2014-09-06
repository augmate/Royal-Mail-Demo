package com.augmate.sample.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.augmate.sample.AugmateApplication;

/**
 * Created by cesaraguilar on 8/18/14.
 */
public class UserUtils {

    private static String sUser;

    public static String getUser() {
        if (sUser == null) {
            Context context = AugmateApplication.getContext();
            sUser = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString("UserName","");
        }
        return sUser;
    }

    public static void setUser(String inUser) {
        Context context = AugmateApplication.getContext();
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
        sUser = inUser;
        edit.putString("UserName",sUser);
        edit.commit();
    }

    public static boolean isAUser(String inUser) {
        return inUser.startsWith("User_") || inUser.startsWith("user_");
    }

    public static String getUserFromBarcode(String inUser) {
        return inUser.replace("User_","").replace("user_","");
    }

}
