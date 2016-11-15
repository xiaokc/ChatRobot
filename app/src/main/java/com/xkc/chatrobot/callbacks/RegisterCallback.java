package com.xkc.chatrobot.callbacks;

/**
 * Created by xkc on 11/14/16.
 */

public interface RegisterCallback {
    void onSuccess(Exception e, Object obj);
    void onFail(Exception e, Object obj);
}
