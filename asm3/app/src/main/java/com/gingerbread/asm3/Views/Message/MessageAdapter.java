package com.gingerbread.asm3.Views.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Models.Message;
import com.gingerbread.asm3.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
    private final List<Message> messages;
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView messageProfileImage;
        private TextView messageTextRight,messageTextLeft;
        private RelativeLayout messageItemLeft,message_item_right;
        //showing left text message

        public ViewHolder(View view) {

            super(view);
            messageProfileImage = view.findViewById(R.id.chatProfileImgLeft);
            messageTextLeft = view.findViewById(R.id.messageTextLeft);
            // Define click listener for the ViewHolder's View

        }


    }

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item_left,parent,false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        //RelativeLayout.LayoutParams params = holder.message_item_left.generateLayoutParams();
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
