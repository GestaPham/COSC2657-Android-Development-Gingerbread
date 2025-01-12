package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

public class ProfileDetailsActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewEditProfile;
    private TextView textViewAge, textViewGender, textViewNationality, textViewReligion, textViewLocation;
    private User user;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            user = (User) result.getData().getSerializableExtra("user");
            loadProfileDetails();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile_details, findViewById(R.id.activity_content));

        profileImageView = findViewById(R.id.profileImageView);
        textViewName = findViewById(R.id.textViewName);
        textViewEditProfile = findViewById(R.id.textViewEditProfile);
        textViewAge = findViewById(R.id.textViewAge);
        textViewGender = findViewById(R.id.textViewGender);
        textViewNationality = findViewById(R.id.textViewNationality);
        textViewReligion = findViewById(R.id.textViewReligion);
        textViewLocation = findViewById(R.id.textViewLocation);

        user = (User) getIntent().getSerializableExtra("user");
        loadProfileDetails();

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        textViewEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileDetailsActivity.this, EditProfileActivity.class);
            intent.putExtra("user", user);
            editProfileLauncher.launch(intent);
        });

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("user", user);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }


    private void loadProfileDetails() {
        if (user != null) {
            textViewName.setText(user.getName());
            textViewAge.setText(String.valueOf(user.getAge()));
            textViewGender.setText(user.getGender());
            textViewNationality.setText(user.getNationality());
            textViewReligion.setText(user.getReligion());
            textViewLocation.setText(user.getLocation());

            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Glide.with(this).load(user.getProfilePictureUrl()).placeholder(R.drawable.ic_placeholder).into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            textViewName.setText("Error loading details");
            textViewAge.setText("-");
            textViewGender.setText("-");
            textViewNationality.setText("-");
            textViewReligion.setText("-");
            textViewLocation.setText("-");
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
