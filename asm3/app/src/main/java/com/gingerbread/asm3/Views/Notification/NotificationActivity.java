package com.gingerbread.asm3.Views.Notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.Models.Notification;
import com.gingerbread.asm3.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private TextView tabAll, tabRead;
    private LinearLayout notificationContainer;
    private List<Notification> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        tabAll = findViewById(R.id.tabAll);
        tabRead = findViewById(R.id.tabRead);
        notificationContainer = findViewById(R.id.notificationContainer);

        activateTab(tabAll);
        deactivateTab(tabRead);

        notificationsList = loadMockNotifications();
        displayNotifications(notificationsList);

        buttonBack.setOnClickListener(v -> finish());

        tabAll.setOnClickListener(v -> {
            activateTab(tabAll);
            deactivateTab(tabRead);
            displayNotifications(notificationsList);
        });

        tabRead.setOnClickListener(v -> {
            activateTab(tabRead);
            deactivateTab(tabAll);
            displayNotifications(filterReadNotifications());
        });
    }

    private List<Notification> loadMockNotifications() {
        List<Notification> mockNotifications = new ArrayList<>();
        try {
            InputStream is = getAssets().open("mock_notifications.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray notificationsArray = new JSONArray(json);
            for (int i = 0; i < notificationsArray.length(); i++) {
                JSONObject notificationObject = notificationsArray.getJSONObject(i);
                String title = notificationObject.getString("title");
                String description = notificationObject.getString("description");
                boolean isRead = notificationObject.getBoolean("isRead");

                mockNotifications.add(new Notification(title, description, isRead));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mockNotifications;
    }

    private List<Notification> filterReadNotifications() {
        List<Notification> filteredList = new ArrayList<>();
        for (Notification notification : notificationsList) {
            if (notification.isRead()) {
                filteredList.add(notification);
            }
        }
        return filteredList;
    }

    private void displayNotifications(List<Notification> notifications) {
        notificationContainer.removeAllViews();
        for (Notification notification : notifications) {
            View notificationView = createNotificationView(notification);
            notificationContainer.addView(notificationView);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) notificationView.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            notificationView.setLayoutParams(params);
        }
    }

    private View createNotificationView(Notification notification) {
        View view = getLayoutInflater().inflate(R.layout.item_notification, null);

        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewDescription = view.findViewById(R.id.textViewDescription);

        textViewTitle.setText(notification.getTitle());
        textViewDescription.setText(notification.getDescription());

        return view;
    }

    private void activateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_active_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }

    private void deactivateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_inactive_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }
}
