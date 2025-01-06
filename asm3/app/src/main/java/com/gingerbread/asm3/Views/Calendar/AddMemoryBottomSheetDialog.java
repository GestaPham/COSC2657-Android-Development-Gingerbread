package com.gingerbread.asm3.Views.Calendar;

import static android.app.Activity.RESULT_OK;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class AddMemoryBottomSheetDialog extends BottomSheetDialogFragment {
    private AddMemoryListener listener;
    private Uri imageUri;
    private static int PICK_IMAGE_REQ = 1;
    private String uploadedImageUrl;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.add_memory_modal,container,false);
        EditText dateInput =v.findViewById(R.id.dateInput);
        EditText noteInput = v.findViewById(R.id.noteInput);
        EditText memoryNameInput = v.findViewById(R.id.memoryNameInput);
        ImageButton addPhotoButton = v.findViewById(R.id.addPhotoButton);
        Button addMemoryButton = v.findViewById(R.id.addMemoryButton);
        addPhotoButton.setOnClickListener(view->{
            openImagePicker();
        });
        addMemoryButton.setOnClickListener(view->{
            String date = dateInput.getText().toString();
            String note = noteInput.getText().toString();
            String name = memoryNameInput.getText().toString();

            listener.onMemoryAdded(name, note, date,uploadedImageUrl);
            dismiss();
        });
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddMemoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddMemoryListener");
        }
    }

    public interface AddMemoryListener{
        void onMemoryAdded(String name,String note,String date,String imageUrl);

    }
    private void uploadImage(){

    }
    private void openImagePicker(){
        Intent imagePickerIntent = new Intent();
        imagePickerIntent.setType("image/*");
        imagePickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imagePickerIntent,PICK_IMAGE_REQ);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // This is the URI of the selected image
        }
    }
    private void uploadImageToCloudStorage(){
        if(imageUri != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            String fileName = System.currentTimeMillis() + ".jpg";
            StorageReference fileRef = storageRef.child("images/" + fileName);
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot->{
                        fileRef.getDownloadUrl().addOnSuccessListener(uri->{
                            uploadedImageUrl =uri.toString();
                        });
                    }).addOnFailureListener(e->{});
        }else{

        }
    }
}
