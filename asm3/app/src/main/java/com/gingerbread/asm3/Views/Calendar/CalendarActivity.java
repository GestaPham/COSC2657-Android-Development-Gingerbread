package com.gingerbread.asm3.Views.CalendarActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button addEventButton, addMemoryButton;
    private long selectedDate;
    private HashMap<Long, List<String>> eventsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        addEventButton = findViewById(R.id.addEventButton);
        addMemoryButton = findViewById(R.id.addMemoryButton);

        selectedDate = calendarView.getDate();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = new java.util.GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
            showEventsForSelectedDate(selectedDate);
        });

        addEventButton.setOnClickListener(v -> showAddDialog(false));
        addMemoryButton.setOnClickListener(v -> showAddDialog(true));
    }

    private void showAddDialog(boolean isMemory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText titleInput = dialogView.findViewById(R.id.editTextTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.editTextDescription);
        Spinner moodSpinner = dialogView.findViewById(R.id.spinnerMood);
        Button addButton = dialogView.findViewById(R.id.buttonAdd);

        if (isMemory) {
            moodSpinner.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = builder.create();

        addButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String mood = isMemory ? moodSpinner.getSelectedItem().toString() : null;

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventOrMemory = isMemory 
                ? "Memory: " + title + " (" + mood + ")" 
                : "Event: " + title;

            if (!eventsMap.containsKey(selectedDate)) {
                eventsMap.put(selectedDate, new ArrayList<>());
            }
            eventsMap.get(selectedDate).add(eventOrMemory);

            Toast.makeText(this, eventOrMemory + " added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEventsForSelectedDate(long date) {
        if (eventsMap.containsKey(date)) {
            List<String> events = eventsMap.get(date);
            Toast.makeText(this, "Events: " + String.join("\n", events), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No events for this date.", Toast.LENGTH_SHORT).show();
        }
    }

}
