package com.augmate.apps.carsweep;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.datastore.CarLoadingDataStore;
import com.parse.DeleteCallback;
import com.parse.ParseException;

public class ClearUpsCacheActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_ups_cache);

        final TextView view = (TextView) findViewById(R.id.ups_clear_cache);

        new CarLoadingDataStore(this).wipeLocalCache(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    view.setText("UPS cache cleared!");
                } else {
                    view.setText(e.getMessage());
                }
            }
        });
    }
}
