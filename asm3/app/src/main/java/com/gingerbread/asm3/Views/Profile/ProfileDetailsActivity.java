package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

public class ProfileDetailsActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewEditPartner;
    private TextView textViewAge, textViewGender, textViewNationality, textViewReligion, textViewLocation;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile_details, findViewById(R.id.activity_content));

        profileImageView = findViewById(R.id.profileImageView);
        textViewName = findViewById(R.id.textViewName);
        textViewEditPartner = findViewById(R.id.textViewEditProfile);
        textViewAge = findViewById(R.id.textViewAge);
        textViewGender = findViewById(R.id.textViewGender);
        textViewNationality = findViewById(R.id.textViewNationality);
        textViewReligion = findViewById(R.id.textViewReligion);
        textViewLocation = findViewById(R.id.textViewLocation);

        user = (User) getIntent().getSerializableExtra("user");
        loadProfileDetails();

        textViewEditPartner.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileDetailsActivity.this, EditProfileActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });
    }

    private void loadProfileDetails() {
        if (user != null) {
            textViewName.setText(user.getName());
            textViewAge.setText(String.valueOf(user.getAge()));
            textViewGender.setText(user.getGender());
            textViewNationality.setText(user.getNationality());
            textViewReligion.setText(user.getReligion());
            textViewLocation.setText(user.getLocation());
        } else {
            textViewName.setText("Error loading details");
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
