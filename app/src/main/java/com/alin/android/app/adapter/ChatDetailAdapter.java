package com.alin.android.app.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.alin.android.app.R;
import com.alin.android.app.model.ChatMessage;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.core.utils.DateUtil;

import java.util.List;

public class ChatDetailAdapter extends BaseCoreAdapter<ChatMessage> {

    private String username;

    public ChatDetailAdapter(String username, List<ChatMessage> mData) {
        super(mData);
        this.username = username;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = mData.get(position);
        ViewHolder holder;
        if (chatMessage.getFrom().equals(username)) {
            holder = ViewHolder.bind(mContext != null?mContext:parent.getContext(), convertView, parent, R.layout.item_chat_send_text, position);
        } else {
            holder = ViewHolder.bind(mContext != null?mContext:parent.getContext(), convertView, parent, R.layout.item_chat_receive_text, position);
        }
        bindView(holder, getItem(position));
        return holder.getItemView();
    }

    @Override
    public void bindView(ViewHolder holder, ChatMessage chatMessage) {
        if (!chatMessage.getFrom().equals(username)) {
            holder.setText(R.id.tv_display_name, chatMessage.getTo());
        }
        holder.setText(R.id.tv_content, chatMessage.getText());
        holder.setText(R.id.tv_sendtime, DateUtil.format(chatMessage.getDate(), DateUtil.DATEFORMATSECOND));
    }
}
