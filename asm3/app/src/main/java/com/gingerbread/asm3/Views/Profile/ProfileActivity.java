package com.gingerbread.asm3.Views.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

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
    private Button buttonMyPartner, buttonProfileDetails, buttonSupport, buttonUpgradePremium, buttonLogout;

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
        buttonUpgradePremium = findViewById(R.id.buttonUpgradePremium);

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

        buttonUpgradePremium.setOnClickListener(v -> showUpgradePremiumDialog());

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
                    user.setPremium(userData.get("isPremium") != null && (boolean) userData.get("isPremium"));
                    user.setShareToken(userData.get("shareToken") != null ? userData.get("shareToken").toString() : "");

                    textViewName.setText(user.getName());
                    textViewPremiumStatus.setVisibility(user.isPremium() ? View.VISIBLE : View.GONE);

                    buttonUpgradePremium.setVisibility(user.isPremium() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onFailure(String errorMessage) {
                    textViewName.setText("Error loading profile");
                }
            });
        }
    }

    private void showUpgradePremiumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upgrade_premium, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.buttonPurchasePremium).setOnClickListener(view -> {
            openStripePaymentPage();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openStripePaymentPage() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", "https://buy.stripe.com/test_8wMdUC63Lbgv5u86oo");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            updatePremiumStatusForBothUsers();
        }
    }

    private void updatePremiumStatusForBothUsers() {
        if (user == null || user.getShareToken() == null || !user.getShareToken().startsWith("LINKED_")) {
            Toast.makeText(this, "No valid relationship found to update premium status.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tokens = user.getShareToken().split("_");
        if (tokens.length != 3) {
            Toast.makeText(this, "Invalid share token format.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId1 = tokens[1];
        String userId2 = tokens[2];

        updateUserPremiumStatus(userId1, () -> updateUserPremiumStatus(userId2, () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Premium activated for both you and your partner!", Toast.LENGTH_SHORT).show();
                loadUserProfile();
            });
        }));
    }

    private void updateUserPremiumStatus(String userId, Runnable onSuccess) {
        userService.updateUser(userId, Map.of("isPremium", true), new UserService.UpdateCallback() {
            @Override
            public void onSuccess() {
                onSuccess.run();
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to update premium for user: " + errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
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
