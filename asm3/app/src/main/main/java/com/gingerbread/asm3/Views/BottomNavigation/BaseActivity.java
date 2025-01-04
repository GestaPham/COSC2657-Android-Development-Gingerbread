package com.gingerbread.asm3.Views.BottomNavigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.Calendar.CalendarActivity;
import com.gingerbread.asm3.Views.Chatbot.ChatbotActivity;
import com.gingerbread.asm3.Views.Home.MainActivity;
import com.gingerbread.asm3.Views.Message.MessageActivity;
import com.gingerbread.asm3.Views.Profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(getSelectedMenuItemId());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    intent = new Intent(this, MainActivity.class);
                }
            } else if (item.getItemId() == R.id.nav_calendar) {
                if (!(this instanceof CalendarActivity)) {
                    intent = new Intent(this, CalendarActivity.class);
                }
            } else if (item.getItemId() == R.id.nav_ai_chatbot) {
                if (!(this instanceof ChatbotActivity)) {
                    intent = new Intent(this, ChatbotActivity.class);
                }
            } else if (item.getItemId() == R.id.nav_message) {
                if (!(this instanceof MessageActivity)) {
                    intent = new Intent(this, MessageActivity.class);
                }
            } else if (item.getItemId() == R.id.nav_profile) {
                if (!(this instanceof ProfileActivity)) {
                    intent = new Intent(this, ProfileActivity.class);
                }
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }

    protected abstract int getLayoutId();

    protected abstract int getSelectedMenuItemId();
}
