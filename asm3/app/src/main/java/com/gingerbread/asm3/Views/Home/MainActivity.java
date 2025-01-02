package com.gingerbread.asm3.Views.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BaseActivity {

    private ImageView imageViewProfile, notificationIcon;
    private TextView textViewGreeting, textViewTogetherYears, textViewTogetherMonths, textViewTogetherDays;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.activity_content));

        imageViewProfile = findViewById(R.id.profileImage);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        notificationIcon = findViewById(R.id.notificationIcon);

        View togetherStatsView = findViewById(R.id.togetherCard);
        textViewTogetherYears = togetherStatsView.findViewById(R.id.togetherYears);
        textViewTogetherMonths = togetherStatsView.findViewById(R.id.togetherMonths);
        textViewTogetherDays = togetherStatsView.findViewById(R.id.togetherDays);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        fetchUserData();

        notificationIcon.setOnClickListener(v -> Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_home;
    }

    private void fetchUserData() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);

                if (user != null) {
                    textViewGreeting.setText("Hi, " + user.getName());

                    if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                        Glide.with(this).load(user.getProfilePictureUrl()).placeholder(R.drawable.ic_placeholder).into(imageViewProfile);
                    } else {
                        imageViewProfile.setImageResource(R.drawable.ic_placeholder);
                    }

                    textViewTogetherYears.setText("1");
                    textViewTogetherMonths.setText("5");
                    textViewTogetherDays.setText("12");
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
