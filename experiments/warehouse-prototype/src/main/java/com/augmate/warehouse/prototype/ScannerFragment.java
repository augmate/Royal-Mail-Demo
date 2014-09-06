package com.augmate.warehouse.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.augmate.sdk.scanner.ScannerFragmentBase;
import com.augmate.sdk.scanner.ScannerVisualDebugger;

/**
 * @author James Davis (Fuzz)
 */
public class ScannerFragment extends ScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        SurfaceView camSurface = (SurfaceView) view.findViewById(R.id.camera_preview);

        setupScannerActivity(camSurface, null);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
