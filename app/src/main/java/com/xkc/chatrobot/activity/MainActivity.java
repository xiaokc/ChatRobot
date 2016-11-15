package com.xkc.chatrobot.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xkc.chatrobot.Helper.Const;
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

/**
 * Main Chat UI
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView chat_rv;
    private LinearLayoutManager linearLayoutManager;
    private EditText chat_et;
    private Button send_btn;

    private List<ChatText> chat_list;

    private ChatTextAdapter adapter;

    private Intent intent;
    private int userid;

    private final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = getIntent();
        userid = intent.getIntExtra("userid", -1);

        initView();
        initEvent();

    }

    private void initView() {
        chat_rv = (RecyclerView) findViewById(R.id.chat_rv);
        chat_et = (EditText) findViewById(R.id.chat_et);
        send_btn = (Button) findViewById(R.id.send_btn);

        chat_list = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);

    }


    private void initEvent() {
        send_btn.setOnClickListener(this);

        adapter = new ChatTextAdapter(this, chat_list);
        chat_rv.setLayoutManager(linearLayoutManager);
        chat_rv.setAdapter(adapter);

        chat_list.add(new ChatText(ChatText.ROBOT, "Welcome", Util.getTime()));
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                doChat();
                break;
        }

        chat_et.setText("");
    }

    private void doChat() {
        String text = chat_et.getText().toString();
        String time = Util.getTime();
        ChatText chatText = new ChatText(ChatText.USER, text, time);

        chat_list.add(chatText);//add user's chat text to chat list
        adapter.notifyDataSetChanged();
        chat_rv.scrollToPosition(chat_list.size() - 1);

        HashMap<String, Object> params = new HashMap<>();
        params.put("userid", userid);
        params.put("key", Const.tuling_key);
        params.put("info", text);
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
                String time = Util.getTime();
                ChatText chatText = new ChatText(ChatText.ROBOT, text, time);
                chat_list.add(chatText);
                adapter.notifyDataSetChanged();

                chat_rv.scrollToPosition(chat_list.size() - 1);
            } else {
                Log.e(TAG, "parse text from server occurred error," + text);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parse JSONException occur: " + e.getMessage());
        }
    }

}
