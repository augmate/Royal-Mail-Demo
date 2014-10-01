package com.augmate.apps.carsweep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import com.augmate.apps.R;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.beacons.BeaconRegion;
import com.augmate.sdk.beacons.BeaconRegionDetector;
import com.augmate.sdk.beacons.RegionProcessor;
import com.augmate.sdk.logger.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IdentifyVehicleActivity extends Activity {
    public static final int REQUEST_CODE_DOWNLOAD_DATA = 0x001;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private BeaconRegionDetector beaconDistanceMeasurer = new BeaconRegionDetector();
    private ScheduledFuture<?> scheduledRegionTest;
    private String detectedCarLoadPosition = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detection);
        Log.start(this);

        beaconDistanceMeasurer.configureFromContext(this);
        startRegionTesting();
    }

    private void testNearestRegion() {
        List<BeaconInfo> latestBeaconDistances = beaconDistanceMeasurer.getLatestBeaconDistances();
        Log.debug("Found %d beacons nearby", latestBeaconDistances.size());

        if(latestBeaconDistances.size() == 0) {
            return;
        }

        for(BeaconInfo beaconInfo : latestBeaconDistances){
            Log.info("-> Beacon name=%s minor/id=%s region=%s", beaconInfo.beaconName, beaconInfo.minor, beaconInfo.regionId);
        }

        RegionProcessor regionProcessor = new RegionProcessor(new ArrayList<>(Arrays.asList(
                new BeaconRegion(2, 5, 12).setRegionId(109),
                new BeaconRegion(3, 9, 11).setRegionId(111)
        )));

        int nearestCarId = regionProcessor.getNearestRegionId(latestBeaconDistances);
        detectedCarLoadPosition = String.valueOf(nearestCarId);

        Log.debug("Detected nearest car id: %s", detectedCarLoadPosition);

        if(nearestCarId != -1) {
            goToDataDownload(detectedCarLoadPosition);
        }
    }

    private void goToDataDownload(String carLoad) {
        stopRegionTesting();

        startActivityForResult(new Intent(this, UpsDataSyncActivity.class)
                .putExtra(UpsDataSyncActivity.EXTRA_CAR_LOAD, carLoad), REQUEST_CODE_DOWNLOAD_DATA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DOWNLOAD_DATA) {
            startActivity(new Intent(this, CarSweepActivity.class)
                    .putExtra(CarSweepActivity.EXTRA_CAR_LOAD, detectedCarLoadPosition));
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startRegionTesting() {
        if(scheduledRegionTest != null) {
            return;
        }

        beaconDistanceMeasurer.startListening();
        scheduledRegionTest = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                testNearestRegion();
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);
    }

    private void stopRegionTesting() {
        if(scheduledRegionTest != null) {
            scheduledRegionTest.cancel(false);
            scheduledRegionTest = null;
            beaconDistanceMeasurer.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRegionTesting();
    }
}
