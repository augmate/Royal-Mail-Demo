package com.augmate.apps.carloading;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.sdk.data.InternetChecker;
import com.augmate.sdk.logger.Log;

public class UpsDataSyncActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ups_data_sync_activity);

        TextView view = (TextView) findViewById(R.id.internet_cnx_state);

        if(new InternetChecker().isConnected(this)){
            Log.info("Connected to the internet");
            view.setText("Connected to the internet");
        }else{
            Log.info("Disconnected from the internet");
            view.setText("Disconnected from the internet");
        }

    }
}
