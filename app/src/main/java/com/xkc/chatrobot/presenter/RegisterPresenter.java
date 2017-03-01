package com.xkc.chatrobot.presenter;

import android.content.Context;
import android.util.Log;

import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.Helper.Util;
import com.xkc.chatrobot.callbacks.RegisterCallback;
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

public class RegisterPresenter {
    private Context context;
    private String username;
    private String password;
    private static final String TAG = RegisterPresenter.class.getSimpleName();
    public RegisterPresenter(Context context, HashMap<String,String> params){
        this.context = context;
        if (params != null){
            if (params.containsKey("username")){
                username = params.get("username");
            }
            if (params.containsKey("password")){
                password = params.get("password");
            }
        }

    }

    public  void doRegister(final RegisterCallback callback){
        new MyAsyncTask(context,"register"){
            @Override
            protected String doInBackground(String... params) {
                if (Util.hasNetwork(context)) {
                    String result = "";
                    if (username == null || username.equals("")
                            || password == null || password.equals("")) {
                        return Const.NULL_PARAMS;
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.accumulate("username", username);
                        jsonObject.accumulate("password", password);
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
                        url = new URL(Const.register_url);
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
                        result = Const.NETWORK_ERROR;
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }

                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException e) {
                            result = Const.NETWORK_ERROR;
                            Log.e(TAG, "Writer Close IOException occur: " + e.getMessage());
                        }


                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            result = Const.NETWORK_ERROR;
                            Log.e(TAG, "Reader Close IOException occur: " + e.getMessage());
                        }

                    }


                    return result;
                }else {
                    return Const.NETWORK_ERROR;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }

                Log.d(TAG,"res:"+s);
                parseRegisterRes(callback,s);

                super.onPostExecute(s);
            }
        }.execute(username,password);
    }

    /**
     * 解析注册的json结果
     * @param s
     */
    private void parseRegisterRes(RegisterCallback callback, String s){
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("state")){
                switch (jsonObject.getInt("state")){
                    case 0:
                        if (jsonObject.has("userid")){
                            callback.onSuccess(null,jsonObject.getLong("userid"));
                        }else {
                            callback.onFail(new Exception(Const.REGISTER_FAIL),Const.NO_USERID);
                        }
                        break;
                    case 1:
                        callback.onFail(new Exception(Const.REGISTER_FAIL),Const.REGISTER_FAIL);
                        break;
                    case 2:
                        callback.onFail(new Exception(Const.REGISTER_FAIL),Const.USER_EXISTED);
                        break;
                }
            }else {
                if (s.equalsIgnoreCase(Const.NULL_PARAMS))
                    callback.onFail(new Exception(Const.REGISTER_FAIL),"username或password"+Const.NULL_PARAMS);
                else if (s.equalsIgnoreCase(Const.NETWORK_ERROR))
                    callback.onFail(new Exception(Const.REGISTER_FAIL),Const.NETWORK_ERROR);
                else {
                    callback.onFail(new Exception(Const.REGISTER_FAIL),Const.ERROR);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG,"JsonException:"+e.getMessage());
            callback.onFail(new Exception(Const.REGISTER_FAIL),Const.NETWORK_ERROR);
        }
    }
}
