package com.augmate.beacontest.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.logger.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RegionDebugger extends Activity {
    private Timer periodicVisualizerUpdate;
    private BeaconDistance beaconDistanceMeasurer = new BeaconDistance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        Log.start(this);

        beaconDistanceMeasurer.configureFromContext(this);
    }

    private void startVisualizationUpdates() {
        periodicVisualizerUpdate = new Timer();
        periodicVisualizerUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // update visualization
                List<BeaconInfo> latestBeaconDistances = beaconDistanceMeasurer.getLatestBeaconDistances();
                ((RegionVisualization) findViewById(R.id.beaconVisualizer)).update(latestBeaconDistances);
            }
        }, 100, 50);
    }

    private void stopVisualizationUpdates() {
        periodicVisualizerUpdate.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.debug("Unbinding from scanner service.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug("Resuming");
        startVisualizationUpdates();
        beaconDistanceMeasurer.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing");
        beaconDistanceMeasurer.stopListening();
        stopVisualizationUpdates();
    }
}
