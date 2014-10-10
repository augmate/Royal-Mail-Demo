package com.augmate.sdk.scanner;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.augmate.sdk.R;
import com.augmate.sdk.logger.Log;
import com.augmate.sdk.scanner.decoder.DecodingJob;

import java.util.concurrent.CountDownLatch;

final class DecoderThread extends Thread {
    private DecodingThreadMessages messagePump;
    private CountDownLatch isMsgPumpReady; // cross-thread synchronization (TODO: is there a simpler lock available?)
    private Handler producerThreadHandler;

    DecoderThread(Handler producerThreadHandler) {
        super();
        this.producerThreadHandler = producerThreadHandler;
        this.isMsgPumpReady = new CountDownLatch(1);
        Log.debug("New Decoding-thread created by thread id=%d", Thread.currentThread().getId());
    }

    /**
     * blocks (activity thread) waiting until decoding thread entry-point has been hit
     * and the decode-thread msg handler has been created. it then returns that cross-thread msg pump.
     *
     * @return
     */
    Handler getMessagePump() {
        try {
            isMsgPumpReady.await();
        } catch (InterruptedException e) {
            Log.exception(e, "Failed to wait for initialization latch to unlock for decoding thread");
        }
        return messagePump;
    }

    // entry-point into new thread
    // spin up a message handler to listen to the message loop
    @Override
    public void run() {
        Thread.currentThread().setName("decoder");
        Log.debug("Running thread with id=%d", Thread.currentThread().getId());
        Looper.prepare();
        messagePump = new DecodingThreadMessages();
        isMsgPumpReady.countDown(); // release latch, allowing msg pump to be used by other threads
        Looper.loop();
        Log.debug("Exiting decoding thread.");
    }

    private void shutdown() {
        Log.debug("Shutting down message pump..");
        // remove any queued messages
        messagePump.removeMessages(R.id.scannerFragmentJobCompleted);
        // fun fact: messagePump.getLooper() is identical to Looper.myLooper()
        messagePump.getLooper().quit();
    }

    private final class DecodingThreadMessages extends Handler {
        private DecoderManager decoderManager = new DecoderManager();

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.decodingThreadNewJob) {
                processDecodeJobRequest((DecodingJob) msg.obj);
            } else if (msg.what == R.id.decodingThreadShutdown) {
                shutdown();
            }
        }

        private void processDecodeJobRequest(DecodingJob job) {
            //Log.debug("Got decoding job with buffer @ 0x%X", job.getLuminance().hashCode());

            // the good stuff begins here
            decoderManager.process(job);

            producerThreadHandler
                    .obtainMessage(R.id.scannerFragmentJobCompleted, job)
                    .sendToTarget();
        }
    }
}
