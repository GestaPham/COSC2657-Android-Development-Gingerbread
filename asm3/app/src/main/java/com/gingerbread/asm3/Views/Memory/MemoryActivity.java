package com.gingerbread.asm3.Views.Memory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private EditText searchMemories;
    private List<Memory> memoryList = new ArrayList<>();
    private List<Memory> filteredList = new ArrayList<>();
    private String memoriesJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        memoryContainer = findViewById(R.id.memoryContainer);
        noMemoriesText = findViewById(R.id.noMemoriesText);
        searchMemories = findViewById(R.id.searchMemories);

        Intent intent = getIntent();
        memoriesJson = intent.getStringExtra("memoriesJson");

        if (memoriesJson != null) {
            Type listType = new TypeToken<ArrayList<Memory>>() {
            }.getType();
            memoryList = new Gson().fromJson(memoriesJson, listType);
        }

        filteredList.addAll(memoryList);
        updateMemoryList("");

        searchMemories.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMemoryList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateMemoryList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(memoryList);
        } else {
            for (Memory memory : memoryList) {
                if (memory.getMemoryName().toLowerCase().contains(query.toLowerCase())
                        || memory.getDate().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(memory);
                }
            }
        }

        memoryContainer.removeAllViews();

        if (filteredList.isEmpty()) {
            noMemoriesText.setVisibility(View.VISIBLE);
        } else {
            noMemoriesText.setVisibility(View.GONE);
            for (Memory memory : filteredList) {
                View cardView = LayoutInflater.from(this).inflate(R.layout.memory_card, memoryContainer, false);

                TextView textMemoryTitle = cardView.findViewById(R.id.textMemoryTitle);
                TextView textMemoryDate = cardView.findViewById(R.id.textMemoryDate);
                TextView textMemoryNote = cardView.findViewById(R.id.textMemoryNote);
                ImageView imageMemory = cardView.findViewById(R.id.imageMemory);

                textMemoryTitle.setText(memory.getMemoryName());
                textMemoryDate.setText(memory.getDate());
                textMemoryNote.setText(memory.getNote());

                if (memory.getImageUrl() != null && !memory.getImageUrl().isEmpty()) {
                    Glide.with(this).load(memory.getImageUrl()).placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_error_image).into(imageMemory);
                } else {
                    imageMemory.setImageResource(R.drawable.ic_placeholder_image);
                }

                imageMemory.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageMemory.setAdjustViewBounds(true);

                ViewGroup.LayoutParams imageParams = imageMemory.getLayoutParams();
                imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                imageParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imageMemory.setLayoutParams(imageParams);

                memoryContainer.addView(cardView);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                params.setMargins(0, 0, 0, 16);
                cardView.setLayoutParams(params);
            }
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
