package com.xkc.chatrobot.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xkc on 11/30/16.
 */

public class TimerManager {
    //时间间隔

    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
    private Context context;
    private final String TAG = TimerManager.class.getSimpleName();


    public TimerManager(Context context) {
        Log.d(TAG,"TimerManager() called");
        this.context = context;

        Calendar calendar = Calendar.getInstance();


        /*** 定制每日15:00执行方法 ***/


        calendar.set(Calendar.HOUR_OF_DAY, 21);

        calendar.set(Calendar.MINUTE, 7);

        calendar.set(Calendar.SECOND, 0);


        Date date = calendar.getTime(); //第一次执行定时任务的时间


        //如果第一次执行定时任务的时间 小于 当前的时间

        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。

        if (date.before(new Date())) {

            date = this.addDay(date, 1);

        }


        Timer timer = new Timer();


        PushTask task = new PushTask();

        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。

        timer.schedule(task, date, PERIOD_DAY);

    }


    // 增加或减少天数

    public Date addDay(Date date, int num) {

        Calendar startDT = Calendar.getInstance();

        startDT.setTime(date);

        startDT.add(Calendar.DAY_OF_MONTH, num);

        return startDT.getTime();

    }

    private class PushTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG,"PushTask{run()} is called");
            Socket socket = null;
            BufferedWriter writer = null;
            BufferedReader reader = null;
            try {
                socket = new Socket("10.3.200.10", 1130);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                long userid = context.getSharedPreferences(
                        "user_info",Context.MODE_PRIVATE).getLong("userid",-1);

                if (isRunning(context)){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.accumulate("userid", userid);
                        jsonObject.accumulate("isRunning",true);
                    } catch (JSONException e) {
                        Log.e(TAG,"isRunning() json exception:"+e.getMessage());
                    }

                    writer.write(jsonObject.toString());
                    writer.flush();
                    String reply;
                    while ((reply = reader.readLine()) != null){
                        Log.d(TAG,"reply is not null : "+reply);
                        Intent intent = new Intent();
                        intent.setAction("com.xkc.chatrobot.activity.MainActivity");
                        intent.putExtra("reply",reply);
                        context.sendBroadcast(intent);
                        Log.d(TAG,"send broadcast");
                    }
                }else {
                    //讲道理，如果activity已经finish了，这里根本运行不到 gg
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.accumulate("userid",userid);
                        jsonObject.accumulate("isRunning",false);

                        writer.write(jsonObject.toString());
                        writer.flush();
                    } catch (JSONException e) {
                        Log.e(TAG,"isNOTRunning() json exception:"+e.getMessage());
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回activity是否处于running状态
     *
     * @param context
     * @return
     */
    private boolean isRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

}
