package com.augmate.apps.carsweep;

import android.app.Activity;
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

public class IdentifyVehicle extends Activity {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private BeaconRegionDetector beaconDistanceMeasurer = new BeaconRegionDetector();
    private String currentCarId;
    private ScheduledFuture<?> scheduledRegionTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_sweep);
        Log.start(this);

        beaconDistanceMeasurer.configureFromContext(this);
        startRegionTesting();
    }

    private void testNearestRegion() {
        List<BeaconInfo> latestBeaconDistances = beaconDistanceMeasurer.getLatestBeaconDistances();
        RegionProcessor regionProcessor = new RegionProcessor(new ArrayList<>(Arrays.asList(
                new BeaconRegion(6).setRegionId(1),
                new BeaconRegion(49623).setRegionId(2)
        )));
        int nearestCarId = regionProcessor.getNearestRegionId(latestBeaconDistances);

        Log.debug("nearest car id: %d", nearestCarId);

        if(nearestCarId != -1) {
            goToDataDownload();
        }
    }

    private void goToDataDownload() {
        stopRegionTesting();
//        startActivity();
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
