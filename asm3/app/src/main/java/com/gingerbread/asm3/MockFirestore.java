package com.gingerbread.asm3;

import com.gingerbread.asm3.Models.Memory;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockFirestore {
    private Map<String, Memory> mockData = new HashMap<>();

    public Task<Void> addMemory(Memory memory) {
        memory.setMemoryId(UUID.randomUUID().toString());
        mockData.put(memory.getMemoryId(), memory);
        return Tasks.forResult(null);
    }

    public Task<List<Memory>> getAllMemories() {
        return Tasks.forResult(new ArrayList<>(mockData.values()));
    }

    public Task<Void> deleteMemory(String id) {
        mockData.remove(id);
        return Tasks.forResult(null);
    }
}

