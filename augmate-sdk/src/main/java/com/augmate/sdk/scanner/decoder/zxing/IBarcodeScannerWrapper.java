package com.augmate.sdk.scanner.decoder.zxing;

import com.augmate.sdk.scanner.decoder.DecodingJob;

public interface IBarcodeScannerWrapper {
    void process(DecodingJob job);
}
