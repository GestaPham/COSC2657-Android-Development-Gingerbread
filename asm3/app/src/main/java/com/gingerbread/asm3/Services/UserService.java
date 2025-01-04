package com.gingerbread.asm3.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    public void findUserByEmail(String email, UserCallback callback) {
        usersCollection.whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> userData = new HashMap<>(document.getData());
                    userData.put("userId", document.getId());
                    callback.onSuccess(userData);
                    return;
                }
                callback.onFailure("User not found with the provided email.");
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to find user by email.");
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

    public void getPartnerByUserId(String userId, UserCallback callback) {
        usersCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String shareToken = task.getResult().getString("shareToken");
                if (shareToken == null || shareToken.isEmpty()) {
                    callback.onSuccess(null);
                    return;
                }

                usersCollection.whereEqualTo("shareToken", shareToken).get().addOnCompleteListener(queryTask -> {
                    if (queryTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : queryTask.getResult()) {
                            if (!document.getId().equals(userId)) {
                                Map<String, Object> partnerData = new HashMap<>(document.getData());
                                partnerData.put("userId", document.getId());
                                callback.onSuccess(partnerData);
                                return;
                            }
                        }
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(queryTask.getException() != null
                                ? queryTask.getException().getMessage()
                                : "Failed to fetch partner");
                    }
                });
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to fetch user data");
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
