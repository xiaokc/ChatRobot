package com.xkc.chatrobot.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.xkc.chatrobot.Helper.Const;
import com.xkc.chatrobot.Helper.DBManager;
import com.xkc.chatrobot.Helper.Util;
import com.xkc.chatrobot.R;
import com.xkc.chatrobot.adapter.ChatTextAdapter;
import com.xkc.chatrobot.callbacks.ChatCallback;
import com.xkc.chatrobot.model.ChatText;
import com.xkc.chatrobot.presenter.ChatPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cn.jpush.android.api.JPushInterface;

import static com.xkc.chatrobot.Helper.Util.getTime;

/**
 * Main Chat UI
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView chat_rv;
    private LinearLayoutManager linearLayoutManager;
    private EditText chat_et;
    private Button send_btn;
    private Button voice_btn;

    private List<ChatText> chat_list;

    private ChatTextAdapter adapter;

    private Intent intent;
    private long userid;

    private TextWatcher textWatcher;//监控EditText的输入状态，如果有文字输入，右边的按钮变为“发送”，否则变为“语音”
    private RecognizerDialog mItaDialog = null;//听写UI

    private String contentString;

    private final String TAG = MainActivity.class.getSimpleName();

    private DBManager dbManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();

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

    }

    private void initView() {
        chat_rv = (RecyclerView) findViewById(R.id.chat_rv);
        chat_et = (EditText) findViewById(R.id.chat_et);
        send_btn = (Button) findViewById(R.id.send_btn);
        voice_btn = (Button) findViewById(R.id.voice_btn);

        chat_list = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);

        // 科大讯飞MSC SDK初始化,创建语音配置对象,APPID为注册讯飞云平台的APPID
        SpeechUtility.createUtility(this,
                SpeechConstant.APPID + "=552fa275");
        mItaDialog = new RecognizerDialog(this, mInitListener);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {//如果输入的内容长度大于0，文本发送按钮出现并可以点击，语音按钮隐藏，不可用
                    send_btn.setEnabled(true);
                    send_btn.setVisibility(View.VISIBLE);

                    voice_btn.setEnabled(false);
                    voice_btn.setVisibility(View.GONE);
                } else {
                    send_btn.setEnabled(false);
                    send_btn.setVisibility(View.GONE);

                    voice_btn.setEnabled(true);
                    voice_btn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };


        chat_et.addTextChangedListener(textWatcher);
        contentString = "";

        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        userid = preferences.getLong("userid", -1L);


    }


    private void initEvent() {
        send_btn.setOnClickListener(this);
        voice_btn.setOnClickListener(this);

        adapter = new ChatTextAdapter(this, chat_list);
        chat_rv.setLayoutManager(linearLayoutManager);
        chat_rv.setAdapter(adapter);

        showChatList();
    }

    private void showChatList() {
        intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(JPushInterface.EXTRA_ALERT)) {
                String message = bundle.getString(JPushInterface.EXTRA_ALERT);
                chat_list.add(new ChatText(ChatText.ROBOT, message, getTime()));
            }

        }

        dbManager = new DBManager(this);
        List<ChatText> list = dbManager.queryListData(userid);
        if (list == null || list.size() == 0){
            chat_list.add(new ChatText(ChatText.ROBOT, "嗨，美好的一天开始啦~", getTime()));
        }else {
            for(ChatText chatText : list){
                chat_list.add(chatText);
            }
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                sendText();
                break;
            case R.id.voice_btn:
                sendVoiceText();
        }

        chat_et.setText("");
    }


    private void sendText() {
        contentString = chat_et.getText().toString();
        String time = getTime();
        ChatText chatText = new ChatText(ChatText.USER, contentString, time);

        chat_list.add(chatText);//add user's chat text to chat list
        adapter.notifyDataSetChanged();
        chat_rv.scrollToPosition(chat_list.size() - 1);
        dbManager.addChatText(userid,chatText);

        doChat();

    }

    private void doChat() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userid", userid);
        params.put("key", Const.tuling_key);
        params.put("info", contentString);
        ChatPresenter presenter = new ChatPresenter(MainActivity.this, params);
        presenter.getAnsFromServer(new ChatCallback() {
            @Override
            public void done(Exception e, Object obj) {
                parse_text_from_server((String) obj);
            }
        });

    }

    /**
     * parse json text from server-end to Robot's chat text
     *
     * @param s
     */
    private void parse_text_from_server(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            int state = jsonObject.getInt("state");
            String text = jsonObject.getString("text");

            if (state == 0) {
                String time = getTime();
                ChatText chatText = new ChatText(ChatText.ROBOT, text, time);
                chat_list.add(chatText);
                adapter.notifyDataSetChanged();
                dbManager.addChatText(userid,chatText);
                chat_rv.scrollToPosition(chat_list.size() - 1);
            } else {
                Log.e(TAG, "parse text from server occurred error," + text);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse JSONException occur: " + e.getMessage());
        }
    }

    /**
     * 发送语音转换的文字
     */
    private void sendVoiceText() {
        //设置UI听写对话框参数
        mItaDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        mItaDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mItaDialog.setParameter(SpeechConstant.DOMAIN, "iat");

        //开始听写
        mItaDialog.setListener(mRecognizerDialogListener);
        mItaDialog.setParameter(SpeechConstant.ASR_PTT, "0");//关闭标点符号


//        显示UI
        mItaDialog.show();
        showTip("请开始讲话");

    }


    /**
     * *********************** 语音听写相关 ****************************
     */
    // 初始化监听器
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private void showTip(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    // 听写UI监听器
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String string = Util.parseIatResult(results.getResultString());
            if (string != null && !string.equals("")) {
                contentString += string;// 解析成文字

                ChatText chatText = new ChatText(ChatText.USER, contentString, getTime());// 点击发送按钮发送的内容标识为用户状态
                chat_list.add(chatText);
                dbManager.addChatText(userid,chatText);//将获取到的数据添加到聊天记录数据库表中
                adapter.notifyDataSetChanged();// 添加完数据之后需要进行重新适配，刷新

                doChat();
            }
            contentString = "";

        }


        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }
    };

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
