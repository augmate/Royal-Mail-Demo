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
                // getLatestBeaconDistances() is nice and fast now (<1ms), no need to time it any more :)
                List<BeaconInfo> beaconDistances = beaconDistance.getLatestBeaconDistances();

                String displayMsg = String.format("%d beacons are within the area\n", beaconDistances.size());
                //Log.debug("We know about %d beacons within the area", beaconDistances.size());

                for (BeaconInfo beacon : beaconDistances) {
                    //Log.debug("  beacon %d = mean: %.2f / 80th percentile: %.2f", beacon.minor, beacon.distance, beacon.weightedAvgDistance);
                    displayMsg += String.format("#%d dist=%.2f weighted=%.2f\n", beacon.minor, beacon.distance, beacon.weightedAvgDistance);
                }

                if(beaconDistances.size() > 0) {
                    Collections.sort(beaconDistances, new Comparator<BeaconInfo>() {
                        @Override
                        public int compare(BeaconInfo b1, BeaconInfo b2) {
                            return (int) (b1.weightedAvgDistance * 500 - b2.weightedAvgDistance * 500);
                        }
                    });

                    BeaconInfo nearestBeacon = beaconDistances.get(0);
                    Log.info("-> Nearest beacon: #%d at %.2f units away", nearestBeacon.minor, nearestBeacon.weightedAvgDistance);
                }

                Message msg = displayMsgUpdater.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("msg", displayMsg);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }, 0, 500);
    }
}
