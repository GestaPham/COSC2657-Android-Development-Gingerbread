package com.gingerbread.asm3.Services;

import android.util.Log;

import com.gingerbread.asm3.Models.Relationship;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class RelationshipService {

    private final CollectionReference relationshipsCollection;

    public RelationshipService() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        relationshipsCollection = firestore.collection("relationships");
    }

    public void addRelationship(Relationship relationship, RelationshipCallback callback) {
        String relationshipId = relationshipsCollection.document().getId();
        relationship.setRelationshipId(relationshipId);

        relationshipsCollection.document(relationshipId).set(relationship).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(relationship);
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to add relationship");
            }
        });
    }

    public void getRelationshipByShareToken(String shareToken, RelationshipCallback callback) {
        relationshipsCollection.whereEqualTo("shareToken", shareToken).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (!task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Relationship relationship = document.toObject(Relationship.class);
                        callback.onSuccess(relationship);
                        return;
                    }
                }
                callback.onSuccess(null);
            } else {
                String errorMessage = task.getException() != null
                        ? "Error fetching relationship for shareToken: " + shareToken + ". Exception: " + task.getException().getMessage()
                        : "Failed to fetch relationship for shareToken: " + shareToken;
                Log.e("RelationshipService", errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }

    public void updateRelationship(String relationshipId, Map<String, Object> updates, UpdateCallback callback) {
        relationshipsCollection.document(relationshipId).update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to update relationship");
            }
        });
    }

    public void deleteRelationship(String relationshipId, DeleteCallback callback) {
        relationshipsCollection.document(relationshipId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to delete relationship");
            }
        });
    }

    public interface RelationshipCallback {
        void onSuccess(Relationship relationship);

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
