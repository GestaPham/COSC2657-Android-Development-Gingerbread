package com.gingerbread.asm3.Views.Profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gingerbread.asm3.Models.Relationship;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.RelationshipService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PartnerProfileActivity extends BaseActivity {

    private ImageView partnerProfileImageView;
    private TextView partnerTextViewName, partnerTextViewAge, partnerTextViewGender, partnerTextViewNationality, partnerTextViewReligion, partnerTextViewLocation;
    private TextView partnerTextViewAgeLabel, partnerTextViewGenderLabel, partnerTextViewNationalityLabel, partnerTextViewReligionLabel, partnerTextViewLocationLabel;
    private EditText editTextPartnerEmail;
    private Button buttonSendInvite, buttonAcceptInvite, buttonDenyInvite;
    private ProgressBar progressBarLoading;
    private ScrollView contentLayout;

    private User partnerUser;
    private UserService userService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_partner_profile, findViewById(R.id.activity_content));

        partnerProfileImageView = findViewById(R.id.partnerProfileImageView);
        partnerTextViewName = findViewById(R.id.partnerTextViewName);

        partnerTextViewAgeLabel = findViewById(R.id.partnerTextViewAgeLabel);
        partnerTextViewGenderLabel = findViewById(R.id.partnerTextViewGenderLabel);
        partnerTextViewNationalityLabel = findViewById(R.id.partnerTextViewNationalityLabel);
        partnerTextViewReligionLabel = findViewById(R.id.partnerTextViewReligionLabel);
        partnerTextViewLocationLabel = findViewById(R.id.partnerTextViewLocationLabel);

        partnerTextViewAge = findViewById(R.id.partnerTextViewAge);
        partnerTextViewGender = findViewById(R.id.partnerTextViewGender);
        partnerTextViewNationality = findViewById(R.id.partnerTextViewNationality);
        partnerTextViewReligion = findViewById(R.id.partnerTextViewReligion);
        partnerTextViewLocation = findViewById(R.id.partnerTextViewLocation);

        editTextPartnerEmail = findViewById(R.id.editTextPartnerEmail);
        buttonSendInvite = findViewById(R.id.buttonSendInvite);
        buttonAcceptInvite = findViewById(R.id.buttonAcceptInvite);
        buttonDenyInvite = findViewById(R.id.buttonDenyInvite);

        progressBarLoading = findViewById(R.id.progressBarLoading);
        contentLayout = findViewById(R.id.contentLayout);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        userService = new UserService();
        loadCurrentUser();
    }

    private void showLoading() {
        progressBarLoading.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBarLoading.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void loadCurrentUser() {
        showLoading();
        String userId = userService.getCurrentUserId();
        Log.d("PartnerProfile", "loadCurrentUser: " + userId);
        if (userId != null) {
            userService.getUser(userId, new UserService.UserCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    currentUser = new User();
                    currentUser.setUserId(userId);
                    currentUser.setShareToken(userData.get("shareToken") != null ? userData.get("shareToken").toString() : null);
                    loadPartnerProfile();
                }

                @Override
                public void onFailure(String errorMessage) {
                    hideLoading();
                    Toast.makeText(PartnerProfileActivity.this, "Error loading user: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            hideLoading();
        }
    }

    private void loadPartnerProfile() {
        if (currentUser.getShareToken() != null) {
            userService.getPartnerByUserId(currentUser.getUserId(), new UserService.UserCallback() {
                @Override
                public void onSuccess(Map<String, Object> partnerData) {
                    if (partnerData != null) {
                        partnerUser = new User();
                        partnerUser.setName(partnerData.get("name") != null ? partnerData.get("name").toString() : "N/A");
                        partnerUser.setAge(partnerData.get("age") != null ? (int) ((long) partnerData.get("age")) : 0);
                        partnerUser.setGender(partnerData.get("gender") != null ? partnerData.get("gender").toString() : "N/A");
                        partnerUser.setNationality(partnerData.get("nationality") != null ? partnerData.get("nationality").toString() : "N/A");
                        partnerUser.setReligion(partnerData.get("religion") != null ? partnerData.get("religion").toString() : "N/A");
                        partnerUser.setLocation(partnerData.get("location") != null ? partnerData.get("location").toString() : "N/A");

                        displayPartnerProfile();
                    } else {
                        setProfileDetailsVisible(false);
                        checkForPendingInvite();
                    }
                    hideLoading();
                }

                @Override
                public void onFailure(String errorMessage) {
                    checkForPendingInvite();
                    hideLoading();
                }
            });
        } else {
            checkForPendingInvite();
            hideLoading();
        }
    }

    private void setProfileDetailsVisible(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        partnerTextViewAgeLabel.setVisibility(visibility);
        partnerTextViewGenderLabel.setVisibility(visibility);
        partnerTextViewNationalityLabel.setVisibility(visibility);
        partnerTextViewReligionLabel.setVisibility(visibility);
        partnerTextViewLocationLabel.setVisibility(visibility);

        partnerTextViewAge.setVisibility(visibility);
        partnerTextViewGender.setVisibility(visibility);
        partnerTextViewNationality.setVisibility(visibility);
        partnerTextViewReligion.setVisibility(visibility);
        partnerTextViewLocation.setVisibility(visibility);
    }

    private void checkForPendingInvite() {
        userService.getUser(currentUser.getUserId(), new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                String pendingPartnerId = userData.get("pendingPartner") != null ? userData.get("pendingPartner").toString() : null;

                if (!TextUtils.isEmpty(pendingPartnerId)) {
                    userService.getUser(pendingPartnerId, new UserService.UserCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> partnerData) {
                            if (partnerData != null) {
                                partnerUser = new User();
                                partnerUser.setUserId(pendingPartnerId);
                                partnerUser.setEmail(partnerData.get("email") != null ? partnerData.get("email").toString() : "N/A");
                                partnerUser.setName(partnerData.get("name") != null ? partnerData.get("name").toString() : "N/A");
                                partnerUser.setAge(partnerData.get("age") != null ? (int) ((long) partnerData.get("age")) : 0);
                                partnerUser.setGender(partnerData.get("gender") != null ? partnerData.get("gender").toString() : "N/A");
                                partnerUser.setNationality(partnerData.get("nationality") != null ? partnerData.get("nationality").toString() : "N/A");
                                partnerUser.setReligion(partnerData.get("religion") != null ? partnerData.get("religion").toString() : "N/A");
                                partnerUser.setLocation(partnerData.get("location") != null ? partnerData.get("location").toString() : "N/A");

                                displayPendingInvitation(partnerUser.getEmail());
                            } else {
                                displayNoPartnerLinked();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            displayNoPartnerLinked();
                        }
                    });
                } else {
                    displayNoPartnerLinked();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                displayNoPartnerLinked();
            }
        });
    }

    private void displayPendingInvitation(String partnerEmail) {
        partnerTextViewName.setText(partnerEmail + " wants to link with you");
        editTextPartnerEmail.setVisibility(View.GONE);
        buttonSendInvite.setVisibility(View.GONE);
        buttonAcceptInvite.setVisibility(View.VISIBLE);
        buttonDenyInvite.setVisibility(View.VISIBLE);

        buttonAcceptInvite.setOnClickListener(v -> linkPartner());
        buttonDenyInvite.setOnClickListener(v -> denyPartner());
    }

    private void linkPartner() {
        String userId = currentUser.getUserId();
        String partnerId = partnerUser.getUserId();
        String sharedToken = "LINKED_" + userId + "_" + partnerId;

        updateUsersWithSharedToken(userId, partnerId, sharedToken, () -> {
            createRelationship(sharedToken, () -> {
                Toast.makeText(this, "Partner linked successfully!", Toast.LENGTH_SHORT).show();
                loadPartnerProfile();
            });
        });
    }

    private void updateUsersWithSharedToken(String userId, String partnerId, String sharedToken, Runnable onSuccess) {
        userService.updateUser(userId, Map.of("shareToken", sharedToken, "pendingPartner", ""), new UserService.UpdateCallback() {
            @Override
            public void onSuccess() {
                userService.updateUser(partnerId, Map.of("shareToken", sharedToken, "pendingPartner", ""), new UserService.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        onSuccess.run();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(PartnerProfileActivity.this, "Error linking partner: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PartnerProfileActivity.this, "Error linking partner: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRelationship(String sharedToken, Runnable onSuccess) {
        String relationshipId = java.util.UUID.randomUUID().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(new Date());

        Relationship newRelationship = new Relationship(
                relationshipId,
                sharedToken,
                startDate,
                1,
                "Active"
        );

        RelationshipService relationshipService = new RelationshipService();
        relationshipService.createRelationship(newRelationship, new RelationshipService.RelationshipCallback() {
            @Override
            public void onSuccess(Relationship relationship) {
                onSuccess.run();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PartnerProfileActivity.this, "Error saving relationship: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void denyPartner() {
        String userId = currentUser.getUserId();
        userService.updateUser(userId, Map.of("pendingPartner", ""), new UserService.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(PartnerProfileActivity.this, "Partner request denied", Toast.LENGTH_SHORT).show();
                displayNoPartnerLinked();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PartnerProfileActivity.this, "Error denying partner request: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPartnerProfile() {
        partnerTextViewName.setText(partnerUser.getName() != null ? partnerUser.getName() : "N/A");
        partnerTextViewAge.setText(partnerUser.getAge() > 0 ? String.valueOf(partnerUser.getAge()) : "N/A");
        partnerTextViewGender.setText(partnerUser.getGender() != null ? partnerUser.getGender() : "N/A");
        partnerTextViewNationality.setText(partnerUser.getNationality() != null ? partnerUser.getNationality() : "N/A");
        partnerTextViewReligion.setText(partnerUser.getReligion() != null ? partnerUser.getReligion() : "N/A");
        partnerTextViewLocation.setText(partnerUser.getLocation() != null ? partnerUser.getLocation() : "N/A");
        setProfileDetailsVisible(true);

        editTextPartnerEmail.setVisibility(View.GONE);
        buttonSendInvite.setVisibility(View.GONE);
        buttonAcceptInvite.setVisibility(View.GONE);
        buttonDenyInvite.setVisibility(View.GONE);
    }

    private void displayNoPartnerLinked() {
        partnerTextViewName.setText("No Partner Linked");
        setProfileDetailsVisible(false);
        editTextPartnerEmail.setVisibility(View.VISIBLE);
        buttonSendInvite.setVisibility(View.VISIBLE);
        buttonAcceptInvite.setVisibility(View.GONE);
        buttonDenyInvite.setVisibility(View.GONE);

        buttonSendInvite.setOnClickListener(v -> sendInvite());
    }

    private void sendInvite() {
        String partnerEmail = editTextPartnerEmail.getText().toString().trim();
        if (TextUtils.isEmpty(partnerEmail)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.findUserByEmail(partnerEmail, new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> partnerData) {
                if (partnerData != null) {
                    String partnerId = partnerData.get("userId").toString();
                    String partnerShareToken = partnerData.get("shareToken") != null ? partnerData.get("shareToken").toString() : "";

                    if (!TextUtils.isEmpty(partnerShareToken) && partnerShareToken.startsWith("LINKED")) {
                        Toast.makeText(PartnerProfileActivity.this, "This person already has a partner.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    userService.updateUser(partnerId, Map.of("pendingPartner", currentUser.getUserId()), new UserService.UpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(PartnerProfileActivity.this, "Invitation sent", Toast.LENGTH_SHORT).show();
                            displayNoPartnerLinked();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(PartnerProfileActivity.this, "Failed to send invite: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(PartnerProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(PartnerProfileActivity.this, "Error finding user: " + errorMessage, Toast.LENGTH_SHORT).show();
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
