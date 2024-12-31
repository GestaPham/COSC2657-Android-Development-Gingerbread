package com.gingerbread.asm3.Views.Calendar;

import android.os.Bundle;

import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

public class CalendarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.activity_content));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_calendar;
    }
}
