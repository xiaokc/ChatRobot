package com.xkc.chatrobot.Helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xkc on 6/18/16.
 */
public class Util {
    public static String getTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();

        String time = format.format(curDate);
        return time;
    }
}
