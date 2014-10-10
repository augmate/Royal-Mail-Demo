package com.augmate.sdk.scanner;

import android.os.SystemClock;
import com.augmate.sdk.logger.Log;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class NativeUtilsTest {
    @Test
    public void shouldPass() {
        assertThat(true).isTrue();
    }

    @Ignore
    public void shouldBinarizeArray() {
        byte[] src = new byte[1920 * 1200 * 4];
        int[] dst = new int[1920 * 1200];

        long start = SystemClock.elapsedRealtime();
        NativeUtils.binarizeToIntBuffer(src, dst, 1920, 1200);
        long span = SystemClock.elapsedRealtime() - start;

        Log.debug("Binarizer took: %d msec", span);

        System.out.println("please print");

        assertThat(span < 3000).isTrue();

        assertThat(true).isFalse();
    }
}