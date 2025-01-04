package com.gingerbread.asm3.Services;

import android.app.Activity;

import com.gingerbread.asm3.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

public class AuthenticationService {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthenticationService() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String name, int age, String gender,
                             String nationality, String religion, String location, boolean isPremium,
                             String profilePictureUrl, Activity activity, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
            if (!task.isSuccessful()) {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                callback.onFailure("Registration failed: " + errorMessage);
                return;
            }

            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                callback.onFailure("Registration failed: User not found.");
                return;
            }

            String userId = firebaseUser.getUid();
            String shareToken = UUID.randomUUID().toString();

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                if (!tokenTask.isSuccessful()) {
                    String errorMessage = tokenTask.getException() != null ? tokenTask.getException().getMessage() : "Unknown error";
                    callback.onFailure("Failed to fetch FCM token: " + errorMessage);
                    return;
                }

                String fcmToken = tokenTask.getResult();
                if (fcmToken == null || fcmToken.isEmpty()) {
                    callback.onFailure("Failed to fetch FCM token: Token is null or empty.");
                    return;
                }

                User user = new User(
                        userId,
                        name,
                        email,
                        profilePictureUrl,
                        age,
                        gender,
                        nationality,
                        religion,
                        location,
                        isPremium,
                        fcmToken,
                        shareToken,
                        ""
                );

                firestore.collection("users").document(userId).set(user).addOnCompleteListener(firestoreTask -> {
                    if (firestoreTask.isSuccessful()) {
                        callback.onSuccess(firebaseUser);
                    } else {
                        String errorMessage = firestoreTask.getException() != null ? firestoreTask.getException().getMessage() : "Unknown error";
                        callback.onFailure("Failed to save user data: " + errorMessage);
                    }
                });
            });
        });
    }

    public void loginUser(String email, String password, Activity activity, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
            if (!task.isSuccessful()) {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                callback.onFailure("Login failed: " + errorMessage);
                return;
            }

            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                callback.onFailure("Login failed: User not found.");
                return;
            }

            String userId = firebaseUser.getUid();

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                if (!tokenTask.isSuccessful()) {
                    String errorMessage = tokenTask.getException() != null ? tokenTask.getException().getMessage() : "Unknown error";
                    callback.onFailure("Failed to fetch FCM token: " + errorMessage);
                    return;
                }

                String fcmToken = tokenTask.getResult();
                if (fcmToken == null || fcmToken.isEmpty()) {
                    callback.onFailure("Failed to fetch FCM token: Token is null or empty.");
                    return;
                }

                firestore.collection("users").document(userId).update("fcmToken", fcmToken).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess(firebaseUser);
                    } else {
                        String errorMessage = updateTask.getException() != null ? updateTask.getException().getMessage() : "Unknown error";
                        callback.onFailure("Failed to update FCM token: " + errorMessage);
                    }
                });
            });
        });
    }

    public void isFcmTokenValid(Activity activity, AuthCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("No user is currently logged in.");
            return;
        }

        String userId = currentUser.getUid();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(activity, tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                String errorMessage = tokenTask.getException() != null ? tokenTask.getException().getMessage() : "Unknown error";
                callback.onFailure("Failed to fetch current FCM token: " + errorMessage);
                return;
            }

            String currentFcmToken = tokenTask.getResult();
            if (currentFcmToken == null || currentFcmToken.isEmpty()) {
                callback.onFailure("Current FCM token is null or empty.");
                return;
            }

            firestore.collection("users").document(userId).get().addOnCompleteListener(firestoreTask -> {
                if (firestoreTask.isSuccessful() && firestoreTask.getResult() != null && firestoreTask.getResult().exists()) {
                    String storedFcmToken = firestoreTask.getResult().getString("fcmToken");
                    if (currentFcmToken.equals(storedFcmToken)) {
                        callback.onSuccess(currentUser);
                    } else {
                        callback.onFailure("FCM token is invalid or outdated.");
                    }
                } else {
                    String errorMessage = firestoreTask.getException() != null ? firestoreTask.getException().getMessage() : "User data not found.";
                    callback.onFailure("Failed to fetch stored FCM token: " + errorMessage);
                }
            });
        });
    }

    public void logoutUser() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);

        void onFailure(String errorMessage);
    }
}
