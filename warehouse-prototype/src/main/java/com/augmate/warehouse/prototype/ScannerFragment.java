package com.augmate.warehouse.prototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.augmate.sdk.scanner.ScannerFragmentBase;

/**
 * @author James Davis (Fuzz)
 */
public class ScannerFragment extends ScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        SurfaceView camSurface = (SurfaceView) view.findViewById(R.id.camera_preview);
        setupScannerActivity(camSurface, null);
        super.onViewCreated(view, savedInstanceState);
    }
}
