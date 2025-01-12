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

import com.bumptech.glide.Glide;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.Authentication.LoginActivity;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.gingerbread.asm3.Views.Support.HelpCenterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewPremiumStatus;
    private Button buttonMyPartner, buttonProfileDetails, buttonSupport,
            buttonUpgradePremium, buttonLogout;

    private UserService userService;
    private User user;

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

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

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51OOwHOADvj1zBNJ9a3T5i1b63iBtAIFT6bl01kSwklXlADIxTKHfruK8PRFia3iVdtfMW7yNUhyfGQs24hsqyIft00ksYsRucF"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

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

        buttonUpgradePremium.setOnClickListener(v -> {
            showUpgradePremiumDialog();
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
                    user.setPremium(userData.get("premium") != null && (boolean) userData.get("premium"));
                    user.setShareToken(userData.get("shareToken") != null ? userData.get("shareToken").toString() : "");
                    user.setProfilePictureUrl(userData.get("profilePictureUrl") != null ? userData.get("profilePictureUrl").toString() : "");

                    textViewName.setText(user.getName());
                    textViewPremiumStatus.setVisibility(user.isPremium() ? View.VISIBLE : View.GONE);

                    if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(user.getProfilePictureUrl())
                                .placeholder(R.drawable.ic_placeholder)
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_placeholder);
                    }

                    if (user.isPremium() && user.getShareToken() != null && user.getShareToken().startsWith("LINKED")) {
                        buttonUpgradePremium.setEnabled(false);
                        buttonUpgradePremium.setVisibility(View.GONE);
                    } else {
                        buttonUpgradePremium.setEnabled(true);
                        buttonUpgradePremium.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    textViewName.setText("Error loading profile");
                    Toast.makeText(ProfileActivity.this, "Failed to load user profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpgradePremiumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upgrade_premium, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.buttonPurchasePremium).setOnClickListener(view -> {
            createPaymentIntentAndPresentSheet();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createPaymentIntentAndPresentSheet() {
        Map<String, Object> data = new HashMap<>();
        data.put("sharedToken", user.getShareToken());

        FirebaseFunctions.getInstance()
                .getHttpsCallable("createPaymentIntent")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {
                    if (httpsCallableResult.getData() != null) {
                        Map<String, Object> resultData = (Map<String, Object>) httpsCallableResult.getData();
                        if (resultData.containsKey("error")) {
                            Toast.makeText(this,
                                    "Error: " + resultData.get("error"),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            paymentIntentClientSecret = (String) resultData.get("clientSecret");
                            presentPaymentSheet();
                        }
                    } else {
                        Toast.makeText(this, "No response from server", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error calling createPaymentIntent: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration configuration =
                new PaymentSheet.Configuration("My Awesome App, Inc.");

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            setPremiumForUserAndPartner();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment canceled.", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(this,
                    "Payment failed: " + failedResult.getError().getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setPremiumForUserAndPartner() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("premium", true);

        userService.updateUser(user.getUserId(), updates, new UserService.UpdateCallback() {
            @Override
            public void onSuccess() {
                if (user.getShareToken() != null && user.getShareToken().startsWith("LINKED_")) {
                    String[] parts = user.getShareToken().split("_");
                    if (parts.length == 3) {
                        String userId1 = parts[1];
                        String userId2 = parts[2];
                        String partnerId = userId1.equals(user.getUserId()) ? userId2 : userId1;

                        userService.updateUser(partnerId, updates, new UserService.UpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ProfileActivity.this,
                                        "Both you and your partner are now Premium!",
                                        Toast.LENGTH_LONG).show();
                                loadUserProfile();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(ProfileActivity.this,
                                        "Error updating partner: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "You are now Premium!",
                            Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileActivity.this,
                        "Error updating user: " + errorMessage,
                        Toast.LENGTH_LONG).show();
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
