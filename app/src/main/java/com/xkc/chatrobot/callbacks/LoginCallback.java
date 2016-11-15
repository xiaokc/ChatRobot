package com.xkc.chatrobot.callbacks;

/**
 * Created by xkc on 11/15/16.
 */

public interface LoginCallback {
    void onFail(Exception e, Object obj);
    void onSuccess(Exception e, Object obj);
}
