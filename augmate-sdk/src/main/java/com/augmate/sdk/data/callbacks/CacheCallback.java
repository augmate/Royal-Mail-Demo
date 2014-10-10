package com.augmate.sdk.data.callbacks;

import com.parse.ParseException;
import com.parse.SaveCallback;

public abstract class CacheCallback extends SaveCallback {

    @Override
    final public void done(ParseException e) {
        if(e == null){
            success();
        }else{
            error(e);
        }
    }

    public abstract void success();

    public abstract void error(ParseException e);
}
