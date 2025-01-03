package com.gingerbread.asm3.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final CollectionReference usersCollection;

    public UserService() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        usersCollection = firestore.collection("users");
    }

    public void getUser(String userId, UserCallback callback) {
        usersCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                callback.onSuccess(task.getResult().getData());
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "User not found");
            }
        });
    }

    public void updateUser(String userId, Map<String, Object> updates, UpdateCallback callback) {
        usersCollection.document(userId).update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to update user data");
            }
        });
    }

    public void deleteUser(String userId, DeleteCallback callback) {
        usersCollection.document(userId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to delete user");
            }
        });
    }

    public void findUserByShareToken(String shareToken, UserCallback callback) {
        usersCollection.whereEqualTo("shareToken", shareToken).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        callback.onSuccess(task.getResult().getDocuments().get(0).getData());
                    } else {
                        callback.onFailure("Invalid shareToken or user not found");
                    }
                });
    }

    public void linkUsers(String userId, String partnerUserId, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("linkedUserId", partnerUserId);

        usersCollection.document(userId).update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed to link users");
                    }
                });
    }

    public String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public interface UserCallback {
        void onSuccess(Map<String, Object> userData);

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
