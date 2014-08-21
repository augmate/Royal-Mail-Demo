package com.augmate.sample.common;

import com.augmate.sample.AugmateApplication;
import com.augmate.sample.R;

/**
* Created by prem on 8/19/14.
*/
public enum ErrorPrompt {
    SCAN_ERROR(R.string.error_scan),
    BIN_ERROR(R.string.error_bin),
    TIMEOUT_ERROR(R.string.error_timed_out),
    TRY_AGAIN(R.string.message_try_again);

    int error_msg;

    ErrorPrompt(int msg) {
        this.error_msg = msg;
    }

    public String getString() {
        return AugmateApplication.getContext().getString(error_msg);
    }
}
