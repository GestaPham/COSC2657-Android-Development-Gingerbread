package com.gingerbread.asm3.Views.Memory;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.Models.Memory;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MemoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MemoryAdapter memoryAdapter;
    private FirebaseFirestore firestore;
    private UserService userService;
    private List<Memory> memoryList = new ArrayList<>();
    private Intent allMemoryIntent = getIntent();
    private String memoriesJson = allMemoryIntent.getStringExtra("memoriesJson");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);
        recyclerView =findViewById(R.id.all_memories_recycler);
        if(memoriesJson!=null){
            Type listType = new TypeToken<ArrayList<Memory>>() {}.getType();
            memoryList = new Gson().fromJson(memoriesJson,listType);
        }
        memoryAdapter = new MemoryAdapter(memoryList,memory -> {

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memoryAdapter);

    }
}