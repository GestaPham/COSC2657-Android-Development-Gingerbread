package com.gingerbread.asm3.Views.Calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Services.CalendarService;
import com.gingerbread.asm3.Views.Memory.MemoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class CalendarActivity extends BaseActivity implements AddMemoryBottomSheetDialog.AddMemoryListener{

    private CalendarView calendarView;
    private Button addEventButton, addMemoryButton;
    private ImageButton addMemoryButton2,addEventButton2;
    private long selectedDate;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ViewPager2 viewPagerMemories;
    private MemoryAdapter memoryAdapter;
    private HashMap<Long, List<String>> eventsMap = new HashMap<>();
    private HashMap<String, Memory> memoryHashMap = new HashMap<>();
    private List<Memory> userMemories = new ArrayList<>();
    private CalendarService calendarService = new CalendarService();
    private TextView viewAll;
    Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.activity_content));
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        calendarView = findViewById(R.id.calendarView);
        addEventButton = findViewById(R.id.addEventButton);
        //addMemoryButton = findViewById(R.id.addMemoryButton);
        addMemoryButton2 = findViewById(R.id.addMemoryButton2);
        viewAll = findViewById(R.id.viewAllLink);
        selectedDate = calendarView.getDate();

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList);
        recyclerViewEvents.setAdapter(eventAdapter);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
                Toast.makeText(CalendarActivity.this, "Selected Date: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });

        addEventButton.setOnClickListener(v -> addEvent());
        //addMemoryButton.setOnClickListener(v -> addMemory());
        addMemoryButton2.setOnClickListener(v->addMemory());
        viewAll.setOnClickListener(v->{

            fetchUsersMemories(currentUser.getUid());
            String memoriesJson = gson.toJson(userMemories);
            Intent intent = new Intent(CalendarActivity.this, MemoryActivity.class);
            intent.putExtra("memoriesJson",memoriesJson);
            startActivity(intent);

        });
        fetchEventsForDate(selectedDate);
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
        new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AddMemoryBottomSheetDialog addMemoryBottomSheetDialog = new AddMemoryBottomSheetDialog();
                addMemoryBottomSheetDialog.show(getSupportFragmentManager(),"AddMemoryBottomSheet");

            }
        };
    }

    private void fetchUsersMemories(String currentUserId){
        calendarService.getAllMemories(currentUserId, new CalendarService.UsersMemoriesCallback() {
            @Override
            public void onError(Exception e) {
                Log.e("UsersMemoryCallback", "Error fetching memory: ", e);
                Toast toast = Toast.makeText(CalendarActivity.this,"Error fetching memories", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onMemoriesFetched(List<Memory> memories) {
                userMemories = memories;

            }
        });

    }

    private void addNewMemory(String name,String note,String date,String imageUrl,String userId,String relationshipId){
        Memory newMemory = new Memory();
        newMemory.setNote(note);
        newMemory.setMemoryName(name);
        newMemory.setDate(date);
        newMemory.setImageUrl(imageUrl);
        newMemory.setUserId(userId);
        newMemory.setRelationshipId(relationshipId);
        calendarService.addMemory(newMemory,this);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_calendar;
    }

    @Override
    public void onMemoryAdded(String name, String note, String date, String imageUrl) {
        addNewMemory(name,note, date,imageUrl, currentUser.getUid(),"");
    }

}