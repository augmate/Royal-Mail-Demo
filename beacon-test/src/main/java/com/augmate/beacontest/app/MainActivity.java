package com.augmate.beacontest.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import com.augmate.sdk.beacons.BeaconDistance;
import com.augmate.sdk.beacons.BeaconInfo;
import com.augmate.sdk.logger.Log;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    BeaconDistance beaconDistance = new BeaconDistance();
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconDistance.configureFromContext(this);
        beaconDistance.startListening();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map<Integer, BeaconInfo> beaconDistances = beaconDistance.getLatestBeaconDistances();

                String displayMsg = String.format("%d beacons are within the area\n", beaconDistances.size());

                Log.debug("We know about %d beacons within the area", beaconDistances.size());
                for (BeaconInfo beacon : beaconDistances.values()) {
                    Log.debug("  beacon %s %d is %.2f units away", beacon.beaconName, beacon.uniqueId, beacon.distance);

                    displayMsg += String.format("%s %d is %.2f units away\n", beacon.beaconName, beacon.uniqueId, beacon.distance);
                }

                Message msg = displayMsgUpdater.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("msg", displayMsg);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }, 0, 2000);
    }

    private Handler displayMsgUpdater = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String displayMsg = msg.getData().getString("msg");
            ((TextView) findViewById(R.id.beaconScanResults)).setText(displayMsg);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        beaconDistance.stopListening();
    }
}
