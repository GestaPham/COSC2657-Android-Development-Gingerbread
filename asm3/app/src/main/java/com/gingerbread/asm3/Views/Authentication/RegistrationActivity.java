package com.gingerbread.asm3.Views.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.AuthenticationService;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextNationality;
    private Button buttonRegister;
    private AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authService = new AuthenticationService();

        View scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextNationality = findViewById(R.id.editTextNationality);

        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(v -> registerUser());

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String nationality = editTextNationality.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email format");
            isValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            isValid = false;
        } else if (!Pattern.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", password)) {
            editTextPassword.setError("Password must contain at least one number, one uppercase letter, one lowercase letter, and at least 8 characters.");
            isValid = false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Password confirmation is required");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        if (TextUtils.isEmpty(ageStr)) {
            editTextAge.setError("Age is required");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age < 18) {
                    editTextAge.setError("You must be at least 18 years old");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                editTextAge.setError("Age must be a valid number");
                isValid = false;
            }
        }
        if (TextUtils.isEmpty(gender)) {
            editTextGender.setError("Gender is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(nationality)) {
            editTextNationality.setError("Nationality is required");
            isValid = false;
        }

        if (!isValid) return;

        int age = Integer.parseInt(ageStr);

        authService.registerUser(email, password, name, age, gender, nationality, "", "", false, "", this, new AuthenticationService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                editTextEmail.setError(errorMessage);
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
