package com.xkc.chatrobot.presenter;

import android.content.Context;
import android.util.Log;

import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.callbacks.ChatCallback;
import com.xkc.chatrobot.tasks.MyAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by xkc on 11/15/16.
 */

public class ChatPresenter {
    private Context context;
    private long userid;
    private String key;
    private String info;
    private boolean requestSentiment;
    private final String TAG = ChatPresenter.class.getSimpleName();
    public ChatPresenter(Context context, HashMap<String,Object> params){
        this.context = context;
        this.userid = (long) params.get("userid");
        this.key = (String) params.get("key");
        this.info = (String) params.get("info");
        this.requestSentiment = (boolean) params.get("sentiment");

    }

    public void getAnsFromServer(final ChatCallback chatCallback){
        new MyAsyncTask(context,"chat"){
            @Override
            protected void onPreExecute() {
                if (mDialog != null){
                    mDialog.dismiss();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                String result = "";
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.accumulate("key", key);
                    jsonObject.accumulate("info", info);
                    jsonObject.accumulate("userid",userid);
                    jsonObject.accumulate("sentiment",requestSentiment);

                } catch (JSONException e) {
                    Log.e(TAG, "Post JSONException occur: " + e.getMessage());
                }

                String requestParams = jsonObject.toString();

                URL url = null;
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                BufferedWriter writer = null;

                Log.i(TAG, "requestParams=" + requestParams);
                try {
                    url = new URL(Const.chat_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(Const.connect_timeout);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestProperty("Content-type", "application/json");

                    connection.connect();
                    writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write(requestParams);
                    writer.flush();


                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }

                    Log.i(TAG, "requestParams=" + requestParams + ",result=" + result);
                } catch (IOException e) {
                    Log.e(TAG, "IOException occur: " + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Writer Close IOException occur: " + e.getMessage());
                    }


                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Reader Close IOException occur: " + e.getMessage());
                    }

                }

                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                chatCallback.done(null,s);
                super.onPostExecute(s);
            }
        }.execute();
    }

}
