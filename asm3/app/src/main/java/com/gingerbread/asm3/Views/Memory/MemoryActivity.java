package com.gingerbread.asm3.Views.Memory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MemoryActivity extends AppCompatActivity {
    private LinearLayout memoryContainer;
    private TextView noMemoriesText;
    private List<Memory> memoryList = new ArrayList<>();
    private String memoriesJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        memoryContainer = findViewById(R.id.memoryContainer);
        noMemoriesText = findViewById(R.id.noMemoriesText);

        Intent intent = getIntent();
        memoriesJson = intent.getStringExtra("memoriesJson");

        if (memoriesJson != null) {
            Type listType = new TypeToken<ArrayList<Memory>>() {
            }.getType();
            memoryList = new Gson().fromJson(memoriesJson, listType);
        }

        if (memoryList.isEmpty()) {
            noMemoriesText.setVisibility(View.VISIBLE);
        } else {
            noMemoriesText.setVisibility(View.GONE);
            displayMemories();
        }
    }

    private void displayMemories() {
        for (Memory memory : memoryList) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.memory_card, memoryContainer, false);

            TextView textMemoryTitle = cardView.findViewById(R.id.textMemoryTitle);
            TextView textMemoryDate = cardView.findViewById(R.id.textMemoryDate);
            TextView textMemoryNote = cardView.findViewById(R.id.textMemoryNote);

            textMemoryTitle.setText(memory.getMemoryName());
            textMemoryDate.setText(memory.getDate());
            textMemoryNote.setText(memory.getNote());

            cardView.setOnClickListener(v -> {
                Toast.makeText(this, "Clicked on: " + memory.getMemoryName(), Toast.LENGTH_SHORT).show();
            });

            memoryContainer.addView(cardView);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            cardView.setLayoutParams(params);
        }
    }
}
