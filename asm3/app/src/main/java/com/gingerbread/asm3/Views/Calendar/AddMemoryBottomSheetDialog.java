package com.gingerbread.asm3.Views.Calendar;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class AddMemoryBottomSheetDialog extends BottomSheetDialogFragment {
    private AddMemoryListener listener;
    private Uri imageUri;
    private String uploadedImageUrl;
    private static final int PICK_IMAGE_REQ = 1;
    private ImageButton addPhotoButton;
    private ProgressBar uploadProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_memory_modal, container, false);

        EditText dateInput = v.findViewById(R.id.dateInput);
        EditText noteInput = v.findViewById(R.id.noteInput);
        EditText memoryNameInput = v.findViewById(R.id.memoryNameInput);
        addPhotoButton = v.findViewById(R.id.addPhotoButton);
        Button addMemoryButton = v.findViewById(R.id.buttonAddMemoryModal);
        uploadProgressBar = v.findViewById(R.id.uploadProgressBar);

        addPhotoButton.setOnClickListener(view -> openImagePicker());

        addMemoryButton.setOnClickListener(view -> {
            if (imageUri != null && (uploadedImageUrl == null || uploadedImageUrl.isEmpty())) {
                Toast.makeText(getContext(), "Please wait for the image upload to finish.", Toast.LENGTH_SHORT).show();
            } else {
                String date = dateInput.getText().toString();
                String note = noteInput.getText().toString();
                String name = memoryNameInput.getText().toString();

                listener.onMemoryAdded(name, note, date, uploadedImageUrl != null ? uploadedImageUrl : "");
                dismiss();
            }
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

    public interface AddMemoryListener {
        void onMemoryAdded(String name, String note, String date, String imageUrl);
    }

    private void openImagePicker() {
        Intent imagePickerIntent = new Intent();
        imagePickerIntent.setType("image/*");
        imagePickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imagePickerIntent, PICK_IMAGE_REQ);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                addPhotoButton.setImageBitmap(bitmap);
                uploadImageToCloudStorage(imageUrl -> {
                    uploadedImageUrl = imageUrl;
                    Log.d("Firebase", "Uploaded URL is ready: " + uploadedImageUrl);
                });
            } catch (IOException e) {
                Log.e("Image Picker", "Error loading image: " + e.getMessage());
            }
        }
    }

    private void uploadImageToCloudStorage(ImageUploadListener listener) {
        if (imageUri != null) {
            uploadProgressBar.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            String uniqueFileName = "images/" + System.currentTimeMillis() + "_" + imageUri.getLastPathSegment();
            StorageReference fileRef = storageRef.child(uniqueFileName);

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString();
                uploadProgressBar.setVisibility(View.GONE);
                listener.onImageUploaded(uploadedImageUrl);
            })).addOnFailureListener(e -> {
                uploadProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Upload failed: " + e.getMessage());
            });
        }
    }

    public interface ImageUploadListener {
        void onImageUploaded(String imageUrl);
    }
}
