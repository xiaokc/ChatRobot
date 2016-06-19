package com.xkc.chatrobot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xkc.chatrobot.R;
import com.xkc.chatrobot.model.ChatText;

import java.util.List;

/**
 * Created by xkc on 6/15/16.
 */
public class ChatTextAdapter extends RecyclerView.Adapter<ChatTextAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ChatText> mData;

    public ChatTextAdapter(Context context, List<ChatText> data) {
        this.mContext = context;
        this.mData = data;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        View itemView = null;

        switch (viewType) {
            case ChatText.USER:
                itemView = mInflater.inflate(R.layout.right_layout, null);
                holder = new RightViewHolder(itemView);
                break;
            case ChatText.ROBOT:
                itemView = mInflater.inflate(R.layout.left_layout, null);
                holder = new LeftViewHolder(itemView);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatText chatText = mData.get(position);

        String content = chatText.getContent();
        String time = chatText.getTime();

        if (holder instanceof LeftViewHolder){
            ((LeftViewHolder) holder).left_tv.setText(content);
        }else if (holder instanceof RightViewHolder){
            ((RightViewHolder) holder).right_tv.setText(content);
        }

        holder.time_tv.setText(time);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getFlag();
    }

    class LeftViewHolder extends ChatTextAdapter.ViewHolder {
        TextView left_tv;

        public LeftViewHolder(View itemView) {
            super(itemView);
            left_tv = (TextView) itemView.findViewById(R.id.left_tv);
        }
    }

    class RightViewHolder extends ChatTextAdapter.ViewHolder {
        TextView right_tv;

        public RightViewHolder(View itemView) {
            super(itemView);
            right_tv = (TextView) itemView.findViewById(R.id.right_tv);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView time_tv;

        public ViewHolder(View itemView) {
            super(itemView);
            time_tv = (TextView) itemView.findViewById(R.id.time);
        }
    }
}
