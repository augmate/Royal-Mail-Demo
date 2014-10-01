package com.augmate.sdk.scanner.bluetooth;

import android.text.TextUtils;
import com.augmate.sdk.logger.Log;

import java.util.ArrayList;

public class BarcodeStreamParser {
    private String accumulationBuffer = "";
    private ResultHandler resultHandler;

    public interface ResultHandler {
        public void onBarcodeDecoded(String barcode);
    }

    public BarcodeStreamParser(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public void onNewData(int read, byte[] buffer) {
        accumulationBuffer += new String(buffer, 0, read);

        // normalize line-endings
        accumulationBuffer = accumulationBuffer.replace("\r\n", "\n");
        accumulationBuffer = accumulationBuffer.replace("\r", "\n"); // scanfob likes to use \r

        // wait until we have a terminator
        int indexOfEOL = accumulationBuffer.indexOf("\n");
        if(indexOfEOL == -1)
            return;

        // grab substring without normalized terminator
        String substring = accumulationBuffer.substring(0, indexOfEOL);

        // advance buffer in case of partials
        accumulationBuffer = accumulationBuffer.substring(indexOfEOL+1);

        //dumpRawBuffer(substring);

        // if the substring contains a meaningful amount of data, try to parse it
        if(substring.length() > 3)
            tryToParseStream(substring);
    }

    private void dumpRawBuffer(String substring) {
        ArrayList<String> bytes = new ArrayList<>();
        for (int i = 0; i < substring.length(); i++) {
            bytes.add(String.format("%X", (byte) substring.charAt(i)));
        }
        Log.debug("Raw input: [%s] (%d bytes)", TextUtils.join(",", bytes), substring.length());
    }

    /**
     * typical formats
     * scanfob 2006
     *   default: prefix=STX, suffix=CR
     *                   0x02, 0x0A
     *   strangely their CR is actually \n, but it can sometimes be \r
     */
    private void tryToParseStream(String substring) {
        if (substring.charAt(0) == 0x02) {
            // Scanfob STX+CR
            substring = substring.substring(1);
            Log.debug("Decoded Scanfob STX+CR format: [%s]", substring);

            resultHandler.onBarcodeDecoded(substring);
        } else {
            // Ecom ES 301 scanner
            resultHandler.onBarcodeDecoded(substring);
        }
    }
}
