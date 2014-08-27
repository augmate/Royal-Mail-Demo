package com.augmate.sample.common;

/**
 * Created by cesaraguilar on 8/18/14.
 */
public class UserUtils {

    private static String sUser;

    public static String getUser() {
        return sUser;
    }

    public static void setUser(String inUser) {
        sUser = inUser;
    }

    public static boolean isAUser(String inUser) {
        return inUser.startsWith("User_") || inUser.startsWith("user_");
    }

    public static String getUserFromBarcode(String inUser) {
        return inUser.replace("User_","").replace("user_","");
    }

}
