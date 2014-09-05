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
import com.augmate.sdk.logger.What;

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
                long start = What.timey();
                List<BeaconInfo> beaconDistances = beaconDistance.getLatestBeaconDistances();
                long span = What.timey() - start;
                Log.debug("getLatestBeaconDistances took %d msec", span);

                String displayMsg = String.format("%d beacons are within the area\n", beaconDistances.size());

                Log.debug("We know about %d beacons within the area", beaconDistances.size());

                for (BeaconInfo beacon : beaconDistances) {
                    Log.debug("beacon %d = mean: %.2f / 80th percentile: %.2f", beacon.minor, beacon.distanceMean, beacon.distancePercentile);

                    displayMsg += String.format("beacon #%d dist=%.2f 80th percentile=%.2f\n", beacon.minor, beacon.distanceMean, beacon.distancePercentile);
                }

                if(beaconDistances.size() > 0) {
                    Collections.sort(beaconDistances, new Comparator<BeaconInfo>() {
                        @Override
                        public int compare(BeaconInfo b1, BeaconInfo b2) {
                            return (int) (b1.distanceMean * 50 - b2.distanceMean * 50);
                        }
                    });

                    BeaconInfo nearestBeacon = beaconDistances.get(0);

                    Log.debug("Nearest beacon: #%d at %.2f units away", nearestBeacon.minor, nearestBeacon.distanceMean);
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
