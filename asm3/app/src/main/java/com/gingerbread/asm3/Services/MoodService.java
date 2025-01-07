package com.gingerbread.asm3.Services;

import android.util.Log;

import com.gingerbread.asm3.Models.MoodLog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class MoodService {

    private final CollectionReference moodLogsCollection;

    public MoodService() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        moodLogsCollection = firestore.collection("mood_logs");
    }

    public void addMoodLog(MoodLog moodLog, MoodLogCallback callback) {
        String logId = moodLogsCollection.document().getId();
        moodLog.setLogId(logId);

        moodLogsCollection.document(logId).set(moodLog).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(moodLog);
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to add mood log");
            }
        });
    }

    public void getMoodLogsByUserId(String userId, MoodLogListCallback callback) {
        moodLogsCollection.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                List<MoodLog> moodLogs = task.getResult().toObjects(MoodLog.class);
                callback.onSuccess(moodLogs);
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to fetch mood logs");
            }
        });
    }

    public void getLatestMoodLog(String userId, MoodLogCallback callback) {
        moodLogsCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        MoodLog latestMoodLog = queryDocumentSnapshots.toObjects(MoodLog.class).get(0);
                        callback.onSuccess(latestMoodLog);
                    } else {

                        callback.onFailure("No mood logs found for the user.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("moodlog", e.getMessage());
                    callback.onFailure("Error fetching mood log: " + e.getMessage());
                });
    }

    public void updateMoodLog(String logId, MoodLog updatedMoodLog, UpdateCallback callback) {
        moodLogsCollection.document(logId).set(updatedMoodLog).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to update mood log");
            }
        });
    }

    public void deleteMoodLog(String logId, DeleteCallback callback) {
        moodLogsCollection.document(logId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to delete mood log");
            }
        });
    }

    public interface MoodLogCallback {
        void onSuccess(MoodLog moodLog);

        void onFailure(String errorMessage);
    }

    public interface MoodLogListCallback {
        void onSuccess(List<MoodLog> moodLogs);

        void onFailure(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public interface DeleteCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }
}
