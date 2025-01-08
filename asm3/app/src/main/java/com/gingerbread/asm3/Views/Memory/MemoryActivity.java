package com.gingerbread.asm3.Views.Memory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Memory;
import com.gingerbread.asm3.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MemoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MemoryAdapter memoryAdapter;
    private List<Memory> memoryList = new ArrayList<>();
    private String memoriesJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.all_memories_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memoryAdapter);

        Intent intent = getIntent();
        memoriesJson = intent.getStringExtra("memoriesJson");

        if (memoriesJson != null) {
            Type listType = new TypeToken<ArrayList<Memory>>() {}.getType();
            memoryList = new Gson().fromJson(memoriesJson, listType);
        }

        memoryAdapter = new MemoryAdapter(memoryList, memory -> {
            Toast.makeText(this, "Clicked on: " + memory.getMemoryName(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(memoryAdapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, android.view.View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 16);
            }
        });
    }
}
