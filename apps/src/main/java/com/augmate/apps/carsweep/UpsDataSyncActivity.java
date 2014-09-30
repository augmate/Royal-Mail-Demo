package com.augmate.apps.carsweep;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.augmate.sdk.data.InternetChecker;
import com.augmate.sdk.data.callbacks.CacheCallback;
import com.augmate.sdk.logger.Log;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class UpsDataSyncActivity extends Activity {

    public final static String EXTRA_CAR_LOAD = "EXTRA_CAR_LOAD";

    private List<String> carLoads = new ArrayList<>();
    private List<String> loadsDone = new ArrayList<>();

    private ProgressBar progressBarView;
    private TextView downloadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ups_data_sync_activity);

        String carLoad = getIntent().getStringExtra(EXTRA_CAR_LOAD);

        carLoads.add(carLoad);

        progressBarView = (ProgressBar) findViewById(R.id.sync_progress_bar);
        downloadView = (TextView) findViewById(R.id.download_state);

        final TextView cnxView = (TextView) findViewById(R.id.internet_cnx_state);

        if (new InternetChecker().isConnected(this)) {
            Log.info("Connected to the internet");
            startUpsDataDownload();
        } else {
            Log.info("Not connected to the internet!");
            cnxView.setVisibility(View.VISIBLE);
            findViewById(R.id.download_state).setVisibility(View.INVISIBLE);
        }
    }

    private void startUpsDataDownload() {
        final CarLoadingDataStore carLoadingDataStore = new CarLoadingDataStore(UpsDataSyncActivity.this);

        downloadView.setText("Download started...");
        progressBarView.setVisibility(View.VISIBLE);
        Log.info("Download started");

        for (final String load : carLoads) {
            carLoadingDataStore.downloadCarLoadDataToCache(load, new CacheCallback() {
                @Override
                public void success() {
                    Log.info("Done downloading car load data for %s", load);
                    loadFinished(load);
                }

                @Override
                public void error(ParseException e) {
                    Log.info("Could not download car load data for %s", load);
                }
            });
        }
    }

    //Thread safe due to SaveCallback being called on main thread.
    private void loadFinished(String load) {
        loadsDone.add(load);

        if (loadsDone.size() == carLoads.size()) {
            progressBarView.setVisibility(View.INVISIBLE);

            String loadStr = "";
            for (String l : loadsDone) {
                loadStr = l + '\n';
            }

            downloadView.setText("Download complete for load \n\n" + loadStr );
            Log.info("UPS Download complete");
        }
    }
}
