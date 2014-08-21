package com.augmate.sample.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.augmate.sample.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        TextView message1 = ((TextView) findViewById(R.id.messageLine1));
        TextView message2 = ((TextView) findViewById(R.id.messageLine2));
        ImageView imageView = ((ImageView) findViewById(R.id.imageView));
        String message = "";


        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mError = extras.getBoolean(ERROR, false);
            message = extras.getString(MESSAGE, "");
            mNextClass = extras.getString(CLASS, null);
            data = extras.getSerializable(DATA);
        }

        if (mError){
            if (message1 != null){
                message1.setText(message);
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        } else if (mNextClass != null && !mNextClass.isEmpty()){
            if (message1 != null){
                message1.setText(message);
            }
            if (message2 != null){
                message2.setVisibility(View.GONE);
            }
            if (imageView != null){
                imageView.setImageResource(R.drawable.success);
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Class<?> nextClass = Class.forName(mNextClass);
                        Intent intent = new Intent(MessageActivity.this, nextClass);
                        if (data != null) {
                            intent.putExtra(DATA, data);
                        }
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }, 1500);
        }
    }
}
