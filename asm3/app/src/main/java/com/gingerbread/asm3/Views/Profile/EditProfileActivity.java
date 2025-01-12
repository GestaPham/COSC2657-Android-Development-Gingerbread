package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQ = 1;

    private ImageView profileImageView;
    private EditText editTextName, editTextAge, editTextGender, editTextNationality, editTextReligion, editTextLocation;
    private Button buttonSave, buttonCancel;
    private UserService userService;
    private User user;
    private Uri imageUri;
    private String uploadedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_edit_profile, findViewById(R.id.activity_content));

        userService = new UserService();

        profileImageView = findViewById(R.id.profileImageView);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextNationality = findViewById(R.id.editTextNationality);
        editTextReligion = findViewById(R.id.editTextReligion);
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        user = (User) getIntent().getSerializableExtra("user");
        loadProfileDetails();

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        profileImageView.setOnClickListener(v -> openImagePicker());

        buttonSave.setOnClickListener(v -> saveProfileDetails());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadProfileDetails() {
        if (user != null) {
            editTextName.setText(user.getName());
            editTextAge.setText(String.valueOf(user.getAge()));
            editTextGender.setText(user.getGender());
            editTextNationality.setText(user.getNationality());
            editTextReligion.setText(user.getReligion());
            editTextLocation.setText(user.getLocation());

            if (!TextUtils.isEmpty(user.getProfilePictureUrl())) {
                Glide.with(this)
                        .load(user.getProfilePictureUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(profileImageView);
            }
        } else {
            Toast.makeText(this, "Error loading profile details", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        Intent imagePickerIntent = new Intent();
        imagePickerIntent.setType("image/*");
        imagePickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(imagePickerIntent, PICK_IMAGE_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                uploadImageToCloudStorage(imageUrl -> uploadedImageUrl = imageUrl);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToCloudStorage(ImageUploadListener listener) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            String uniqueFileName = "profile_images/" + user.getUserId() + ".jpg";
            StorageReference fileRef = storageRef.child(uniqueFileName);

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString();
                listener.onImageUploaded(uploadedImageUrl);
            })).addOnFailureListener(e -> {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public interface ImageUploadListener {
        void onImageUploaded(String imageUrl);
    }

    private void saveProfileDetails() {
        String name = editTextName.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String nationality = editTextNationality.getText().toString().trim();
        String religion = editTextReligion.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(gender)
                || TextUtils.isEmpty(nationality) || TextUtils.isEmpty(religion) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Age must be a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        user.setNationality(nationality);
        user.setReligion(religion);
        user.setLocation(location);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("gender", gender);
        updates.put("nationality", nationality);
        updates.put("religion", religion);
        updates.put("location", location);

        if (!TextUtils.isEmpty(uploadedImageUrl)) {
            updates.put("profilePictureUrl", uploadedImageUrl);
        }

        String userId = userService.getCurrentUserId();
        if (userId != null) {
            userService.updateUser(userId, updates, new UserService.UpdateCallback() {
                @Override
                public void onSuccess() {
                    if (!TextUtils.isEmpty(uploadedImageUrl)) {
                        user.setProfilePictureUrl(uploadedImageUrl);
                    }
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("user", user);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_profile;
    }
}
