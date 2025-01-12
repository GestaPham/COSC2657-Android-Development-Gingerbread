package com.gingerbread.asm3.Views.Calendar;

import static android.app.Activity.RESULT_OK;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.Models.Notification;
import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddEventBottomSheetDialog extends BottomSheetDialogFragment {

    private AddEventListener listener;
    private EditText eventNameInput, eventDateInput, eventDescriptionInput;
    private String relationshipId;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_event_modal, container, false);

        firestore = FirebaseFirestore.getInstance();

        eventNameInput = v.findViewById(R.id.eventNameInput);
        eventDateInput = v.findViewById(R.id.eventDateInput);
        eventDescriptionInput = v.findViewById(R.id.eventDescriptionInput);

        Button addEventButton = v.findViewById(R.id.buttonAddEventModal);

        if (getArguments() != null) {
            if (getArguments().containsKey("selectedDate")) {
                eventDateInput.setText(getArguments().getString("selectedDate"));
            }
            if (getArguments().containsKey("relationshipId")) {
                relationshipId = getArguments().getString("relationshipId");
            }
        }

        addEventButton.setOnClickListener(view -> {
            String eventName = eventNameInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();
            String eventDescription = eventDescriptionInput.getText().toString().trim();

            if (eventName.isEmpty() || eventDate.isEmpty() || TextUtils.isEmpty(relationshipId)) {
                Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            listener.onEventAdded(eventName, eventDate, eventDescription, relationshipId);

            sendNotification(eventName, eventDescription, relationshipId);

            if (getActivity() != null) {
                getActivity().setResult(RESULT_OK);
            }

            dismiss();
        });

        return v;
    }

    public interface AddEventListener {
        void onEventAdded(String name, String date, String description, String relationshipId);
    }

    public void setAddEventListener(AddEventListener listener) {
        this.listener = listener;
    }

    private void sendNotification(String title, String description, String relationshipId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("description", description);
        notificationData.put("relationshipId", relationshipId);
        notificationData.put("isRead", false);
        notificationData.put("readBy", new ArrayList<>());
        notificationData.put("timestamp", com.google.firebase.Timestamp.now());
        notificationData.put("type", "event");

        firestore.collection("Notifications").add(notificationData).addOnSuccessListener(documentReference -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Event notification sent!", Toast.LENGTH_SHORT).show();
            }
            Log.d("Notification", "Notification sent: " + documentReference.getId());
        }).addOnFailureListener(e -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Failed to send event notification.", Toast.LENGTH_SHORT).show();
            }
            Log.e("Notification", "Failed to send notification: " + e.getMessage());
        });
    }
}
