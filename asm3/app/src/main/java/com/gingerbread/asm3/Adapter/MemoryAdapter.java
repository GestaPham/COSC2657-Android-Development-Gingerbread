package com.gingerbread.asm3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.R;

import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder> {

    public interface OnMemoryClickListener {
        void onMemoryClick(Memory memory);
    }

    private List<Memory> memoryList;
    private OnMemoryClickListener listener;

    public MemoryAdapter(List<Memory> memoryList, OnMemoryClickListener listener) {
        this.memoryList = memoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memory_card, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        Memory memory = memoryList.get(position);

        holder.textMemoryTitle.setText(memory.getMemoryName());
        holder.textMemoryNote.setText(memory.getNote());
        holder.textMemoryDate.setText(memory.getDate());
        Glide.with(holder.itemView.getContext()).load(memory.getImageUrl()).into(holder.imageMemory);

        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        holder.itemView.setOnClickListener(v -> listener.onMemoryClick(memory));
    }

    @Override
    public int getItemCount() {
        return memoryList.size();
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMemory;
        TextView textMemoryTitle, textMemoryNote, textMemoryDate;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMemory = itemView.findViewById(R.id.imageMemory);
            textMemoryTitle = itemView.findViewById(R.id.textMemoryTitle);
            textMemoryNote = itemView.findViewById(R.id.textMemoryNote);
            textMemoryDate = itemView.findViewById(R.id.textMemoryDate);
        }
    }
}
