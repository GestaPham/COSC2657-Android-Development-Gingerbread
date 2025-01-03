package com.gingerbread.asm3.Views.Message;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageProfileImage;
        private TextView messageTextRight,messageTextLeft;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
        }


    }

    public MessageAdapter() {
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }



    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
