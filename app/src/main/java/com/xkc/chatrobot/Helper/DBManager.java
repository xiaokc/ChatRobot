package com.xkc.chatrobot.Helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xkc.chatrobot.model.ChatText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xkc on 11/20/16.
 */

public class DBManager {
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private final String TAG = DBManager.class.getSimpleName();

    public DBManager(Context context){
        dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    public void deleteLastDayData(){
        
    }

    //在数据库中添加一条聊天记录
    public void addChatText(long userid, ChatText chatText) {
        Log.i(TAG, "addChatText() is called");
        db.beginTransaction();
        String addSql = "insert into chat(userid,chat_time,chat_content,chat_flag) values(?,?,?,?)";

        String content = chatText.getContent();//聊天记录内容

        String time = chatText.getTime();//聊天记录时间
        int  flag = chatText.getFlag();//聊天标识，1表示用户，2表示机器人

        try {
            db.execSQL(addSql, new Object[]{userid,time, content, flag});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }


    /**
     * 返回对应对应userid的所有聊天记录
     * @param userid
     * @return
     */
    public List<ChatText> queryListData(long userid){
        Log.d(TAG,"queryListData() is called");
        List<ChatText> datas = new ArrayList<>();
        cursor = queryUserid(userid);
        cursor.moveToFirst();
        Log.d(TAG,"cursor count :"+cursor.getCount());
        if (cursor.getCount() > 0) {
            do {
                ChatText chatText = new ChatText();
                chatText.setContent(cursor.getString(cursor.getColumnIndexOrThrow("chat_content")));
                chatText.setFlag(cursor.getInt(cursor.getColumnIndexOrThrow("chat_flag")));
                chatText.setTime(cursor.getString(cursor.getColumnIndexOrThrow("chat_time")));

                datas.add(chatText);
            } while (cursor.moveToNext());
        }
        return datas;
    }

    /**
     * 查询userid的游标
     * @param userid
     * @return 最多10条数据
     */
    private Cursor queryUserid(long userid){
        Log.i(TAG,"queryUserid() is called");
        cursor = db.rawQuery("select * from chat where userid = ? " +
                "And chat_time >= date('now','start of day')", new String[]{String.valueOf(userid)});
        return cursor;
    }
}
