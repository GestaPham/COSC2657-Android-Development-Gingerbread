package com.gingerbread.asm3.Views.Profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;

import java.util.Map;

public class ProfileDetailsActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewEditPartner;
    private TextView textViewAge, textViewGender, textViewNationality, textViewReligion, textViewLocation;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        userService = new UserService();

        profileImageView = findViewById(R.id.profileImageView);
        textViewName = findViewById(R.id.textViewName);
        textViewEditPartner = findViewById(R.id.textViewEditPartner);
        textViewAge = findViewById(R.id.textViewAge);
        textViewGender = findViewById(R.id.textViewGender);
        textViewNationality = findViewById(R.id.textViewNationality);
        textViewReligion = findViewById(R.id.textViewReligion);
        textViewLocation = findViewById(R.id.textViewLocation);

        loadProfileDetails();

        textViewEditPartner.setOnClickListener(v -> {
            //startActivity(new Intent(ProfileDetailsActivity.this, EditPartnerActivity.class));
        });
    }

    private void loadProfileDetails() {
        String userId = userService.getCurrentUserId();
        if (userId != null) {
            userService.getUser(userId, new UserService.UserCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    textViewName.setText(userData.get("name").toString());
                    textViewAge.setText(userData.get("age").toString());
                    textViewGender.setText(userData.get("gender").toString());
                    textViewNationality.setText(userData.get("nationality").toString());
                    textViewReligion.setText(userData.get("religion").toString());
                    textViewLocation.setText(userData.get("location").toString());
                }

                @Override
                public void onFailure(String errorMessage) {
                    textViewName.setText("Error loading details");
                }
            });
        }
    }
}
