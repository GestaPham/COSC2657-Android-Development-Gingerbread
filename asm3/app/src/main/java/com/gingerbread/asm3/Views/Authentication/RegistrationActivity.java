package com.gingerbread.asm3.Views.Authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.AuthenticationService;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextAge, editTextGender;
    private Spinner spinnerCountry, spinnerCity;
    private Button buttonRegister;
    private AuthenticationService authService;
    private Map<String, List<String>> countryCityMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authService = new AuthenticationService();

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        spinnerCity = findViewById(R.id.spinnerCity);
        buttonRegister = findViewById(R.id.buttonRegister);

        countryCityMap = getCountryCityMap();
        setupCountrySpinner();

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void setupCountrySpinner() {
        List<String> countries = new ArrayList<>(countryCityMap.keySet());
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        spinnerCountry.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCountry = countries.get(position);
                setupCitySpinner(selectedCountry);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupCitySpinner(String country) {
        List<String> cities = countryCityMap.getOrDefault(country, new ArrayList<>());
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
    }

    private Map<String, List<String>> getCountryCityMap() {
        Map<String, List<String>> map = new HashMap<>();

        map.put("United States", List.of("New York", "Los Angeles", "Chicago", "Houston"));
        map.put("United Kingdom", List.of("London", "Manchester", "Birmingham", "Liverpool"));
        map.put("India", List.of("Mumbai", "Delhi", "Bangalore", "Chennai"));
        map.put("Canada", List.of("Toronto", "Vancouver", "Montreal", "Ottawa"));
        map.put("Australia", List.of("Sydney", "Melbourne", "Brisbane", "Perth"));

        return map;
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String country = spinnerCountry.getSelectedItem().toString();
        String city = spinnerCity.getSelectedItem().toString();
        String location = country + ", " + city;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(country)
                || TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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

        authService.registerUser(email, password, name, age, gender, country, city, location,
                false, "", this, new AuthenticationService.AuthCallback() {
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
