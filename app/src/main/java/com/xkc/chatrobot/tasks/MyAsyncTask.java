package com.xkc.chatrobot.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xkc on 11/14/16.
 */

public class MyAsyncTask extends AsyncTask<String,Integer, String> {
    private Context mContext;
    protected static ProgressDialog mDialog = null;
    private static final Set<String> set = new HashSet<>();
    private String tag;

    public MyAsyncTask(Context context, String tag){
        this.mContext = context;
        this.tag = tag;
    }



    @Override
    protected void onPreExecute() {
        set.add(tag);
        if (mDialog == null){
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage("正在传送数据，请稍等……");
            mDialog.show();
        }

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mDialog != null){
            if (set.contains(tag)){
                set.remove(tag);
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//        super.onProgressUpdate(values);
        int value = values[0];
        mDialog.setProgress(value);
    }
}
