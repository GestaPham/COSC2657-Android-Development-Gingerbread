package com.gingerbread.asm3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Models.Message;
import com.gingerbread.asm3.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
    private final List<Message> messages;
    private final String currentUserId;
    private static final int VIEW_TYPE_SENDING = 0;
    private static final int VIEW_TYPE_RECEIVING = 1;
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView messageProfileImage;
        private TextView messageText;
        private CardView messageItemSending,messageItemReceiving;
        //private RelativeLayout messageItemLeft,message_item_right;
        //showing left text message

        public ViewHolder(View view) {
            super(view);
            messageProfileImage = view.findViewById(R.id.messageProfileImage);
            messageText = view.findViewById(R.id.messageText);
            // Define click listener for the ViewHolder's View
        }
    }

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(message.getSenderId().equals(currentUserId)){
            return VIEW_TYPE_RECEIVING;
        }else{
            return VIEW_TYPE_SENDING;
        }

    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_RECEIVING) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_receiving, parent, false);
        } else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_sending, parent, false);
        }
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getMessageContent());
        //RelativeLayout.LayoutParams params = holder.message_item_left.generateLayoutParams();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
