package com.augmate.apps.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.augmate.apps.R;
import com.augmate.apps.common.FlowUtils;
import com.augmate.apps.common.FontHelper;
import roboguice.inject.InjectView;

import java.io.Serializable;

/**
 * @author James Davis (Fuzz)
 */
public class MessageActivity extends BaseActivity {
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String CLASS = "class";
    public static final String DATA = "data";

    private boolean mError = false;
    private String mNextClass = null;
    Serializable data = "";

    @InjectView(R.id.messageLine1)
    TextView messageLine1;

    @InjectView(R.id.messageLine2)
    TextView messageLine2;

    @InjectView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        FontHelper.updateFontForBrightness(messageLine1,messageLine2);
        String message = "";


        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mError = extras.getBoolean(ERROR, false);
            message = extras.getString(MESSAGE, "");
            mNextClass = extras.getString(CLASS, null);
            data = extras.getSerializable(DATA);
        }

        if (mError){
            if (messageLine1 != null){
                messageLine1.setText(message);
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }, FlowUtils.TRANSITION_TIMEOUT);
        } else {
            if (messageLine1 != null){
                messageLine1.setText(message);
            }
            if (messageLine2 != null){
                messageLine2.setVisibility(View.GONE);
            }
            if (imageView != null){
                imageView.setImageResource(R.drawable.verify_check);
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mNextClass != null) {
                        Class<?> nextClass = Class.forName(mNextClass);
                            Intent intent = new Intent(MessageActivity.this, nextClass);
                            if (data != null) {
                                intent.putExtra(DATA, data);
                            }
                            startActivity(intent);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    setResult(RESULT_OK);
                    finish();
                }
            }, FlowUtils.SUCCESS_MESSAGE_DURATION);
        }
    }
}
