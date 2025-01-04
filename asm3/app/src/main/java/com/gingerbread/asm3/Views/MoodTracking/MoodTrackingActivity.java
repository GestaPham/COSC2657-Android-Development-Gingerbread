package com.gingerbread.asm3.Views.MoodTracking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.gingerbread.asm3.Adapter.MoodTimelineAdapter;
import com.gingerbread.asm3.Models.MoodLog;
import com.gingerbread.asm3.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MoodTrackingActivity extends AppCompatActivity {

    private GraphView moodChart;
    private TextView overallMoodText;
    private RecyclerView moodTimeline;
    private Spinner filterSpinner;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private List<MoodLog> moodLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracking);

        moodChart = findViewById(R.id.moodChart);
        overallMoodText = findViewById(R.id.overallMoodText);
        moodTimeline = findViewById(R.id.moodTimeline);
        filterSpinner = findViewById(R.id.filterSpinner);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupSpinner();
        fetchMoodData();

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        moodTimeline.setLayoutManager(layoutManager);


        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(moodTimeline);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.timeframe_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timeframe = parent.getItemAtPosition(position).toString();
                filterMoodData(timeframe);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default - daily
                filterMoodData("Daily");
            }
        });
    }

    private void fetchMoodData() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("mood_logs").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            moodLogs = queryDocumentSnapshots.toObjects(MoodLog.class);
            filterMoodData("Daily");
            displayMoodTimeline();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch mood data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterMoodData(String timeframe) {
        List<MoodLog> filteredLogs = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // filter
        switch (timeframe) {
            case "Daily":

                String today = String.format("%1$tY-%1$tm-%1$td", calendar);
                for (MoodLog log : moodLogs) {
                    if (log.getDate().startsWith(today)) {
                        filteredLogs.add(log);
                    }
                }
                break;

            case "Weekly":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                String weekStart = String.format("%1$tY-%1$tm-%1$td", calendar);
                calendar.add(Calendar.DATE, 6);
                String weekEnd = String.format("%1$tY-%1$tm-%1$td", calendar);

                for (MoodLog log : moodLogs) {
                    if (log.getDate().compareTo(weekStart) >= 0 && log.getDate().compareTo(weekEnd) <= 0) {
                        filteredLogs.add(log);
                    }
                }
                break;

            case "Monthly":
                String month = String.format("%1$tY-%1$tm", calendar);
                for (MoodLog log : moodLogs) {
                    if (log.getDate().startsWith(month)) {
                        filteredLogs.add(log);
                    }
                }
                break;
        }

        displayMoodChart(filteredLogs);
        calculateMoodInsights(filteredLogs);
    }

    private void displayMoodChart(List<MoodLog> logs) {
        moodChart.removeAllSeries();

        DataPoint[] dataPoints = new DataPoint[logs.size()];
        for (int i = 0; i < logs.size(); i++) {
            dataPoints[i] = new DataPoint(i, moodToValue(logs.get(i).getMood()));
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
        series.setSpacing(10);
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);

        series.setValueDependentColor(data -> {
            int index = (int) data.getX();
            return (index == logs.size() - 1) ? Color.parseColor("#FFC0CB") : Color.parseColor("#ADD8E6");
        });

        moodChart.addSeries(series);
        moodChart.getViewport().setYAxisBoundsManual(true);
        moodChart.getViewport().setMinY(1);
        moodChart.getViewport().setMaxY(5);
        moodChart.getViewport().setXAxisBoundsManual(true);
        moodChart.getViewport().setMinX(0);
        moodChart.getViewport().setMaxX(logs.size() - 1);
        moodChart.getGridLabelRenderer().setHorizontalAxisTitle("Mood Logs");
        moodChart.getGridLabelRenderer().setVerticalAxisTitle("Mood (1-5)");
    }

    private void displayMoodTimeline() {
        MoodTimelineAdapter adapter = new MoodTimelineAdapter(moodLogs);
        moodTimeline.setAdapter(adapter);
    }

    private void calculateMoodInsights(List<MoodLog> logs) {
        if (logs.isEmpty()) {
            overallMoodText.setText("No data to display.");
            return;
        }

        int totalMoodValue = 0;
        for (MoodLog log : logs) {
            totalMoodValue += moodToValue(log.getMood());
        }

        float averageMoodValue = (float) totalMoodValue / logs.size();
        String insights;

        if (averageMoodValue >= 4) {
            insights = "You're generally in a great mood! Keep spreading positivity!";
        } else if (averageMoodValue >= 3) {
            insights = "You're doing fine. Focus on self-care and maintaining relationships.";
        } else {
            insights = "You may need some support. Consider reaching out to someone you trust.";
        }

        overallMoodText.setText(insights);
    }

    private int moodToValue(String mood) {
        switch (mood) {
            case "Bad":
                return 1;
            case "Tired":
                return 2;
            case "Okay":
                return 3;
            case "Happy":
                return 4;
            case "Excited":
                return 5;
            default:
                return 0;
        }
    }
}
