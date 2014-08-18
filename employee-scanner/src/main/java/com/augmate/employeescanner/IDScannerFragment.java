package com.augmate.employeescanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.augmate.sdk.scanner.ScannerFragmentBase;
import com.augmate.sdk.scanner.ScannerVisualDebugger;

/**
 * Created by premnirmal on 8/18/14.
 */
public class IDScannerFragment extends ScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.id_scan_fragment, container, false);

        ScannerVisualDebugger dbg = (ScannerVisualDebugger) view.findViewById(R.id.scanner_visual_debugger);
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.camera_preview);

        setupScannerActivity(sv, dbg);

        return view;
    }

}
