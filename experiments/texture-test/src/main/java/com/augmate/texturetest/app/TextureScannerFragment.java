package com.augmate.texturetest.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import com.augmate.sdk.scanner.ScannerFragmentTextureBase;
import com.augmate.sdk.scanner.ScannerVisualDebugger;


public class TextureScannerFragment extends ScannerFragmentTextureBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.id_scan_fragment, container, false);

        ScannerVisualDebugger dbg = (ScannerVisualDebugger) view.findViewById(R.id.scanner_visual_debugger);
        TextureView tv = (TextureView) view.findViewById(R.id.camera_preview);
        setupScannerActivity(tv, dbg);

        return view;
    }

}
