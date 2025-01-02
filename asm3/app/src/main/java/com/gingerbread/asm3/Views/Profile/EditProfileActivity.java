package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private ImageView profileImageView;
    private EditText editTextName, editTextAge, editTextGender, editTextNationality, editTextReligion, editTextLocation;
    private Button buttonSave, buttonCancel;
    private UserService userService;
    private User user;

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
        } else {
            Toast.makeText(this, "Error loading profile details", Toast.LENGTH_SHORT).show();
        }
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

        String userId = userService.getCurrentUserId();
        if (userId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("age", age);
            updates.put("gender", gender);
            updates.put("nationality", nationality);
            updates.put("religion", religion);
            updates.put("location", location);

            userService.updateUser(userId, updates, new UserService.UpdateCallback() {
                @Override
                public void onSuccess() {
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
