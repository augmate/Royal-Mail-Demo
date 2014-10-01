package com.augmate.apps.carloading;

import android.os.Bundle;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.augmate.sdk.data.PackageCarLoad;
import com.augmate.sdk.logger.Log;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class TestUpsDataSyncActivity extends RoboActivity {

    @InjectView(R.id.num_entries_ups_data)
    TextView numPkgsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ups_data_sync);

        CarLoadingDataStore carLoadingDataStore = new CarLoadingDataStore(this);

        String trackingNumber = "1Z0X37X30342307044";
        PackageCarLoad loadForTrackingNumber = carLoadingDataStore.findLoadForTrackingNumber(trackingNumber);

        Log.debug("Looking for tracking number %s", trackingNumber);

        Log.debug("Tracking # %s has location %s",
                loadForTrackingNumber.getTrackingNumber(),
                loadForTrackingNumber.getLoadPosition());

        Integer packageCount = carLoadingDataStore.numberOfPackages();
        Log.debug("Found a total of %d packages", packageCount);

        numPkgsView.setText("" + packageCount);
    }

}
