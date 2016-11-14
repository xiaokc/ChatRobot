package com.xkc.chatrobot.activity;

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
import com.xkc.chatrobot.model.ChatText;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        adapter = new ChatTextAdapter(this,chat_list);
        chat_rv.setLayoutManager(linearLayoutManager);
        chat_rv.setAdapter(adapter);

        chat_list.add(new ChatText(ChatText.ROBOT,"Welcome",Util.getTime()));
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                send_to_local_server();

                break;
        }

        chat_et.setText("");
    }

    /**
     * send user's chat text to server
     */
    private void send_to_tuling() {
        String text = chat_et.getText().toString();
        String time = Util.getTime();
        ChatText chatText = new ChatText(ChatText.USER, text, time);

        chat_list.add(chatText);//add user's chat text to chat list
        adapter.notifyDataSetChanged();
        chat_rv.scrollToPosition(chat_list.size() - 1);

        MyTask task = new MyTask(){
            @Override
            protected void onPostExecute(String s) {
                parse_text_from_server(s);
            }
        };
        task.execute(Const.turing_url, Const.new_key, text);

    }

    private void send_to_local_server(){
        String text = chat_et.getText().toString();
        String time = Util.getTime();
        ChatText chatText = new ChatText(ChatText.USER, text, time);

        chat_list.add(chatText);//add user's chat text to chat list
        adapter.notifyDataSetChanged();
        chat_rv.scrollToPosition(chat_list.size() - 1);

        MyTask task = new MyTask(){
            @Override
            protected void onPostExecute(String s) {
                parse_text_from_server(s);
            }
        };
        task.execute(Const.local_server, Const.new_key, text);
    }

    /**
     * parse json text from server-end to Robot's chat text
     * @param s
     */
    private void parse_text_from_server(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            String code = jsonObject.getString("code");
            String text = jsonObject.getString("text");

            if (code.equals("100000")){
                String time = Util.getTime();
                ChatText chatText = new ChatText(ChatText.ROBOT,text,time);
                chat_list.add(chatText);
                adapter.notifyDataSetChanged();

                chat_rv.scrollToPosition(chat_list.size() - 1);
            }else {
                Log.e("====>","parse text from server occurred error," + text);
            }
        } catch (JSONException e) {
            Log.e("====>", "Parse JSONException occur: " + e.getMessage());
        }
    }


    class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String turing_url = params[0];
            String key = params[1];
            String info = params[2];

            String result = "";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("key", key);
                jsonObject.accumulate("info", info);
            } catch (JSONException e) {
                Log.e("====>", "Post JSONException occur: " + e.getMessage());
            }

            String requestParams = jsonObject.toString();

            URL url = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            BufferedWriter writer = null;

            Log.i("====>","requestParams="+requestParams);
            try {
                url = new URL(turing_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(Const.connect_timeout);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Content-type","application/json");

                connection.connect();
                writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                writer.write(requestParams);
                writer.flush();


                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null){
                    result += line;
                }

                Log.i("====>","requestParams="+requestParams+",result="+result);
            }
            catch (IOException e) {
                Log.e("====>", "IOException occur: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    Log.e("====>", "Writer Close IOException occur: " + e.getMessage());
                }


                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Log.e("====>", "Reader Close IOException occur: " + e.getMessage());
                }

            }


            return result;
        }
    }


}
