package com.gingerbread.asm3.Views.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.Models.MoodLog;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Views.Notification.NotificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ImageView imageViewProfile, notificationIcon, selectedMoodImage;
    private TextView textViewGreeting, textViewTogetherYears, textViewTogetherMonths, textViewTogetherDays, textViewMoodAdvice, textViewMoodName;
    private ViewPager2 viewPagerMemories;
    private MemoryAdapter memoryAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

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

        loadMockData();
        fetchUserData();
        initializeMoodTracking();
        initializeMemoryCarousel();

        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
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

    private void fetchUserData() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);

                if (user != null) {
                    textViewGreeting.setText("Hi, " + user.getName());

                    if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                        Glide.with(this).load(user.getProfilePictureUrl()).placeholder(R.drawable.ic_placeholder).into(imageViewProfile);
                    } else {
                        imageViewProfile.setImageResource(R.drawable.ic_placeholder);
                    }
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMockData() {
        try {
            InputStream is = getAssets().open("mock_together_stats.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONObject statsObject = jsonObject.getJSONObject("togetherStats");

            int years = statsObject.getInt("years");
            int months = statsObject.getInt("months");
            int days = statsObject.getInt("days");

            textViewTogetherYears.setText(String.valueOf(years));
            textViewTogetherMonths.setText(String.valueOf(months));
            textViewTogetherDays.setText(String.valueOf(days));

        } catch (Exception e) {
            Toast.makeText(this, "Error loading mock data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeMoodTracking() {
        selectedMoodImage.setVisibility(View.GONE);
        textViewMoodAdvice.setVisibility(View.GONE);
        textViewMoodName.setVisibility(View.GONE);

        findViewById(R.id.moodBad).setOnClickListener(v -> updateMood("Bad", R.drawable.ic_mood_bad));
        findViewById(R.id.moodTired).setOnClickListener(v -> updateMood("Tired", R.drawable.ic_mood_tired));
        findViewById(R.id.moodOkay).setOnClickListener(v -> updateMood("Okay", R.drawable.ic_mood_okay));
        findViewById(R.id.moodHappy).setOnClickListener(v -> updateMood("Happy", R.drawable.ic_mood_happy));
        findViewById(R.id.moodExcited).setOnClickListener(v -> updateMood("Excited", R.drawable.ic_mood_excited));
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

            // Update mood display
            selectedMoodImage.setVisibility(View.VISIBLE);
            textViewMoodName.setVisibility(View.VISIBLE);
            textViewMoodAdvice.setVisibility(View.VISIBLE);

            selectedMoodImage.setImageResource(moodIcon);
            textViewMoodName.setText(mood);
            textViewMoodAdvice.setText(advice);

            findViewById(R.id.moodOptionsContainer).setVisibility(View.GONE);

            saveMoodLog(mood, advice);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading mood advice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMoodLog(String mood, String advice) {
        String userId = auth.getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String notes = advice;

        MoodLog moodLog = new MoodLog(null, userId, date, mood, notes);

        firestore.collection("mood_logs").add(moodLog).addOnSuccessListener(documentReference -> {
            String generatedLogId = documentReference.getId();
            documentReference.update("logId", generatedLogId).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Mood logged successfully with ID: " + generatedLogId, Toast.LENGTH_SHORT).show();
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

                memories.add(new Memory(
                        obj.getString("memoryId"),
                        obj.getString("memoryName"),
                        obj.getString("date"),
                        obj.getString("note"),
                        obj.getString("imageUrl"),
                        obj.optString("userId", "defaultUserId"),
                        obj.optString("relationshipId", "defaultRelationshipId")
                ));
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
