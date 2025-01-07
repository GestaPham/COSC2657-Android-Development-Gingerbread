package com.gingerbread.asm3.Services;

import com.gingerbread.asm3.Models.Milestone;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MilestoneService {

    private final CollectionReference milestonesCollection;

    public MilestoneService() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        milestonesCollection = firestore.collection("milestones");
    }

    public void addMilestone(Milestone milestone, MilestoneCallback callback) {
        String milestoneId = milestonesCollection.document().getId();
        milestone.setMilestoneId(milestoneId);

        milestonesCollection.document(milestoneId).set(milestone).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(milestone);
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to add milestone");
            }
        });
    }

    public void getMilestonesByShareToken(String shareToken, MilestoneListCallback callback) {
        milestonesCollection.whereEqualTo("shareToken", shareToken).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                List<Milestone> milestones = task.getResult().toObjects(Milestone.class);
                callback.onSuccess(milestones);
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to fetch milestones");
            }
        });
    }

    public void deleteMilestone(String milestoneId, DeleteCallback callback) {
        milestonesCollection.document(milestoneId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to delete milestone");
            }
        });
    }

    public interface MilestoneCallback {
        void onSuccess(Milestone milestone);

        void onFailure(String errorMessage);
    }

    public interface MilestoneListCallback {
        void onSuccess(List<Milestone> milestones);

        void onFailure(String errorMessage);
    }

    public interface DeleteCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }
}
