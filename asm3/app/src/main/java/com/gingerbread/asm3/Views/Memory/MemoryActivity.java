package com.gingerbread.asm3.Views.Memory;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.gingerbread.asm3.Adapter.MemoryAdapter;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MemoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MemoryAdapter memoryAdapter;
    private FirebaseFirestore firestore;
    private UserService userService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);
        recyclerView =findViewById(R.id.all_memories_recycler);
        //memoryAdapter = new MemoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memoryAdapter);

    }
}