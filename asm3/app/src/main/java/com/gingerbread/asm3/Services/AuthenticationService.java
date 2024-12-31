package com.gingerbread.asm3.Services;

import android.app.Activity;

import com.gingerbread.asm3.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;
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
                callback.onFailure("Registration failed: " + Objects.requireNonNull(task.getException()).getMessage());
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
                    callback.onFailure("Failed to fetch FCM token: " + Objects.requireNonNull(tokenTask.getException()).getMessage());
                    return;
                }

                String fcmToken = tokenTask.getResult();
                User user = new User(userId, name, email, profilePictureUrl, age, gender, nationality, religion, location, isPremium, fcmToken, shareToken);

                firestore.collection("users").document(userId).set(user).addOnCompleteListener(firestoreTask -> {
                    if (firestoreTask.isSuccessful()) {
                        callback.onSuccess(firebaseUser);
                    } else {
                        callback.onFailure("Failed to save user data: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                    }
                });
            });
        });
    }

    public void loginUser(String email, String password, Activity activity, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
            if (!task.isSuccessful()) {
                callback.onFailure("Login failed: " + Objects.requireNonNull(task.getException()).getMessage());
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
                    callback.onFailure("Failed to fetch FCM token: " + Objects.requireNonNull(tokenTask.getException()).getMessage());
                    return;
                }

                String fcmToken = tokenTask.getResult();
                firestore.collection("users").document(userId).update("fcmToken", fcmToken).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess(firebaseUser);
                    } else {
                        callback.onFailure("Failed to update FCM token: " + Objects.requireNonNull(updateTask.getException()).getMessage());
                    }
                });
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
