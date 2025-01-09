package com.gingerbread.asm3.Views.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
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

import androidx.recyclerview.widget.LinearLayoutManager;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.gingerbread.asm3.Adapter.EventAdapter;
import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Event;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.Models.Relationship;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.RelationshipService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Services.CalendarService;
import com.gingerbread.asm3.Views.Home.MainActivity;
import com.gingerbread.asm3.Views.Memory.MemoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends BaseActivity implements AddMemoryBottomSheetDialog.AddMemoryListener, MemoryAdapter.OnMemoryClickListener {
    private CalendarView calendarView;
    private ImageButton addMemoryButton2, addEventButton2;
    private long selectedDate;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private MemoryAdapter memoryAdapter;
    private HashMap<Long, List<String>> eventsMap = new HashMap<>();
    private HashMap<String, Memory> memoryHashMap = new HashMap<>();
    private HashMap<String, User> userHashMap = new HashMap<>();
    private List<Memory> userMemories = new ArrayList<>();
    private CalendarService calendarService = new CalendarService();
    private TextView viewAll;
    private FirebaseFirestore firestore;
    private RelationshipService relationshipService = new RelationshipService();
    private UserService userService = new UserService();
    private String relationshipId;
    private String argUserSharedToken;
    private List<Event> eventList;
    private EventAdapter eventAdapter;


    Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.activity_content));
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        calendarView = findViewById(R.id.calendarView);
        addEventButton2 = findViewById(R.id.addEventButton2);
        addMemoryButton2 = findViewById(R.id.addMemoryButton2);
        viewAll = findViewById(R.id.viewAllLink);
        selectedDate = calendarView.getDate();

        RecyclerView recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, event -> {
            // Handle event click
            Toast.makeText(this, "Clicked: " + event.getEventName(), Toast.LENGTH_SHORT).show();
        });

        recyclerViewEvents.setAdapter(eventAdapter);

        fetchEventsForDate(selectedDate);

        ViewPager2 viewPagerMemories = findViewById(R.id.viewPagerMemories);

        // Use the shared userMemories list for the adapter
        memoryAdapter = new MemoryAdapter(userMemories, this);
        viewPagerMemories.setAdapter(memoryAdapter);

        // Fetch memories for current user
        fetchUsersMemories(currentUser.getUid(), null);

        memoryAdapter = new MemoryAdapter(userMemories, this);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
                Toast.makeText(CalendarActivity.this, "Selected Date: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });

        addEventButton2.setOnClickListener(v -> addEvent());
        addMemoryButton2.setOnClickListener(v -> addMemory());
        viewAll.setOnClickListener(v -> {
            fetchUsersMemories(currentUser.getUid(), () -> {
                String memoriesJson = gson.toJson(userMemories);
                Log.d("user memories", memoriesJson.toString());
                Intent intent = new Intent(CalendarActivity.this, MemoryActivity.class);
                intent.putExtra("memoriesJson", memoriesJson);
                startActivity(intent);
            });


        });
        fetchEventsForDate(selectedDate);
    }

    private void fetchEventsForDate(long selectedDate) {
        calendarService.getAllEvents(currentUser.getUid(), new CalendarService.EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (eventList != null) {
                    eventList.clear();
                    eventList.addAll(events);
                    eventAdapter.notifyDataSetChanged();

                    TextView noEventsText = findViewById(R.id.noEventsText);
                    if (eventList.isEmpty()) {
                        noEventsText.setVisibility(View.VISIBLE);
                    } else {
                        noEventsText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CalendarActivity.this, "Error fetching events: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEvent(){
        AddEventBottomSheetDialog addEventBottomSheetDialog = new AddEventBottomSheetDialog();
        addEventBottomSheetDialog.show(getSupportFragmentManager(),"AddEventBottomSheet");
        /*
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
        }*/
    }

    private void addMemory() {
        AddMemoryBottomSheetDialog addMemoryBottomSheetDialog = new AddMemoryBottomSheetDialog();
        addMemoryBottomSheetDialog.show(getSupportFragmentManager(), "AddMemoryBottomSheet");
        Log.d("CurrentUserID", currentUser.getUid());
    }

    private void fetchUsersMemories(String currentUserId, Runnable onComplete) {
        calendarService.getAllMemories(currentUserId, new CalendarService.UsersMemoriesCallback() {
            @Override
            public void onError(Exception e) {
                Log.e("UsersMemoryCallback", "Error fetching memory: ", e);
                Toast.makeText(CalendarActivity.this, "Error fetching memories", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMemoriesFetched(List<Memory> memories) {
                userMemories.clear();
                userMemories.addAll(memories);
                memoryAdapter.notifyDataSetChanged();

                TextView noMemoriesText = findViewById(R.id.noMemoriesText);
                if (userMemories.isEmpty()) {
                    noMemoriesText.setVisibility(View.VISIBLE);
                } else {
                    noMemoriesText.setVisibility(View.GONE);
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void addNewMemory(String name, String note, String date, String imageUrl, String userId, String relationshipId) {
        Memory newMemory = new Memory();
        newMemory.setNote(note);
        newMemory.setMemoryName(name);
        newMemory.setDate(date);
        newMemory.setImageUrl(imageUrl);
        newMemory.setUserId(userId);
        newMemory.setRelationshipId(relationshipId);
        calendarService.addMemory(newMemory, this);
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
        Log.d("CurrentUserID", currentUser.getUid());
        getCurrentUserRelationship();

        if (relationshipId != null) {
            Log.d("relationShipId", relationshipId);
            addNewMemory(name, note, date, imageUrl, currentUser.getUid(), relationshipId);
        } else {
            addNewMemory(name, note, date, imageUrl, currentUser.getUid(), "");
        }
    }

    private void getCurrentUserRelationship() {
        userService.getUser(currentUser.getUid(), new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                User user = new Gson().fromJson(new Gson().toJson(userData), User.class);
                String userShareToken = user.getShareToken();
                if (userShareToken != null && userShareToken.startsWith("LINKED")) {
                    //argUserSharedToken = userShareToken;
                    relationshipService.getRelationshipByShareToken(userShareToken, new RelationshipService.RelationshipCallback() {
                        @Override
                        public void onSuccess(Relationship relationship) {
                            if (relationship != null) {
                                relationshipId = relationship.getRelationshipId();
                                Log.d("Relationship", "Relationship name: " + relationshipId);
                            } else {
                                Log.d("Relationship", "No relationship found for shareToken: " + userShareToken);
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e("RelationshipService", "Failed to fetch relationship: " + errorMessage);
                        }
                    });
                } else {
                    Log.d("Relationship", "No shareToken available for the current user.");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });

    }

    @Override
    public void onMemoryClick(Memory memory) {
        Intent intent = new Intent(this, MemoryActivity.class);
        String memoriesJson = new Gson().toJson(userMemories);
        intent.putExtra("memoriesJson", memoriesJson);
        startActivity(intent);
    }

}