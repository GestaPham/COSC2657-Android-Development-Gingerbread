package com.gingerbread.asm3.Services;

import com.gingerbread.asm3.Models.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;

    public MessageService() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getMessages(String conversationId, MessageListCallback callback, ErrorCallback errorCallback) {
        CollectionReference chatRef = firestore.collection("chats").document(conversationId).collection("messages");
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Message> messages = new ArrayList<>(task.getResult().toObjects(Message.class));
                callback.onSuccess(messages);
            } else {
                errorCallback.onError(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to fetch messages");
            }
        });
    }

    public void sendMessage(String conversationId, Message message, SuccessCallback successCallback, ErrorCallback errorCallback) {
        firestore.collection("chats").document(conversationId).collection("messages").add(message)
                .addOnSuccessListener(documentReference -> successCallback.onSuccess())
                .addOnFailureListener(e -> errorCallback.onError(e.getMessage()));
    }

    public void listenForMessages(String conversationId, MessageListCallbackWithErrorHandling callback, ErrorCallback errorCallback) {
        CollectionReference chatRef = firestore.collection("chats").document(conversationId).collection("messages");
        listenerRegistration = chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        errorCallback.onError(e.getMessage());
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<Message> messages = new ArrayList<>(snapshot.toObjects(Message.class));
                        callback.onSuccess(messages);
                    }
                });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    public interface MessageListCallback {
        void onSuccess(List<Message> messages);
    }

    public interface MessageListCallbackWithErrorHandling {
        void onSuccess(List<Message> messages);

        void onError(String errorMessage);
    }

    public interface SuccessCallback {
        void onSuccess();
    }

    public interface ErrorCallback {
        void onError(String errorMessage);
    }
}
