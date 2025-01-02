package com.gingerbread.asm3.Views.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.AuthenticationService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.Home.MainActivity;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegistration;
    private AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegistration = findViewById(R.id.buttonRegistration);

        authService = new AuthenticationService();

        checkAutoLogin();

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editTextEmail.setError("Email is required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                editTextPassword.setError("Password is required");
                return;
            }

            authService.loginUser(email, password, this, new AuthenticationService.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        buttonRegistration.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void checkAutoLogin() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            UserService userService = new UserService();
            userService.getUser(userId, new UserService.UserCallback() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    String name = (String) userData.get("name");

                    authService.isFcmTokenValid(LoginActivity.this, new AuthenticationService.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser firebaseUser) {
                            Toast.makeText(LoginActivity.this, "Welcome back, " + name, Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(LoginActivity.this, "Session expired, please login again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, "Failed to fetch user data: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
