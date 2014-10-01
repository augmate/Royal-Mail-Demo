package com.augmate.apps.carsweep;

import android.os.Bundle;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class ClearUpsCacheActivity extends RoboActivity {

    @InjectView(R.id.ups_clear_cache)
    TextView ups_clear_cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_ups_cache);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new CarLoadingDataStore(this).wipeLocalCache(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ups_clear_cache.setText("UPS cache cleared!");
                } else {
                    ups_clear_cache.setText(e.getMessage());
                }
            }
        });
    }
}
