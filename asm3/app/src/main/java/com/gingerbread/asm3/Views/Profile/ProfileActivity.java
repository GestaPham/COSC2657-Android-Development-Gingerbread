package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.Authentication.LoginActivity;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Views.Support.HelpCenterActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewPremiumStatus;
    private Button buttonMyPartner, buttonProfileDetails, buttonSupport, buttonLogout;

    private UserService userService;
    private User user;

    private final ActivityResultLauncher<Intent> profileDetailsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    user = (User) result.getData().getSerializableExtra("user");
                    loadUserProfile();
                }
            });

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

        buttonProfileDetails.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileDetailsActivity.class);
            intent.putExtra("user", user);
            profileDetailsLauncher.launch(intent);
        });

        buttonMyPartner.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PartnerProfileActivity.class);
            startActivity(intent);
        });

        buttonSupport.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HelpCenterActivity.class);
            startActivity(intent);
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
                    user = new User();
                    user.setUserId(userId);
                    user.setName(userData.get("name") != null ? userData.get("name").toString() : "");
                    user.setAge(userData.get("age") != null ? (int) ((long) userData.get("age")) : 0);
                    user.setGender(userData.get("gender") != null ? userData.get("gender").toString() : "");
                    user.setNationality(userData.get("nationality") != null ? userData.get("nationality").toString() : "");
                    user.setReligion(userData.get("religion") != null ? userData.get("religion").toString() : "");
                    user.setLocation(userData.get("location") != null ? userData.get("location").toString() : "");
                    user.setPremium(userData.get("isPremium") != null && (boolean) userData.get("isPremium"));

                    textViewName.setText(user.getName());
                    textViewPremiumStatus.setVisibility(user.isPremium() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(String errorMessage) {
                    textViewName.setText("Error loading profile");
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
