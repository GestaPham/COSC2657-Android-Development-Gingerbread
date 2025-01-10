package com.gingerbread.asm3.Views.Calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.TimeZone;

public class AddEventBottomSheetDialog extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText eventNameInput,eventDateInput,noteInputEvent;
    private Spinner spinnerReminder;
    private Button buttonAdd;
    private AddEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_event_modal,container,false);
        eventDateInput = v.findViewById(R.id.eventDateInput);
        eventNameInput = v.findViewById(R.id.eventNameInput);
        noteInputEvent = v.findViewById(R.id.noteInputEvent);
        spinnerReminder = v.findViewById(R.id.spinnerReminder);
        buttonAdd = v.findViewById(R.id.buttonAddEvent);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.requireContext(),
                R.array.reminders_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);
        spinnerReminder.setOnItemSelectedListener(this);
        buttonAdd.setOnClickListener(view->{
            String date = eventDateInput.getText().toString();
            String name = eventNameInput.getText().toString();
            String note = noteInputEvent.getText().toString();
            addEvent(name,date,note);
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (AddEventListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement AddEventListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface AddEventListener{
        void onEventAdded(String date,String name,String note);
    }
    private void addEvent(String title, long startTime,long endTime,int reminderMinutes) {
        ContentResolver contentResolver = this.requireContext().getContentResolver();
        ContentValues eventValues = new ContentValues();
        eventValues.put(CalendarContract.Events.CALENDAR_ID, 1);
        eventValues.put(CalendarContract.Events.TITLE, title);
        //eventValues.put(CalendarContract.Events.DESCRIPTION, description);
        //eventValues.put(CalendarContract.Events.EVENT_LOCATION, location);
        eventValues.put(CalendarContract.Events.DTSTART, startTime);
        eventValues.put(CalendarContract.Events.DTEND, endTime);
        //eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        eventValues.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        Uri eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
        if (eventUri != null) {
            long eventId = ContentUris.parseId(eventUri);

            // Insert Reminder
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.MINUTES, reminderMinutes); // Reminder in minutes before event
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT); // Alert type
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Toast.makeText(this.requireContext(), "Event and reminder added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.requireContext(), "Failed to add event.", Toast.LENGTH_SHORT).show();
        }
    }
}
