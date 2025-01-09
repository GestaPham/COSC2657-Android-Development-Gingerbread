package com.gingerbread.asm3.Services;

import android.content.Context;

import android.widget.Toast;

import com.gingerbread.asm3.Models.Event;
import com.gingerbread.asm3.Models.Memory;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CalendarService {
    private final CollectionReference memoriesCollection;
    private final CollectionReference eventsCollection;
    private FirebaseFirestore firestore;


    public CalendarService() {
        firestore = FirebaseFirestore.getInstance();
        this.memoriesCollection = firestore.collection("memories");
        this.eventsCollection = firestore.collection("events");
    }

    public void getAllMemories(String userId, UsersMemoriesCallback callback) {
        firestore.collection("memories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Memory> memoriesList = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                memoriesList.add(document.toObject(Memory.class));
            }
            callback.onMemoriesFetched(memoriesList);
        }).addOnFailureListener(e -> {
            callback.onError(e);
        });
    }

    public void updateMemory(String memoryId, String updateField, String updateValue) {
        firestore.collection("memories").document(memoryId).update(updateField, updateValue).addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
        });
    }

    public void deleteMemory(String memoryId) {
        firestore.collection("memories").document(memoryId).delete().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
        });
    }

    public void addMemory(Memory memory, Context context, CalendarService.MemoryAddedCallback callback) {
        Toast toast = new Toast(context);
        DocumentReference newMemoryRef = firestore.collection("memories").document();
        memory.setMemoryId(newMemoryRef.getId());
        newMemoryRef.set(memory)
                .addOnSuccessListener(aVoid -> {
                    toast.setText("New memory added");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                    if (callback != null) {
                        callback.onMemoryAdded();
                    }
                })
                .addOnFailureListener(e -> {
                    toast.setText("Failed to add new memory");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                });
    }

    public interface MemoryAddedCallback {
        void onMemoryAdded();
    }


    public void getEventsByDate(String date, String relationshipId, EventsCallback callback) {
        eventsCollection.whereEqualTo("eventDate", date).whereEqualTo("relationshipId", relationshipId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Event event = document.toObject(Event.class);
                event.setEventId(document.getId());
                events.add(event);
            }
            callback.onSuccess(events);
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface UsersMemoriesCallback {
        void onError(Exception e);

        void onMemoriesFetched(List<Memory> memories);
    }

    public interface EventsCallback {
        void onSuccess(List<Event> events);

        void onFailure(String errorMessage);
    }


    public void getAllEvents(String userId, EventsCallback callback) {
        eventsCollection.whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> eventList = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Event event = document.toObject(Event.class);
                event.setEventId(document.getId());
                eventList.add(event);
            }
            callback.onSuccess(eventList);
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addEvent(Event event, Context context) {
        eventsCollection.add(event).addOnSuccessListener(documentReference -> {
            Toast.makeText(context, "Event added successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    public void updateEvent(String eventId, String field, Object value, Context context) {
        eventsCollection.document(eventId).update(field, value).addOnSuccessListener(aVoid -> Toast.makeText(context, "Event updated!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(context, "Failed to update event.", Toast.LENGTH_SHORT).show());
    }

    public void deleteEvent(String eventId, Context context) {
        eventsCollection.document(eventId).delete().addOnSuccessListener(aVoid -> Toast.makeText(context, "Event deleted!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete event.", Toast.LENGTH_SHORT).show());
    }


}

