package com.gingerbread.asm3.Views.Notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.Models.Notification;
import com.gingerbread.asm3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private TextView tabAll, tabRead;
    private LinearLayout notificationContainer;
    private List<Notification> notificationsList;
    private FirebaseFirestore firestore;
    private String relationshipId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        tabAll = findViewById(R.id.tabAll);
        tabRead = findViewById(R.id.tabRead);
        notificationContainer = findViewById(R.id.notificationContainer);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getIntent().hasExtra("relationshipId")) {
            relationshipId = getIntent().getStringExtra("relationshipId");
        } else {
            Toast.makeText(this, "Relationship ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        activateTab(tabAll);
        deactivateTab(tabRead);

        notificationsList = new ArrayList<>();

        loadNotifications();

        buttonBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> {
            activateTab(tabAll);
            deactivateTab(tabRead);
            displayNotifications(notificationsList, true);
        });

        tabRead.setOnClickListener(v -> {
            activateTab(tabRead);
            deactivateTab(tabAll);
            displayNotifications(filterReadNotifications(), false);
        });
    }

    private void loadNotifications() {
        if (relationshipId == null) {
            Toast.makeText(this, "Relationship ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("Notifications").whereEqualTo("relationshipId", relationshipId).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            notificationsList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Notification notification = document.toObject(Notification.class);
                notification.setNotificationId(document.getId());
                List<String> readBy = (List<String>) document.get("readBy");
                if (readBy == null) {
                    readBy = new ArrayList<>();
                }
                notification.setReadBy(readBy);
                notificationsList.add(notification);
            }

            if (notificationsList.isEmpty()) {
                Log.d("Notifications", "No notifications found for relationshipId: " + relationshipId);
                Toast.makeText(this, "No notifications available.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Notifications", "Notifications loaded: " + notificationsList.size());
                displayNotifications(notificationsList, true);
            }
        }).addOnFailureListener(e -> {
            Log.e("Notifications", "Failed to load notifications: " + e.getMessage());
            Toast.makeText(this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
        });
    }

    private List<Notification> filterReadNotifications() {
        List<Notification> filteredList = new ArrayList<>();
        for (Notification notification : notificationsList) {
            if (notification.getReadBy().contains(currentUser.getUid())) {
                filteredList.add(notification);
            }
        }
        return filteredList;
    }

    private void displayNotifications(List<Notification> notifications, boolean grayForRead) {
        notificationContainer.removeAllViews();
        for (Notification notification : notifications) {
            View notificationView = createNotificationView(notification, grayForRead);
            notificationContainer.addView(notificationView);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) notificationView.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            notificationView.setLayoutParams(params);
        }
    }

    private View createNotificationView(Notification notification, boolean grayForRead) {
        View view = getLayoutInflater().inflate(R.layout.item_notification, null);

        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewDescription = view.findViewById(R.id.textViewDescription);

        String title;

        if ("memory".equals(notification.getType())) {
            title = "Check the memory view.";
        } else if ("event".equals(notification.getType())) {
            title = "New event added, check your calendar.";
        } else {
            title = notification.getDescription();
        }

        String formattedTimestamp = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(notification.getTimestamp().toDate());

        textViewTitle.setText(title);
        textViewDescription.setText(formattedTimestamp);

        if (notification.getReadBy().contains(currentUser.getUid()) && grayForRead) {
            textViewTitle.setTextColor(getResources().getColor(R.color.text_gray));
            textViewDescription.setTextColor(getResources().getColor(R.color.text_gray));
        } else {
            textViewTitle.setTextColor(getResources().getColor(R.color.text_dark));
            textViewDescription.setTextColor(getResources().getColor(R.color.text_dark));
        }

        view.setOnClickListener(v -> {
            if (!notification.getReadBy().contains(currentUser.getUid())) {
                markAsRead(notification);
            }
        });

        return view;
    }

    private void markAsRead(Notification notification) {
        String notificationId = notification.getNotificationId();
        if (notificationId == null || notificationId.isEmpty()) {
            Log.e("Notifications", "Notification ID is missing.");
            return;
        }

        firestore.collection("Notifications").document(notificationId)
                .update("readBy", FieldValue.arrayUnion(currentUser.getUid()))
                .addOnSuccessListener(aVoid -> {
                    notification.getReadBy().add(currentUser.getUid());
                    updateIsReadStatus(notification);
                    Log.d("Notifications", "Notification marked as read: " + notificationId);
                })
                .addOnFailureListener(e -> Log.e("Notifications", "Failed to mark as read: " + e.getMessage()));
    }

    private void markAllAsRead() {
        for (Notification notification : notificationsList) {
            if (!notification.getReadBy().contains(currentUser.getUid())) {
                markAsRead(notification);
            }
        }
    }


    private void updateIsReadStatus(Notification notification) {
        firestore.collection("Notifications").document(notification.getNotificationId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> readBy = (List<String>) documentSnapshot.get("readBy");
                List<String> expectedUsers = (List<String>) documentSnapshot.get("expectedUsers");

                if (expectedUsers != null && readBy != null && readBy.containsAll(expectedUsers)) {
                    firestore.collection("Notifications").document(notification.getNotificationId()).update("isRead", true).addOnSuccessListener(aVoid -> Log.d("Notifications", "Notification marked as fully read")).addOnFailureListener(e -> Log.e("Notifications", "Failed to update isRead: " + e.getMessage()));
                }
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Failed to fetch notification for isRead check: " + e.getMessage()));
    }

    private void activateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_active_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }

    private void deactivateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_inactive_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        markAllAsRead();
    }

}
