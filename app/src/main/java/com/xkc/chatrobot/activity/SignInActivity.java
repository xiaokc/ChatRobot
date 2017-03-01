package com.xkc.chatrobot.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xkc.chatrobot.helper.Const;
import com.xkc.chatrobot.R;
import com.xkc.chatrobot.callbacks.LoginCallback;
import com.xkc.chatrobot.callbacks.RegisterCallback;
import com.xkc.chatrobot.presenter.LoginPresenter;
import com.xkc.chatrobot.presenter.RegisterPresenter;

import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

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

    private long userid;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            }

            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            }

        }

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
            userid = preferences.getLong("userid",-1L);
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
                Log.i(TAG,"register callback obj:"+obj.toString());
                editor.putLong("userid", (Long) obj);
                editor.commit();

                userid = preferences.getLong("userid",-1L);

                goLogin();
            } else {
                Log.e(TAG, Const.REGISTER_FAIL + obj.toString());
            }

        }

        @Override
        public void onFail(Exception e, Object obj) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
            builder.setTitle(Const.REGISTER_FAIL);
            builder.setMessage(obj.toString());
            builder.show();

            Log.e(TAG, Const.REGISTER_FAIL + obj.toString());
        }
    };

    private void goLogin(){
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage("马上登录？")
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("username", preferences.getString("username", ""));
                        params.put("password", preferences.getString("password", ""));
                        LoginPresenter loginPresenter = new LoginPresenter(SignInActivity.this, params);
                        loginPresenter.doLogin(loginCallback);
                    }
                })
                .setNegativeButton("不了，谢谢",null)
                .show();

    }

    private LoginCallback loginCallback = new LoginCallback() {
        @Override
        public void onFail(Exception e, Object obj) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
            builder.setTitle(Const.LOGIN_FAIL);
            builder.setMessage(obj.toString());
            builder.show();

            Log.e(TAG, Const.LOGIN_FAIL + obj.toString());
        }

        @Override
        public void onSuccess(Exception e, Object obj) {
            if (e == null) {

                Toast.makeText(SignInActivity.this, Const.LOGIN_SUCCESS, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                Log.i(TAG,"userid="+userid);
                if (userid != -1){
                    //如果登陆成功，设置用户别名为userid
                    JPushInterface.setAlias(getApplicationContext(), String.valueOf(userid), new TagAliasCallback() {
                        @Override
                        public void gotResult(int i, String s, Set<String> set) {
                            Log.i(TAG,"set alias success!");
                        }
                    });
                }

                startActivity(intent);
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
                boolean isValidRegister = notNullRegisterParams(username,password,confirm_password);
                if(isValidRegister) {
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
                }else {
                 new AlertDialog.Builder(SignInActivity.this)
                         .setMessage("请填入用户名、密码和确认密码")
                         .show();
                }

                break;
            case R.id.login_tv:
                boolean isValidLogin = notNullLoginParams(username,password);
                if (isValidLogin) {
                    LoginPresenter loginPresenter = new LoginPresenter(SignInActivity.this, params);
                    loginPresenter.doLogin(loginCallback);
                }else {
                    new AlertDialog.Builder(SignInActivity.this)
                            .setMessage("请填入用户名和密码")
                            .show();
                }
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

    private boolean notNullRegisterParams(String username, String password, String confirmPassword){
        return ! username.equals("") && ! password.equals("") && ! confirmPassword.equals("");
    }

    private boolean notNullLoginParams(String username, String password){
        return ! username.equals("") && ! password.equals("");
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

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
