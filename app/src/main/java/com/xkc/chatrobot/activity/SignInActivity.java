package com.xkc.chatrobot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.R;
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

/**
 * Created by xkc on 11/14/16.
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = SignInActivity.class.getSimpleName();
    private TextInputLayout username_layout;
    private TextInputLayout password_layout;
    private EditText username_et;
    private EditText password_et;
    private Button register_btn;
    private Button login_btn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        initView();

        initEvent();

    }

    private void initEvent() {
        register_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);
    }

    private void initView() {
        username_layout = (TextInputLayout) findViewById(R.id.username_layout);
        username_et = (EditText) findViewById(R.id.username_et);

        password_layout = (TextInputLayout) findViewById(R.id.password_layout);
        password_et = (EditText) findViewById(R.id.password_et);

        register_btn = (Button) findViewById(R.id.register_btn);
        login_btn = (Button) findViewById(R.id.login_btn);


    }

    private RegisterCallback registerCallback = new RegisterCallback() {
        @Override
        public void onSuccess(Exception e, Object obj) {
            if (e == null){
                Toast.makeText(SignInActivity.this,Const.REGISTER_SUCCESS,Toast.LENGTH_LONG).show();
            }else {
                Log.e(TAG,Const.REGISTER_FAIL + obj.toString());
            }

        }

        @Override
        public void onFail(Exception e, Object obj) {
            Log.e(TAG,Const.REGISTER_FAIL+ obj.toString());
        }
    };

    private void doRegister(final RegisterCallback callback, String username, String password){
        new MyAsyncTask(this,"register"){
            @Override
            protected String doInBackground(String... params) {
                String username = params[0];
                String password = params[1];

                String result = "";

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
                            callback.onSuccess(null,jsonObject.getInt("userid"));
                        }else {
                            callback.onFail(new Exception(Const.NO_USERID),Const.NO_USERID);
                        }
                        break;
                    case 1:
                        callback.onFail(new Exception(Const.REGISTER_FAIL),Const.REGISTER_FAIL);
                        break;
                    case 2:
                        callback.onFail(new Exception(Const.USER_EXISTED),Const.USER_EXISTED);
                        break;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG,"JsonException:"+e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        String username = username_et.getText().toString().trim();
        String password = password_et.getText().toString().trim();
        switch (v.getId()){
            case R.id.register_btn:
                doRegister(registerCallback,username,password);
                break;
            case R.id.login_btn:
                /*doLogin(username,password);*/
                break;
        }

    }


    class LoginSuccessReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


}
