package com.augmate.warehouse.prototype;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author James Davis (Fuzz)
 */
public class Data {
    private static String user;
    private static HashMap<String, Integer> bins;

    static {
        bins = new HashMap<String, Integer>();
    }

    public static void setUser(String newUser){
        user = newUser;
    }

    public static boolean addBin(String binId, int count){
        if (user == null){
            return false;
        } else {
            bins.put(binId, count);
            return true;
        }
    }
}
