package com.gingerbread.asm3.Views.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.Models.MoodLog;
import com.gingerbread.asm3.Models.Relationship;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.MilestoneService;
import com.gingerbread.asm3.Services.MoodService;
import com.gingerbread.asm3.Services.RelationshipService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Views.MoodTracker.MoodTrackerActivity;
import com.gingerbread.asm3.Views.Notification.NotificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private ImageView imageViewProfile, notificationIcon, selectedMoodImage;
    private TextView textViewGreeting, textViewTogetherYears, textViewTogetherMonths, textViewTogetherDays, textViewMoodAdvice, textViewMoodName;
    private ViewPager2 viewPagerMemories;
    private MemoryAdapter memoryAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private User user;
    private UserService userService;

    private Relationship relationship;
    private RelationshipService relationshipService;

    private MoodService moodService;
    private MilestoneService milestoneService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.activity_content));

        imageViewProfile = findViewById(R.id.profileImage);
        notificationIcon = findViewById(R.id.notificationIcon);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        selectedMoodImage = findViewById(R.id.selectedMoodImage);
        textViewMoodAdvice = findViewById(R.id.textViewMoodAdvice);
        textViewMoodName = findViewById(R.id.textViewMoodName);

        View togetherStatsView = findViewById(R.id.togetherCard);
        textViewTogetherYears = togetherStatsView.findViewById(R.id.togetherYears);
        textViewTogetherMonths = togetherStatsView.findViewById(R.id.togetherMonths);
        textViewTogetherDays = togetherStatsView.findViewById(R.id.togetherDays);

        viewPagerMemories = findViewById(R.id.viewPagerMemories);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userService = new UserService();
        relationshipService = new RelationshipService();
        milestoneService = new MilestoneService();
        moodService = new MoodService();

        fetchData();

        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        Button moodTrackerButton = findViewById(R.id.moodTrackerButton);
        moodTrackerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MoodTrackerActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_home;
    }

    private void fetchData() {
        String userId = userService.getCurrentUserId();

        if (userId == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.getUser(userId, new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                user = new Gson().fromJson(new Gson().toJson(userData), User.class);

                String userShareToken = user.getShareToken();

                displayUserDetails();

                if (userShareToken != null && userShareToken.startsWith("LINKED")) {
                    loadDateTogetherStats();
                    initializeMoodTracking();
                    initializeMemoryCarousel();
                } else {
                    displayNoPartnerMessage();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "Error fetching user data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNoPartnerMessage() {
        getLayoutInflater().inflate(R.layout.item_no_partner, findViewById(R.id.activity_content));
    }

    private void displayUserDetails() {
        if (user != null) {
            textViewGreeting.setText("Hi, " + user.getName());

            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfilePictureUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    private void loadDateTogetherStats() {
        if (user.getShareToken() == null || user.getShareToken().isEmpty()) {
            Toast.makeText(this, "No linked relationship found", Toast.LENGTH_SHORT).show();
            return;
        }

        relationshipService.getRelationshipByShareToken(user.getShareToken(), new RelationshipService.RelationshipCallback() {
            @Override
            public void onSuccess(Relationship relationship) {
                if (relationship != null) {
                    displayDaysTogether(relationship.getDaysTogether());
                } else {
                    createRelationship();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("RelationshipService", "Fetching relationship for shareToken: " + user.getShareToken());

                Toast.makeText(MainActivity.this, "Error fetching relationship stats: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRelationship() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Relationship newRelationship = new Relationship();
        newRelationship.setShareToken(user.getShareToken());
        newRelationship.setStartDate(currentDate);
        newRelationship.setDaysTogether(1);
        newRelationship.setRelationshipStatus("Active");

        relationshipService.createRelationship(newRelationship, new RelationshipService.RelationshipCallback() {
            @Override
            public void onSuccess(Relationship relationship) {
                displayDaysTogether(relationship.getDaysTogether());
                Toast.makeText(MainActivity.this, "Relationship created successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "Error creating relationship: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDaysTogether(int daysTogether) {
        int years = daysTogether / 365;
        int months = (daysTogether % 365) / 30;
        int days = (daysTogether % 365) % 30;

        textViewTogetherYears.setText(String.valueOf(years));
        textViewTogetherMonths.setText(String.valueOf(months));
        textViewTogetherDays.setText(String.valueOf(days));
    }

    private void initializeMoodTracking() {
        fetchLatestMoodLog();

        findViewById(R.id.moodBad).setOnClickListener(v -> updateMood("Bad", R.drawable.ic_mood_bad));
        findViewById(R.id.moodTired).setOnClickListener(v -> updateMood("Tired", R.drawable.ic_mood_tired));
        findViewById(R.id.moodOkay).setOnClickListener(v -> updateMood("Okay", R.drawable.ic_mood_okay));
        findViewById(R.id.moodHappy).setOnClickListener(v -> updateMood("Happy", R.drawable.ic_mood_happy));
        findViewById(R.id.moodExcited).setOnClickListener(v -> updateMood("Excited", R.drawable.ic_mood_excited));
    }

    private void fetchLatestMoodLog() {
        String userId = user.getUserId();

        moodService.getLatestMoodLog(userId, new MoodService.MoodLogCallback() {
            @Override
            public void onSuccess(MoodLog latestMoodLog) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String lastLoggedDate = latestMoodLog.getDate();

                    Calendar calendar = Calendar.getInstance();
                    Calendar resetCalendar = Calendar.getInstance();
                    resetCalendar.set(Calendar.HOUR_OF_DAY, 6);
                    resetCalendar.set(Calendar.MINUTE, 0);
                    resetCalendar.set(Calendar.SECOND, 0);

                    Date now = calendar.getTime();
                    Date resetTime = resetCalendar.getTime();
                    Date lastLogDate = dateFormat.parse(lastLoggedDate);

                    if (now.after(resetTime) && (lastLogDate.before(resetTime) || !isSameDay(lastLogDate, now))) {
                        enableMoodSelection();
                    } else {
                        disableMoodSelection(latestMoodLog);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to parse date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                enableMoodSelection();
            }
        });
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void enableMoodSelection() {
        findViewById(R.id.moodOptionsContainer).setVisibility(View.VISIBLE);
        selectedMoodImage.setVisibility(View.GONE);
        textViewMoodName.setVisibility(View.GONE);
        textViewMoodAdvice.setVisibility(View.GONE);
    }

    private void disableMoodSelection(MoodLog latestMoodLog) {
        findViewById(R.id.moodOptionsContainer).setVisibility(View.GONE);

        selectedMoodImage.setVisibility(View.VISIBLE);
        textViewMoodName.setVisibility(View.VISIBLE);
        textViewMoodAdvice.setVisibility(View.VISIBLE);

        selectedMoodImage.setImageResource(getMoodIcon(latestMoodLog.getMood()));
        textViewMoodName.setText(latestMoodLog.getMood());
        textViewMoodAdvice.setText(latestMoodLog.getNotes());
    }

    private void updateMood(String mood, int moodIcon) {
        try {
            InputStream is = getAssets().open("mock_mood_advice.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONObject moodsObject = jsonObject.getJSONObject("moods");
            String advice = moodsObject.getString(mood);

            selectedMoodImage.setVisibility(View.VISIBLE);
            textViewMoodName.setVisibility(View.VISIBLE);
            textViewMoodAdvice.setVisibility(View.VISIBLE);

            selectedMoodImage.setImageResource(moodIcon);
            textViewMoodName.setText(mood);
            textViewMoodAdvice.setText(advice);

            findViewById(R.id.moodOptionsContainer).setVisibility(View.GONE);

            saveMoodLog(mood, advice, () -> {
                Intent intent = new Intent(MainActivity.this, MoodTrackerActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading mood advice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMoodLog(String mood, String advice, Runnable onComplete) {
        String userId = auth.getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        MoodLog moodLog = new MoodLog(null, userId, date, mood, advice);

        firestore.collection("mood_logs").add(moodLog).addOnSuccessListener(documentReference -> {
            String generatedLogId = documentReference.getId();
            documentReference.update("logId", generatedLogId).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Mood logged successfully", Toast.LENGTH_SHORT).show();
                disableMoodSelection(moodLog);

                if (onComplete != null) {
                    onComplete.run();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update logId: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to log mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void autoScrollMemories() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPagerMemories.getCurrentItem();
                int itemCount = memoryAdapter.getItemCount();
                viewPagerMemories.setCurrentItem((currentItem + 1) % itemCount, true);
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void initializeMemoryCarousel() {
        try {
            InputStream is = getAssets().open("mock_memories.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray memoryArray = jsonObject.getJSONArray("memories");

            List<Memory> memories = new ArrayList<>();
            for (int i = 0; i < memoryArray.length(); i++) {
                JSONObject obj = memoryArray.getJSONObject(i);

                memories.add(new Memory(obj.getString("memoryId"), obj.getString("memoryName"), obj.getString("date"), obj.getString("note"), obj.getString("imageUrl"), obj.optString("userId", "defaultUserId"), obj.optString("relationshipId", "defaultRelationshipId")));
            }

            memoryAdapter = new MemoryAdapter(memories, memory -> {

            });

            viewPagerMemories.setAdapter(memoryAdapter);
            viewPagerMemories.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            autoScrollMemories();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading memories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getMoodIcon(String mood) {
        switch (mood) {
            case "Excited":
                return R.drawable.ic_mood_excited;
            case "Happy":
                return R.drawable.ic_mood_happy;
            case "Okay":
                return R.drawable.ic_mood_okay;
            case "Tired":
                return R.drawable.ic_mood_tired;
            case "Bad":
                return R.drawable.ic_mood_bad;
            default:
                return R.drawable.ic_circle;
        }
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                float x = event.getRawX() + v.getLeft() - location[0];
                float y = event.getRawY() + v.getTop() - location[1];

                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    hideKeyboard();
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
