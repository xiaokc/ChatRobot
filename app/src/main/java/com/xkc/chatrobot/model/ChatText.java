package com.xkc.chatrobot.model;

/**
 * Created by xkc on 6/15/16.
 */
public class ChatText {

    public static final int USER = 1;
    public static final int ROBOT = 2;

    private int flag;
    private String content;
    private String time;

    public ChatText(int flag, String content, String time) {
        this.flag = flag;
        this.content = content;
        this.time = time;
    }


    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
