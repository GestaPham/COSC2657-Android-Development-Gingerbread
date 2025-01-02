package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.Authentication.LoginActivity;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewPremiumStatus;
    private Button buttonMyPartner, buttonProfileDetails, buttonSupport, buttonLogout;

    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, findViewById(R.id.activity_content));

        userService = new UserService();

        profileImageView = findViewById(R.id.profileImageView);
        textViewName = findViewById(R.id.textViewName);
        textViewPremiumStatus = findViewById(R.id.textViewPremiumStatus);
        buttonMyPartner = findViewById(R.id.buttonMyPartner);
        buttonProfileDetails = findViewById(R.id.buttonProfileDetails);
        buttonSupport = findViewById(R.id.buttonSupport);
        buttonLogout = findViewById(R.id.buttonLogout);

        loadUserProfile();

        buttonMyPartner.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PartnerProfileActivity.class));
        });

        buttonProfileDetails.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ProfileDetailsActivity.class));
        });

        buttonSupport.setOnClickListener(v -> {
            //startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        String userId = userService.getCurrentUserId();
        if (userId != null) {
            userService.getUser(userId, new UserService.UserCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    textViewName.setText(userData.get("name").toString());

                    boolean isPremium = (boolean) userData.getOrDefault("isPremium", false);
                    if (isPremium) {
                        textViewPremiumStatus.setText("Premium User");
                        textViewPremiumStatus.setVisibility(View.VISIBLE);
                    } else {
                        textViewPremiumStatus.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    textViewName.setText("Error loading profile");
                    textViewPremiumStatus.setVisibility(View.GONE);
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
