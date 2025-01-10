package com.gingerbread.asm3.Views.Calendar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;



public class AddEventBottomSheetDialog extends BottomSheetDialogFragment {

    private AddEventListener listener;
    private EditText eventNameInput, eventDateInput, eventDescriptionInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_event_modal, container, false);

        eventNameInput = v.findViewById(R.id.eventNameInput);
        eventDateInput = v.findViewById(R.id.eventDateInput);
        eventDescriptionInput = v.findViewById(R.id.eventDescriptionInput);

        Button addEventButton = v.findViewById(R.id.buttonAddEventModal);

        if (getArguments() != null && getArguments().containsKey("selectedDate")) {
            eventDateInput.setText(getArguments().getString("selectedDate"));
        }

        addEventButton.setOnClickListener(view -> {
            String eventName = eventNameInput.getText().toString().trim();
            String eventDate = eventDateInput.getText().toString().trim();
            String eventDescription = eventDescriptionInput.getText().toString().trim();

            if (eventName.isEmpty() || eventDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            listener.onEventAdded(eventName, eventDate, eventDescription);
            dismiss();
        });

        return v;
    }

    public interface AddEventListener {
        void onEventAdded(String name, String date, String description);
    }

    public void setAddEventListener(AddEventListener listener) {
        this.listener = listener;
    }
}

