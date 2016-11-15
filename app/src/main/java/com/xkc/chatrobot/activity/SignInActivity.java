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
import com.xkc.chatrobot.presenter.RegisterPresenter;
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
    @Override
    public void onClick(View v) {
        String username = username_et.getText().toString().trim();
        String password = password_et.getText().toString().trim();

        HashMap<String,String> params = new HashMap<>();
        params.put("username",username);
        params.put("password",password);
        switch (v.getId()){
            case R.id.register_btn:
                RegisterPresenter registerPresenter = new RegisterPresenter(SignInActivity.this,params);
                registerPresenter.doRegister(registerCallback);
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
