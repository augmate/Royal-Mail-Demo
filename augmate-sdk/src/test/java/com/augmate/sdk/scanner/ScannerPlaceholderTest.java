package com.augmate.sdk.scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ScannerPlaceholderTest{
    @Test
    public void shouldBeAPlaceholder() {
        assertThat(true).isTrue();
    }
}