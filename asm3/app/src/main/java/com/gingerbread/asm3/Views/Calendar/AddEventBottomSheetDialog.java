package com.gingerbread.asm3.Views.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;
import java.util.TimeZone;

public class AddEventBottomSheetDialog extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener, DatePickerFragment.OnDateSelectedListener {
    private EditText eventNameInput,noteInputEvent;
    private Spinner spinnerReminder;
    private Button buttonAddEvent,buttonAddDate;
    private AddEventListener listener;
    private String date;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_event_modal,container,false);

        eventNameInput = v.findViewById(R.id.eventNameInput);
        noteInputEvent = v.findViewById(R.id.noteInputEvent);
        spinnerReminder = v.findViewById(R.id.spinnerReminder);
        buttonAddEvent = v.findViewById(R.id.buttonAddEvent);
        buttonAddDate = v.findViewById(R.id.buttonAddDate);

        buttonAddDate.setOnClickListener(view->{
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.show(getChildFragmentManager(), "datePicker");
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.requireContext(),
                R.array.reminders_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);
        spinnerReminder.setOnItemSelectedListener(this);
        buttonAddEvent.setOnClickListener(view->{
            String name = eventNameInput.getText().toString();
            String note = noteInputEvent.getText().toString();
            String reminderBefore = spinnerReminder.getSelectedItem().toString();
            addEvent(name,note,date,reminderBefore);
            if (listener != null) {
                listener.onEventAdded(date, name, note);
            }
            dismiss();
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onDateSelected(String selectedDate) {
        this.date = selectedDate;
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
    private void addEvent(String title,String description ,String date,String reminderBefore) {
        ContentResolver contentResolver = this.requireContext().getContentResolver();
        ContentValues eventValues = new ContentValues();
        eventValues.put(CalendarContract.Events.CALENDAR_ID, 1);
        eventValues.put(CalendarContract.Events.TITLE, title);
        eventValues.put(CalendarContract.Events.DESCRIPTION, description);
        eventValues.put(CalendarContract.Events.DTSTART,date);
        eventValues.put(CalendarContract.Events.DTEND,date);
        //eventValues.put(CalendarContract.Events.EVENT_LOCATION, location)
        eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        eventValues.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        Uri eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
        if (eventUri != null) {
            long eventId = ContentUris.parseId(eventUri);
            String[] remindersArray = getResources().getStringArray(R.array.reminders_array);
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            if(reminderBefore.equals(remindersArray[0])){
                reminderValues.put(CalendarContract.Reminders.MINUTES,0);
            } else if (reminderBefore.equals(remindersArray[1])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES,10);
            } else if (reminderBefore.equals(remindersArray[2])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES,60);
            } else if (reminderBefore.equals(remindersArray[3])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES,1440);
            }else {
                reminderValues.put(CalendarContract.Reminders.MINUTES,-1);
            }
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Toast.makeText(this.requireContext(), "Event and reminder added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.requireContext(), "Failed to add event.", Toast.LENGTH_SHORT).show();
        }
    }

}
class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
    private OnDateSelectedListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnDateSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "no listener found");
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
        String date = String.format(String.valueOf(Locale.getDefault()), year, month + 1, dayOfMonth);
        if (listener != null) {
            listener.onDateSelected(date);
        }
    }

}
