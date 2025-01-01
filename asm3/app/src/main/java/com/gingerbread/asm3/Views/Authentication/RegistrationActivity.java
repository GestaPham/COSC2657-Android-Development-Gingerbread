package com.gingerbread.asm3.Views.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.AuthenticationService;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextNationality;
    private TextView errorName, errorEmail, errorPassword, errorConfirmPassword, errorAge, errorGender, errorNationality;
    private Button buttonRegister;
    private AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authService = new AuthenticationService();

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextNationality = findViewById(R.id.editTextNationality);

        errorName = findViewById(R.id.errorName);
        errorEmail = findViewById(R.id.errorEmail);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword = findViewById(R.id.errorConfirmPassword);
        errorAge = findViewById(R.id.errorAge);
        errorGender = findViewById(R.id.errorGender);
        errorNationality = findViewById(R.id.errorNationality);

        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String nationality = editTextNationality.getText().toString().trim();

        clearErrors();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            errorName.setText("Name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            errorEmail.setText("Email is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            errorPassword.setText("Password is required");
            isValid = false;
        } else if (!Pattern.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", password)) {
            errorPassword.setText("Password must contain at least one number, one uppercase letter, one lowercase letter, and at least 8 characters.");
            isValid = false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            errorConfirmPassword.setText("Password confirmation is required");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            errorConfirmPassword.setText("Passwords do not match");
            isValid = false;
        }
        if (TextUtils.isEmpty(ageStr)) {
            errorAge.setText("Age is required");
            isValid = false;
        } else {
            try {
                Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                errorAge.setText("Age must be a valid number");
                isValid = false;
            }
        }
        if (TextUtils.isEmpty(gender)) {
            errorGender.setText("Gender is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(nationality)) {
            errorNationality.setText("Nationality is required");
            isValid = false;
        }

        if (!isValid) return;

        int age = Integer.parseInt(ageStr);

        authService.registerUser(email, password, name, age, gender, nationality, "",
                "", false, "", this, new AuthenticationService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Toast.makeText(RegistrationActivity.this,"Registration successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        errorEmail.setText(errorMessage);
                    }
                });
    }

    private void clearErrors() {
        errorName.setText("");
        errorEmail.setText("");
        errorPassword.setText("");
        errorConfirmPassword.setText("");
        errorAge.setText("");
        errorGender.setText("");
        errorNationality.setText("");
    }
}
