package com.gingerbread.asm3.Views.MoodTracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.gingerbread.asm3.Models.MoodLog;
import com.gingerbread.asm3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodTrackerActivity extends AppCompatActivity {

    private ImageView moodImageView, moodMonday, moodTuesday, moodWednesday, moodThursday, moodFriday, moodSaturday, moodSunday;
    private TextView moodTextView, dateTextView, todayFeelTextView, dateMonday, dateTuesday, dateWednesday, dateThursday, dateFriday, dateSaturday, dateSunday, moodSummaryText;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PieChart moodPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        moodImageView = findViewById(R.id.moodImageView);
        moodTextView = findViewById(R.id.moodTextView);
        dateTextView = findViewById(R.id.dateTextView);
        todayFeelTextView = findViewById(R.id.todayFeelText);
        moodSummaryText = findViewById(R.id.moodSummaryText);
        moodPieChart = findViewById(R.id.moodPieChart);

        // Weekly Timeline Icons and Dates
        moodMonday = findViewById(R.id.moodMonday);
        moodTuesday = findViewById(R.id.moodTuesday);
        moodWednesday = findViewById(R.id.moodWednesday);
        moodThursday = findViewById(R.id.moodThursday);
        moodFriday = findViewById(R.id.moodFriday);
        moodSaturday = findViewById(R.id.moodSaturday);
        moodSunday = findViewById(R.id.moodSunday);

        dateMonday = findViewById(R.id.dateMonday);
        dateTuesday = findViewById(R.id.dateTuesday);
        dateWednesday = findViewById(R.id.dateWednesday);
        dateThursday = findViewById(R.id.dateThursday);
        dateFriday = findViewById(R.id.dateFriday);
        dateSaturday = findViewById(R.id.dateSaturday);
        dateSunday = findViewById(R.id.dateSunday);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        loadMoodData();
    }

    private void loadMoodData() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("mood_logs")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout layoutNoData = findViewById(R.id.layoutNoData);
                    LinearLayout timelineContainer = findViewById(R.id.timelineContainer);
                    PieChart moodPieChart = findViewById(R.id.moodPieChart);
                    TextView todayFeelText = findViewById(R.id.todayFeelText);
                    ImageView moodImageView = findViewById(R.id.moodImageView);
                    TextView moodTextView = findViewById(R.id.moodTextView);
                    TextView dateTextView = findViewById(R.id.dateTextView);
                    TextView moodSummaryText = findViewById(R.id.moodSummaryText);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<MoodLog> moodLogs = queryDocumentSnapshots.toObjects(MoodLog.class);

                        if (!moodLogs.isEmpty()) {
                            layoutNoData.setVisibility(View.GONE);
                            timelineContainer.setVisibility(View.VISIBLE);
                            moodPieChart.setVisibility(View.VISIBLE);
                            todayFeelText.setVisibility(View.VISIBLE);
                            moodImageView.setVisibility(View.VISIBLE);
                            moodTextView.setVisibility(View.VISIBLE);
                            dateTextView.setVisibility(View.VISIBLE);
                            moodSummaryText.setVisibility(View.VISIBLE);

                            MoodLog todayMood = moodLogs.get(0);
                            moodTextView.setText(todayMood.getMood());
                            dateTextView.setText(todayMood.getDate());
                            moodImageView.setImageResource(getMoodIcon(todayMood.getMood()));

                            updateWeeklyTimeline(moodLogs);
                            generateMoodPieChart(moodLogs);
                        } else {
                            layoutNoData.setVisibility(View.VISIBLE);
                            timelineContainer.setVisibility(View.GONE);
                            moodPieChart.setVisibility(View.GONE);
                            todayFeelText.setVisibility(View.GONE);
                            moodImageView.setVisibility(View.GONE);
                            moodTextView.setVisibility(View.GONE);
                            dateTextView.setVisibility(View.GONE);
                            moodSummaryText.setVisibility(View.GONE);
                        }
                    } else {
                        layoutNoData.setVisibility(View.VISIBLE);
                        timelineContainer.setVisibility(View.GONE);
                        moodPieChart.setVisibility(View.GONE);
                        todayFeelText.setVisibility(View.GONE);
                        moodImageView.setVisibility(View.GONE);
                        moodTextView.setVisibility(View.GONE);
                        dateTextView.setVisibility(View.GONE);
                        moodSummaryText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load mood data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.layoutNoData).setVisibility(View.VISIBLE);
                    findViewById(R.id.timelineContainer).setVisibility(View.GONE);
                    findViewById(R.id.moodPieChart).setVisibility(View.GONE);
                    findViewById(R.id.todayFeelText).setVisibility(View.GONE);
                    findViewById(R.id.moodImageView).setVisibility(View.GONE);
                    findViewById(R.id.moodTextView).setVisibility(View.GONE);
                    findViewById(R.id.dateTextView).setVisibility(View.GONE);
                    findViewById(R.id.moodSummaryText).setVisibility(View.GONE);
                });
    }


    private void updateWeeklyTimeline(List<MoodLog> moodLogs) {
        LinearLayout dayContainer = findViewById(R.id.dayContainer);
        dayContainer.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        int todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar startOfWeek = (Calendar) calendar.clone();
        startOfWeek.add(Calendar.DAY_OF_WEEK, -todayIndex);

        Map<String, Integer> weekDateMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            weekDateMap.put(dateFormat.format(startOfWeek.getTime()), i);
            startOfWeek.add(Calendar.DAY_OF_WEEK, 1);
        }

        Calendar currentDay = (Calendar) calendar.clone();
        currentDay.add(Calendar.DAY_OF_WEEK, -todayIndex);

        for (int i = 0; i < 7; i++) {
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            dayLayout.setGravity(Gravity.CENTER);

            Calendar dayInstance = (Calendar) currentDay.clone();

            ImageView moodIcon = new ImageView(this);
            moodIcon.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
            moodIcon.setImageResource(R.drawable.ic_circle);

            // Set day and date labels
            TextView dayText = new TextView(this);
            dayText.setText(dayFormat.format(dayInstance.getTime()));
            dayText.setTextColor(getResources().getColor(R.color.text_dark));
            dayText.setTextSize(12);
            dayText.setGravity(Gravity.CENTER);

            TextView dateText = new TextView(this);
            dateText.setText(String.valueOf(dayInstance.get(Calendar.DAY_OF_MONTH)));
            dateText.setTextColor(getResources().getColor(R.color.text_dark));
            dateText.setTextSize(14);
            dateText.setGravity(Gravity.CENTER);

            if (i == todayIndex) {
                dayLayout.setBackgroundResource(R.drawable.active_day_background);
            }

            dayLayout.addView(moodIcon);
            dayLayout.addView(dayText);
            dayLayout.addView(dateText);

            dayContainer.addView(dayLayout);

            currentDay.add(Calendar.DAY_OF_WEEK, 1);
        }

        for (MoodLog log : moodLogs) {
            try {
                Calendar logDate = Calendar.getInstance();
                logDate.setTime(dateFormat.parse(log.getDate().split(" ")[0]));

                String logDateString = dateFormat.format(logDate.getTime());
                if (weekDateMap.containsKey(logDateString)) {
                    int dayIndex = weekDateMap.get(logDateString);

                    LinearLayout dayLayout = (LinearLayout) dayContainer.getChildAt(dayIndex);
                    ImageView moodIcon = (ImageView) dayLayout.getChildAt(0);
                    moodIcon.setImageResource(getMoodIcon(log.getMood()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateMoodPieChart(List<MoodLog> moodLogs) {
        Map<String, Integer> moodCounts = new HashMap<>();
        for (MoodLog log : moodLogs) {
            String mood = log.getMood();
            moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        int totalLogs = moodLogs.size();
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            float percentage = (entry.getValue() * 100f) / totalLogs;
            pieEntries.add(new PieEntry(percentage, entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(new int[]{Color.parseColor("#FF8A80"), // Excited
                Color.parseColor("#FFD180"), // Happy
                Color.parseColor("#FFECB3"), // Okay
                Color.parseColor("#80CBC4"), // Tired
                Color.parseColor("#90CAF9")  // Bad
        });
        PieData pieData = new PieData(pieDataSet);
        moodPieChart.setData(pieData);
        moodPieChart.invalidate();

        String mostFrequentMood = getMostFrequentMood(moodCounts);
        moodSummaryText.setText("This month, you felt " + mostFrequentMood + " the most.");
    }

    private String getMostFrequentMood(Map<String, Integer> moodCounts) {
        String mostFrequentMood = "Neutral";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostFrequentMood = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return mostFrequentMood;
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
                return R.drawable.ic_mood_okay;
        }
    }
}
