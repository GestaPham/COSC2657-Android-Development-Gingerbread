package com.gingerbread.asm3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Models.MoodLog;
import com.gingerbread.asm3.R;

import java.util.List;

public class MoodTimelineAdapter extends RecyclerView.Adapter<MoodTimelineAdapter.MoodViewHolder> {
    private final List<MoodLog> moodLogs;

    public MoodTimelineAdapter(List<MoodLog> moodLogs) {
        this.moodLogs = moodLogs;
    }

    private int getMoodIcon(String mood) {
        switch (mood) {
            case "Bad":
                return R.drawable.ic_mood_bad;
            case "Tired":
                return R.drawable.ic_mood_tired;
            case "Okay":
                return R.drawable.ic_mood_okay;
            case "Happy":
                return R.drawable.ic_mood_happy;
            case "Excited":
                return R.drawable.ic_mood_excited;
            default:
                return R.drawable.ic_mood_okay;
        }
    }


    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_timeline, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodLog moodLog = moodLogs.get(position);

        holder.dateTextView.setText(moodLog.getDate());
        holder.moodTextView.setText(moodLog.getMood());
        holder.notesTextView.setText(moodLog.getNotes());

        // Set mood icon
        holder.moodImageView.setImageResource(getMoodIcon(moodLog.getMood()));
    }

    @Override
    public int getItemCount() {
        return moodLogs.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, moodTextView, notesTextView;
        ImageView moodImageView;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            moodTextView = itemView.findViewById(R.id.moodTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            moodImageView = itemView.findViewById(R.id.moodImageView);
        }
    }
}
