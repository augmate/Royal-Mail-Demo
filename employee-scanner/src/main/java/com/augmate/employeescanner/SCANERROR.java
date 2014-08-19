package com.augmate.employeescanner;

/**
* Created by prem on 8/19/14.
*/
enum SCANERROR {
    SCAN_ERROR(R.string.scan_error),
    BIN_ERROR(R.string.bin_id_error),
    TIMEOUT_ERROR(R.string.timeout_error);

    int error_msg;

    SCANERROR(int msg) {
        this.error_msg = msg;
    }
}
