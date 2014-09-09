package com.augmate.apps.scanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.augmate.apps.R;
import com.augmate.sdk.scanner.ScannerVisualDebugger;
import com.augmate.sdk.scanner.TextureScannerFragmentBase;

/**
 * Note about extending ScannerFragmentBase:
 * When overriding methods, make sure to call their base versions
 * Specifically: onPause, onResume, onAttach, onDetach
 */
public class ScanFragment extends TextureScannerFragmentBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box_scan, container, false);

        ScannerVisualDebugger dbg = (ScannerVisualDebugger) view.findViewById(R.id.scanner_visual_debugger);
        @SuppressLint("WrongViewCast") TextureView sv = (TextureView) view.findViewById(R.id.camera_preview);

        setupScannerActivity(sv, dbg);

        return view;
    }
}
