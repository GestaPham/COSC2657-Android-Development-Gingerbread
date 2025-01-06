package com.gingerbread.asm3.Views.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import java.util.GregorianCalendar;

public class CalendarActivity extends BaseActivity {

    private CalendarView calendarView;
    private Button addEventButton, addMemoryButton;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.activity_content));

        calendarView = findViewById(R.id.calendarView);
        addEventButton = findViewById(R.id.addEventButton);
        addMemoryButton = findViewById(R.id.addMemoryButton);

        selectedDate = calendarView.getDate();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
                Toast.makeText(CalendarActivity.this, "Selected Date: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });

        addEventButton.setOnClickListener(v -> addEvent());
        addMemoryButton.setOnClickListener(v -> addMemory());
    }

    private void addEvent() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, selectedDate);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, selectedDate + 60 * 60 * 1000);
        intent.putExtra(CalendarContract.Events.TITLE, "New Event");
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Event Description");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Event Location");
        intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No events for this date.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMemory() {
        
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_calendar;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_calendar;
    }
}