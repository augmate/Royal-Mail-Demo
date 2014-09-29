package com.augmate.sdk.scanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Note about extending ScannerFragmentBase:
 * When overriding methods, make sure to call their base versions
 * Specifically: onPause, onResume, onAttach, onDetach
 */
public class ScannerFragmentDefault extends ScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_default_scanner, container, false);

        ScannerVisualDebugger dbg = (ScannerVisualDebugger) view.findViewById(R.id.scanner_visual_debugger);
        @SuppressLint("WrongViewCast") SurfaceView sv = (SurfaceView) view.findViewById(R.id.camera_preview);

        setupScannerActivity(sv, dbg);

        return view;
    }
}
