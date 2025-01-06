package com.gingerbread.asm3.Services;

import android.content.Context;
import android.widget.Toast;

import com.gingerbread.asm3.Models.Memory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CalendarService {
    private final CollectionReference memoriesCollection;
    private FirebaseFirestore firestore;

    public CalendarService() {
        firestore = FirebaseFirestore.getInstance();
        this.memoriesCollection = firestore.collection("memories");
    }
    public void getAllMemories(String userId,MemoryCallback callback){
        firestore.collection("memories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Memory> memoriesList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        memoriesList.add(document.toObject(Memory.class));
                    }
                    callback.onMemoriesFetched(memoriesList);
                }).addOnFailureListener(e->{
                    callback.onError(e);
                });
    }
    public void updateMemory(String memoryId,String updateField,String updateValue){
        firestore.collection("memories").document(memoryId)
                .update(updateField,updateValue)
                .addOnSuccessListener(aVoid->{})
                .addOnFailureListener(e->{});
    }
    public void deleteMemory(String memoryId){
        firestore.collection("memories").document(memoryId)
                .delete()
                .addOnSuccessListener(aVoid->{})
                .addOnFailureListener(e->{});
    }
    public void addMemory(Memory memory, Context context){
        Toast toast = new Toast(context);
        String message;
        firestore.collection("memories")
                .add(memory)
                .addOnSuccessListener(documentReference ->{
                    String memoryId = documentReference.getId();
                    memory.setMemoryId(memoryId);
                    toast.setText("New memory added");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                })
                .addOnFailureListener(e -> {
                    toast.setText("Fail to add new memory");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                });
    }
    public interface UsersMemoriesCallback {
        void onError(Exception e);
        List<Memory> onMemoriesFetched(List<Memory> memories);
    }


}

