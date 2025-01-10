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
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddEventBottomSheetDialog extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText eventNameInput, noteInputEvent;
    private Spinner spinnerReminder;
    private Button buttonAddEvent, buttonAddDate;
    private AddEventListener listener;
    private String date;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_event_modal, container, false);

        eventNameInput = view.findViewById(R.id.eventNameInput);
        noteInputEvent = view.findViewById(R.id.noteInputEvent);
        spinnerReminder = view.findViewById(R.id.spinnerReminder);
        buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        buttonAddDate = view.findViewById(R.id.buttonAddDate);

        buttonAddDate.setOnClickListener(v -> {
            DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(selectedDate -> {
                this.date = selectedDate;
            });
            datePickerFragment.show(getChildFragmentManager(), "datePicker");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.reminders_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);
        spinnerReminder.setOnItemSelectedListener(this);

        buttonAddEvent.setOnClickListener(v -> {
            String name = eventNameInput.getText().toString();
            String note = noteInputEvent.getText().toString();
            String reminderBefore = spinnerReminder.getSelectedItem().toString();
            addEvent(name, note, date, reminderBefore);

            if (listener != null) {
                listener.onEventAdded(date, name, note);
            }
            dismiss();
        });

        return view;
    }


    public void onDateSelected(String selectedDate) {
        this.date = selectedDate;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddEventListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface AddEventListener {
        void onEventAdded(String date, String name, String note);
    }

    private void addEvent(String title, String description, String date, String reminderBefore) {
        ContentResolver contentResolver = this.requireContext().getContentResolver();
        ContentValues eventValues = new ContentValues();
        Log.d("date input",date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFormatted = null;
        try{
            dateFormatted = dateFormat.parse(date);
        } catch (ParseException e){
            Log.e("dateFormatProblem",e.toString());
        }
        if (dateFormatted != null) {
            eventValues.put(CalendarContract.Events.CALENDAR_ID, 1);
            eventValues.put(CalendarContract.Events.TITLE, title);
            eventValues.put(CalendarContract.Events.DESCRIPTION, description);
            eventValues.put(CalendarContract.Events.DTSTART, dateFormatted.getTime());
            eventValues.put(CalendarContract.Events.DTEND, dateFormatted.getTime());
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            eventValues.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        }else {
                Log.e("dateFormatProblem", "Failed to parse date: " + date);
            }
        Uri eventUri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
        if (eventUri != null) {
            long eventId = ContentUris.parseId(eventUri);
            String[] remindersArray = getResources().getStringArray(R.array.reminders_array);
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            if (reminderBefore.equals(remindersArray[0])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES, 0);
            } else if (reminderBefore.equals(remindersArray[1])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES, 10);
            } else if (reminderBefore.equals(remindersArray[2])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES, 60);
            } else if (reminderBefore.equals(remindersArray[3])) {
                reminderValues.put(CalendarContract.Reminders.MINUTES, 1440);
            } else {
                reminderValues.put(CalendarContract.Reminders.MINUTES, -1);
            }
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

            Toast.makeText(this.requireContext(), "Event and reminder added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.requireContext(), "Failed to add event.", Toast.LENGTH_SHORT).show();
        }
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public static DatePickerFragment newInstance(OnDateSelectedListener listener) {
            DatePickerFragment fragment = new DatePickerFragment();
            fragment.listener = listener;
            return fragment;
        }
        private OnDateSelectedListener listener;
        interface OnDateSelectedListener {
            void onDateSelected(String date);
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String date = String.format(Locale.getDefault(),"%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (listener != null) {
                listener.onDateSelected(date);
            }
        }
    }

}


