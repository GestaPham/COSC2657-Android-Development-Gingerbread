package com.gingerbread.asm3.Views.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextNationality = findViewById(R.id.editTextNationality);
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

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(gender) || TextUtils.isEmpty(nationality)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Pattern.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", password)) {
            Toast.makeText(this, "Password must contain at least one number, one uppercase letter, one lowercase letter, and at least 8 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Age must be a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.registerUser(email, password, name, age, gender, nationality, "",
                "", false, "", this, new AuthenticationService.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
