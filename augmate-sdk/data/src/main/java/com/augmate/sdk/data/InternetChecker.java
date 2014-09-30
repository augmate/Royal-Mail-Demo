package com.augmate.sdk.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetChecker {
    public boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo bluetoothInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

        if(wifiInfo.isConnected() || bluetoothInfo.isConnected()){
            return true;
        }

        return false;
    }
}
