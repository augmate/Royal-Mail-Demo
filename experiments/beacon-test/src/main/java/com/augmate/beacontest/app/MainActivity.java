package com.augmate.beacontest.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.TextView;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.logger.Log;

import java.util.*;

public class MainActivity extends Activity {
    BeaconDistance beaconDistance = new BeaconDistance();
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private Handler displayMsgUpdater = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String displayMsg = msg.getData().getString("msg");
            ((TextView) findViewById(R.id.beaconScanResults)).setText(displayMsg);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.debug("Pausing..");

        timer.cancel();
        beaconDistance.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.debug("Resuming..");

        beaconDistance.configureFromContext(this);
        beaconDistance.startListening();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // getLatestBeaconDistances() is nice and fast now (<1ms)
                List<BeaconInfo> beaconDistances = beaconDistance.getLatestBeaconDistances();

                String displayMsg = String.format("%d beacons are within the area\n", beaconDistances.size());
                //Log.debug("We know about %d beacons within the area", beaconDistances.size());

                BeaconRegion truck1 = new BeaconRegion();
                BeaconRegion truck2 = new BeaconRegion();

                for (BeaconInfo beacon : beaconDistances) {
                    //Log.debug("  beacon %d = mean: %.2f / 80th percentile: %.2f", beacon.minor, beacon.distance, beacon.weightedAvgDistance);
                    displayMsg += String.format("#%d samples=%02d weighted=%.2f\n", beacon.minor, beacon.numValidSamples, beacon.weightedAvgDistance);

                    if(beacon.minor == 1 || beacon.minor == 2) {
                        truck1.minDistance = Math.min(truck1.minDistance, beacon.weightedAvgDistance);
                        truck1.numOfBeacons++;
                    }

                    if(beacon.minor == 3 || beacon.minor == 4) {
                        truck2.minDistance = Math.min(truck2.minDistance, beacon.weightedAvgDistance);
                        truck2.numOfBeacons++;
                    }
                }

                displayMsg += String.format("Truck 1: samples=%d dist=%.1f\n", truck1.numOfBeacons, truck1.minDistance);
                displayMsg += String.format("Truck 2: samples=%d dist=%.1f\n", truck2.numOfBeacons, truck2.minDistance);

                Message msg = displayMsgUpdater.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("msg", displayMsg);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }, 0, 100);
    }

}
