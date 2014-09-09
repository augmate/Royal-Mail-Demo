package com.augmate.apps.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.augmate.apps.LoginActivity;
import com.augmate.apps.R;
import com.augmate.apps.SplashActivity;
import com.augmate.apps.common.FlowUtils;

/**
 * @author James Davis (Fuzz)
 */
public class LoginTests extends ActivityInstrumentationTestCase2<SplashActivity> {
    Activity activity;
    public LoginTests() {
        super(SplashActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }



    public void test01_OpenLoginScreen(){
        Instrumentation.ActivityMonitor monitor = getInstrumentation()
                .addMonitor(LoginActivity.class.getName(), null, false);

        activity = monitor.waitForActivityWithTimeout(FlowUtils.SPLASH_ANIMATION_TOTAL_DURATION + 10000L);

        assertNotNull("LoginActivity did not start", activity);
    }

    public void test02_Dimensions(){
        test01_OpenLoginScreen();

        TextView loginPrompt = ((TextView) activity.findViewById(R.id.tap_to_scan));

        assertNotNull(loginPrompt);


        assertEquals("The text size does not match the expected text size", 45f /*pixels*/, loginPrompt.getTextSize());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (activity != null){
            activity.onBackPressed();
        }
    }

}
