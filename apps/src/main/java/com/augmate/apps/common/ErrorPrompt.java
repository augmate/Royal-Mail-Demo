package com.augmate.apps.common;

import com.augmate.apps.AugmateApplication;
import com.augmate.apps.R;

/**
* Created by prem on 8/19/14.
*/
public enum ErrorPrompt {
    SCAN_ERROR(R.string.error_scan),
    BIN_ERROR(R.string.error_bin),
    TIMEOUT_ERROR(R.string.error_timed_out),
    SOUND_ERROR(R.string.error_listening),
    NETWORK_ERROR(R.string.error_network),
    NUMBER_ERROR(R.string.error_number);

    int error_msg;

    ErrorPrompt(int msg) {
        this.error_msg = msg;
    }

    public String getString() {
        return AugmateApplication.getContext().getString(error_msg);
    }
}
