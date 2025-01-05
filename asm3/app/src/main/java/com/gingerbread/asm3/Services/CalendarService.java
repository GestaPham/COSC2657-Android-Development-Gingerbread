package com.gingerbread.asm3.Services;

import android.content.Context;
import android.widget.Toast;

import com.gingerbread.asm3.Models.Memory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CalendarService {
    private final CollectionReference memoriesCollection;
    private FirebaseFirestore firestore;

    public CalendarService(CollectionReference memoriesCollection) {
        this.memoriesCollection = memoriesCollection;
        firestore = FirebaseFirestore.getInstance();
    }
    public Memory getAMemory(String memoriesId){
        firestore.collection("memories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot memories : queryDocumentSnapshots){
                        if(memories.get("memoryId").equals(memoriesId)){
                            return memories;
                        }
                    }
                }).addOnSuccessListener(e->{
                    return null;
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
    public void addMemories(Memory memory, Context context){
        Toast toast = new Toast(context);
        String message;
        firestore.collection("memories")
                .add(memory)
                .addOnSuccessListener(DocumentReference ->{
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
}
