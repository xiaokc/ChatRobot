package com.xkc.chatrobot.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.R;
import com.xkc.chatrobot.callbacks.LoginCallback;
import com.xkc.chatrobot.callbacks.RegisterCallback;
import com.xkc.chatrobot.presenter.LoginPresenter;
import com.xkc.chatrobot.presenter.RegisterPresenter;

import java.util.HashMap;

/**
 * Created by xkc on 11/14/16.
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = SignInActivity.class.getSimpleName();
    private EditText username_et;
    private EditText password_et;
    private TextInputLayout confirm_password_layout;
    private EditText confirm_password_et;
    private TextView register_tv;
    private TextView login_tv;
    private TextView go_register_tv;

    //存储已注册用户的登录名和密码
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //是否已注册
    private boolean hasRegistered;

    private int userid;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        initView();

        initEvent();

        preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        editor = preferences.edit();

        //如果preferences中已经有userid，说明之前已经注册过
        //将preferences中的username和password读出放到对应的EditText中
        hasRegistered = preferences.contains("userid");

        initUI();

    }

    /**
     * 如果hasRegistered为true
     * 将已经注册的username和password从preferences中读出显示在当前UI中
     */
    private void initUI() {
        if (hasRegistered) {
            username_et.setText(preferences.getString("username", ""));
            password_et.setText(preferences.getString("password", ""));
            userid = preferences.getInt("userid",-1);
        }
    }


    private void initEvent() {
        register_tv.setOnClickListener(this);
        login_tv.setOnClickListener(this);
        go_register_tv.setOnClickListener(this);
    }

    private void initView() {
        username_et = (EditText) findViewById(R.id.username_et);

        password_et = (EditText) findViewById(R.id.password_et);
        confirm_password_layout = (TextInputLayout) findViewById(R.id.confirm_password_layout);
        confirm_password_et = (EditText) findViewById(R.id.confirm_password_et);

        register_tv = (TextView) findViewById(R.id.register_tv);
        login_tv = (TextView) findViewById(R.id.login_tv);

        go_register_tv = (TextView) findViewById(R.id.go_register_tv);


    }

    private RegisterCallback registerCallback = new RegisterCallback() {
        @Override
        public void onSuccess(Exception e, Object obj) {
            if (e == null) {
                Toast.makeText(SignInActivity.this, Const.REGISTER_SUCCESS, Toast.LENGTH_LONG).show();
                editor.putInt("userid", (Integer) obj);
                editor.commit();
            } else {
                Log.e(TAG, Const.REGISTER_FAIL + obj.toString());
            }

        }

        @Override
        public void onFail(Exception e, Object obj) {
            Log.e(TAG, Const.REGISTER_FAIL + obj.toString());
        }
    };

    private LoginCallback loginCallback = new LoginCallback() {
        @Override
        public void onFail(Exception e, Object obj) {
            Log.e(TAG, Const.LOGIN_FAIL + obj.toString());
        }

        @Override
        public void onSuccess(Exception e, Object obj) {
            if (e == null) {
                Toast.makeText(SignInActivity.this, Const.LOGIN_SUCCESS, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                if (userid != -1)
                    intent.putExtra("userid",userid);
                startActivity(intent);
                Log.d(TAG,"userid:"+userid);
                SignInActivity.this.finish();
            } else {
                Log.e(TAG, Const.LOGIN_FAIL + obj.toString());
            }
        }
    };

    @Override
    public void onClick(View v) {
        String username = username_et.getText().toString().trim();
        String password = password_et.getText().toString().trim();
        String confirm_password = confirm_password_et.getText().toString().trim();

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        switch (v.getId()) {
            case R.id.register_tv:
                RegisterPresenter registerPresenter = new RegisterPresenter(SignInActivity.this, params);
                if (confirm_password.equals(password)) {
                    registerPresenter.doRegister(registerCallback);
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.commit();

                } else {
                    Toast.makeText(this, Const.NOT_SAME_PASSWORD, Toast.LENGTH_LONG).show();
                    confirm_password_et.setText("");
                }

                break;
            case R.id.login_tv:
                LoginPresenter loginPresenter = new LoginPresenter(SignInActivity.this, params);
                loginPresenter.doLogin(loginCallback);
                break;
            case R.id.go_register_tv:
                login_tv.setVisibility(View.INVISIBLE);
                confirm_password_layout.setVisibility(View.VISIBLE);
                register_tv.setVisibility(View.VISIBLE);
                go_register_tv.setVisibility(View.INVISIBLE);

                username_et.setText("");
                password_et.setText("");
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (login_tv.getVisibility() == View.VISIBLE) {
            this.finish();
        } else {
            confirm_password_layout.setVisibility(View.GONE);
            register_tv.setVisibility(View.INVISIBLE);
            login_tv.setVisibility(View.VISIBLE);
            go_register_tv.setVisibility(View.INVISIBLE);
        }
    }
}
