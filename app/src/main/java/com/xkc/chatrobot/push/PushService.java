package com.xkc.chatrobot.push;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by xkc on 11/30/16.
 */

public class PushService extends Service {
    private final String TAG = PushService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate() called");
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand() called");
        new TimerManager(PushService.this);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestory() called");
        super.onDestroy();
    }
}
