package com.gingerbread.asm3.Views.Calendar;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import com.gingerbread.asm3.Models.Notification;
import com.gingerbread.asm3.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddMemoryBottomSheetDialog extends BottomSheetDialogFragment {

    private AddMemoryListener listener;
    private Uri imageUri;
    private String uploadedImageUrl;
    private static final int PICK_IMAGE_REQ = 1;
    private ImageButton addPhotoButton;
    private ProgressBar uploadProgressBar;
    private FirebaseFirestore firestore;
    private String relationshipId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_memory_modal, container, false);

        firestore = FirebaseFirestore.getInstance();

        EditText dateInput = v.findViewById(R.id.dateInput);
        EditText noteInput = v.findViewById(R.id.noteInput);
        EditText memoryNameInput = v.findViewById(R.id.memoryNameInput);
        addPhotoButton = v.findViewById(R.id.addPhotoButton);
        Button addMemoryButton = v.findViewById(R.id.buttonAddMemoryModal);
        uploadProgressBar = v.findViewById(R.id.uploadProgressBar);

        if (getArguments() != null) {
            relationshipId = getArguments().getString("relationshipId");
        }

        addPhotoButton.setOnClickListener(view -> openImagePicker());

        addMemoryButton.setOnClickListener(view -> {
            if (imageUri != null && (uploadedImageUrl == null || uploadedImageUrl.isEmpty())) {
                Toast.makeText(getContext(), "Please wait for the image upload to finish.", Toast.LENGTH_SHORT).show();
            } else {
                String date = dateInput.getText().toString().trim();
                String note = noteInput.getText().toString().trim();
                String name = memoryNameInput.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(note) || TextUtils.isEmpty(date)) {
                    Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (relationshipId == null) {
                    Toast.makeText(getContext(), "Relationship ID is missing.", Toast.LENGTH_SHORT).show();
                    return;
                }

                listener.onMemoryAdded(name, note, date, uploadedImageUrl != null ? uploadedImageUrl : "", relationshipId);

                sendNotification(name, note, relationshipId);

                if (getActivity() != null) {
                    getActivity().setResult(RESULT_OK);
                }

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
        void onMemoryAdded(String name, String note, String date, String imageUrl, String relationshipId);
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

    private void sendNotification(String title, String description, String relationshipId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("description", description);
        notificationData.put("relationshipId", relationshipId);
        notificationData.put("isRead", false);
        notificationData.put("readBy", new ArrayList<>());
        notificationData.put("timestamp", com.google.firebase.Timestamp.now());
        notificationData.put("type", "memory");

        firestore.collection("Notifications").add(notificationData).addOnSuccessListener(documentReference -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Memory notification sent!", Toast.LENGTH_SHORT).show();
            }
            Log.d("Notification", "Notification sent: " + documentReference.getId());
        }).addOnFailureListener(e -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Failed to send memory notification.", Toast.LENGTH_SHORT).show();
            }
            Log.e("Notification", "Failed to send notification: " + e.getMessage());
        });
    }

}
