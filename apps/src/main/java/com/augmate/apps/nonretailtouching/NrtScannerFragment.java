package com.augmate.apps.nonretailtouching;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.augmate.apps.R;
import com.augmate.sdk.scanner.ScannerFragmentBase;

public class NrtScannerFragment extends ScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nrt_scanner, container, false);
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.camera_preview);
        setupScannerActivity(sv, null);
        return view;
    }
}
