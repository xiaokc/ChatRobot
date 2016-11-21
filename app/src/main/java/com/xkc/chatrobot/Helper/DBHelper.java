package com.xkc.chatrobot.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by xkc on 11/20/16.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/chatrobot/chat.db";
    private static final int DATABASE_VERSION = 1;
    private final String TAG = DBHelper.class.getSimpleName();

    private static final String CREATE_TABLE_SQL =
            "create table chat(_id integer primary key autoincrement,"
                    + "userid int(4),"
                    + "chat_time varchar(20),"
                    + "chat_content varchar(1000),"
                    + "chat_flag int(4))";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "DBHelper() is called");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
