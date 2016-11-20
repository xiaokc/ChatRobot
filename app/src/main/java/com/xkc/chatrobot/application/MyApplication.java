package com.xkc.chatrobot.application;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by xkc on 11/19/16.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
