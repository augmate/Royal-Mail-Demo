package com.augmate.warehouse.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import roboguice.inject.InjectView;

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
    String data = "";

    @InjectView(R.id.messageLine1)
    TextView messageLine1;

    @InjectView(R.id.messageLine1)
    TextView messageLine2;

    @InjectView(R.id.messageLine1)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error);
        String message = "";


        Bundle extras = getIntent().getExtras();
        if (extras != null){
            mError = extras.getBoolean(ERROR, false);
            message = extras.getString(MESSAGE, "");
            mNextClass = extras.getString(CLASS, null);
            data = extras.getString(DATA, "");
        }

        if (mError){
            if (messageLine1 != null){
                messageLine1.setText(message);
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        } else if (mNextClass != null && !mNextClass.isEmpty()){
            if (messageLine1 != null){
                messageLine1.setText(message);
            }
            if (messageLine2 != null){
                messageLine2.setVisibility(View.GONE);
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
