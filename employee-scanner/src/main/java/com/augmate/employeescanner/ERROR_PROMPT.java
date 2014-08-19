package com.augmate.employeescanner;

/**
* Created by prem on 8/19/14.
*/
enum ERROR_PROMPT {
    SCAN_ERROR(R.string.scan_error),
    BIN_ERROR(R.string.bin_id_error),
    TIMEOUT_ERROR(R.string.timeout_error),
    TRY_AGAIN(R.string.try_again);

    int error_msg;

    ERROR_PROMPT(int msg) {
        this.error_msg = msg;
    }
}
