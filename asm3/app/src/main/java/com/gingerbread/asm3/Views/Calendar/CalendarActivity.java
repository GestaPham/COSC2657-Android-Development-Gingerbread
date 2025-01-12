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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CalendarActivity extends BaseActivity implements AddMemoryBottomSheetDialog.AddMemoryListener, MemoryAdapter.OnMemoryClickListener {


    private CalendarView calendarView;
    private ImageButton addMemoryButton2, addEventButton2;
    private long selectedDate;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private MemoryAdapter memoryAdapter;
    private List<Memory> userMemories = new ArrayList<>();
    private CalendarService calendarService = new CalendarService();
    private TextView viewAll;
    private FirebaseFirestore firestore;
    private RelationshipService relationshipService = new RelationshipService();
    private UserService userService = new UserService();
    private String relationshipId;
    private List<Event> eventList;
    private EventAdapter eventAdapter;
    private HashMap<Long, Boolean> eventDates = new HashMap<>();


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

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, event -> {
            // Handle event click
        });

        recyclerViewEvents.setAdapter(eventAdapter);

        fetchEventsForDate(selectedDate);

        ViewPager2 viewPagerMemories = findViewById(R.id.viewPagerMemories);

        memoryAdapter = new MemoryAdapter(userMemories, this);
        viewPagerMemories.setAdapter(memoryAdapter);

        fetchUsersMemories(currentUser.getUid(), null);

        memoryAdapter = new MemoryAdapter(userMemories, this);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
            fetchEventsForDate(selectedDate);
        });

        addEventButton2.setOnClickListener(v -> {
            AddEventBottomSheetDialog dialog = new AddEventBottomSheetDialog();

            Bundle bundle = new Bundle();
            bundle.putString("selectedDate", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
            getRelationshipId(new RelationshipIdCallback() {
                @Override
                public void onSuccess(String relationshipId) {
                    bundle.putString("relationshipId", relationshipId);
                    dialog.setArguments(bundle);

                    dialog.setAddEventListener((name, date, description, relId) -> {
                        Event event = new Event(UUID.randomUUID().toString(), currentUser.getUid(), relId, name, date, description, null);

                        calendarService.addEvent(event, CalendarActivity.this);
                        fetchEventsForDate(selectedDate);
                    });

                    dialog.show(getSupportFragmentManager(), "AddEventBottomSheetDialog");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CalendarActivity.this, "Error fetching relationship ID: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

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

    private void getRelationshipId(RelationshipIdCallback callback) {
        if (relationshipId != null) {
            callback.onSuccess(relationshipId);
            return;
        }

        if (currentUser == null || currentUser.getUid() == null) {
            callback.onFailure("User is not logged in or UID is null");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userShareToken = documentSnapshot.getString("shareToken");
                if (userShareToken != null && userShareToken.startsWith("LINKED")) {
                    relationshipService.getRelationshipByShareToken(userShareToken, new RelationshipService.RelationshipCallback() {
                        @Override
                        public void onSuccess(Relationship relationship) {
                            if (relationship != null) {
                                relationshipId = relationship.getRelationshipId();
                                callback.onSuccess(relationshipId);
                            } else {
                                callback.onFailure("No relationship found for the shareToken");
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            callback.onFailure("Failed to fetch relationship: " + errorMessage);
                        }
                    });
                } else {
                    callback.onFailure("No valid shareToken found for the current user");
                }
            } else {
                callback.onFailure("User document does not exist");
            }
        }).addOnFailureListener(e -> callback.onFailure("Error fetching user document: " + e.getMessage()));
    }

    public interface RelationshipIdCallback {
        void onSuccess(String relationshipId);

        void onFailure(String errorMessage);
    }

    private void fetchEventsForDate(long selectedDate) {
        getRelationshipId(new RelationshipIdCallback() {
            @Override
            public void onSuccess(String relationshipId) {
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate);

                calendarService.getEventsByDate(formattedDate, relationshipId, new CalendarService.EventsCallback() {
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

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CalendarActivity.this, "Unable to fetch relationship ID: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemory() {
        AddMemoryBottomSheetDialog addMemoryBottomSheetDialog = new AddMemoryBottomSheetDialog();

        getRelationshipId(new RelationshipIdCallback() {
            @Override
            public void onSuccess(String relationshipId) {
                Bundle bundle = new Bundle();
                bundle.putString("relationshipId", relationshipId);
                addMemoryBottomSheetDialog.setArguments(bundle);
                addMemoryBottomSheetDialog.show(getSupportFragmentManager(), "AddMemoryBottomSheetDialog");
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CalendarActivity.this, "Failed to retrieve relationship ID: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchUsersMemories(String currentUserId, Runnable onComplete) {
        getRelationshipId(new RelationshipIdCallback() {
            @Override
            public void onSuccess(String relationshipId) {
                calendarService.getAllMemoriesByRelationshipId(relationshipId, new CalendarService.UsersMemoriesCallback() {
                    @Override
                    public void onError(Exception e) {
                        Log.e("UsersMemoryCallback", "Error fetching memories: ", e);
                        Toast.makeText(CalendarActivity.this, "Error fetching memories", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onMemoriesFetched(List<Memory> memories) {
                        List<Memory> filteredMemories = new ArrayList<>();
                        for (Memory memory : memories) {
                            if (memory.getRelationshipId().equals(relationshipId)) {
                                filteredMemories.add(memory);
                            }
                        }

                        userMemories.clear();
                        userMemories.addAll(filteredMemories);
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

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CalendarActivity.this, "Error fetching relationship ID: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewMemory(String name, String note, String date, String imageUrl, String userId, String relationshipId) {
        Memory newMemory = new Memory();
        newMemory.setMemoryName(name);
        newMemory.setNote(note);
        newMemory.setDate(date);
        newMemory.setImageUrl(imageUrl);
        newMemory.setUserId(userId);
        newMemory.setRelationshipId(relationshipId);

        calendarService.addMemory(newMemory, CalendarActivity.this, () -> {
            fetchUsersMemories(currentUser.getUid(), null);
        });
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
    public void onMemoryAdded(String name, String note, String date, String imageUrl, String relationshipId) {
        Log.d("CurrentUserID", currentUser.getUid());

        if (relationshipId != null) {
            Log.d("RelationshipId", relationshipId);
            addNewMemory(name, note, date, imageUrl, currentUser.getUid(), relationshipId);
        } else {
            Toast.makeText(this, "Relationship ID is missing. Unable to add memory.", Toast.LENGTH_SHORT).show();
        }

        // Refresh memories after adding
        fetchUsersMemories(currentUser.getUid(), null);
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