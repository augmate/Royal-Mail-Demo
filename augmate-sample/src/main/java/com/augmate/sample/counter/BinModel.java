package com.augmate.sample.counter;

import java.io.Serializable;

/**
 * Created by cesaraguilar on 8/18/14.
 */
public class BinModel implements Serializable {

    public static final String TAG = "BinModel";

    public String user;
    public String count;
    public String binBarcode;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getBinBarcode() {
        return binBarcode;
    }

    public void setBinBarcode(String barcode) {
        this.binBarcode = barcode;
    }
}
