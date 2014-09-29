package com.augmate.apps.carloading;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.augmate.sdk.data.InternetChecker;
import com.augmate.sdk.logger.Log;
import com.parse.ParseException;
import com.parse.SaveCallback;

public class UpsDataSyncActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ups_data_sync_activity);

        final TextView view = (TextView) findViewById(R.id.internet_cnx_state);

        if(new InternetChecker().isConnected(this)){
            Log.info("Connected to the internet");
            view.setText("Connected to the internet");
        }else{
            Log.info("Disconnected from the internet");
            view.setText("Disconnected from the internet");
        }

        final TextView downloadView = (TextView) findViewById(R.id.download_state);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                downloadView.setText("Download started...");
                Log.info("Download started");

                new CarLoadingDataStore(UpsDataSyncActivity.this).pullToLocalCache(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        downloadView.setText("Download complete");
                        Log.info("Download complete");
                    }
                });

            }
        }, 5000);


    }
}
