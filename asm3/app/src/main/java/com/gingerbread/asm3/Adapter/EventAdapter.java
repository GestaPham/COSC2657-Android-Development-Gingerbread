package com.gingerbread.asm3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Models.Event;
import com.gingerbread.asm3.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> eventList;
    private OnEventClickListener listener;

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventTitle.setText(event.getEventName());
        holder.eventDescription.setText(event.getEventDescription());
        holder.eventDate.setText(formatDate(event.getEventDate()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    private String formatDate(String dateString) {
        return dateString;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDescription, eventDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.textEventTitle);
            eventDescription = itemView.findViewById(R.id.textEventDescription);
            eventDate = itemView.findViewById(R.id.textEventDate);
        }
    }
}
