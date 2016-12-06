package com.xkc.chatrobot.model;

import com.xkc.chatrobot.Helper.Const;

/**
 * Created by xkc on 12/5/16.
 */

public class ChatCount {
    private int value;
    private MaxValueListener listener;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        if (this.value == Const.MAX_CHAT_COUNT){

            if (listener != null){
                listener.onMaxCount();
            }
        }
    }

    public void setMaxValueListener(MaxValueListener listener){
        this.listener = listener;
    }

    public interface MaxValueListener {
        void onMaxCount();
    }


}
