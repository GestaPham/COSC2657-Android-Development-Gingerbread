package com.gingerbread.asm3.Views.Message;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Adapter.ChatAdapter;
import com.gingerbread.asm3.Models.Message;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.MessageService;
import com.gingerbread.asm3.Services.NotificationServiceHttp;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageActivity extends BaseActivity {

    private UserService userService;
    private MessageService messageService;
    private NotificationServiceHttp notificationService;
    private String currentUserId, partnerId, sharedToken, partnerName;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private TextView textViewPartnerName;

    private String userFcmToken, partnerFcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_message, findViewById(R.id.activity_content));

        userService = new UserService();
        messageService = new MessageService();
        notificationService = new NotificationServiceHttp();

        currentUserId = userService.getCurrentUserId();
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        textViewPartnerName = findViewById(R.id.textViewPartnerName);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, currentUserId);
        recyclerViewChat.setAdapter(chatAdapter);

        loadSharedToken();
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void loadSharedToken() {
        userService.getUser(currentUserId, new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                sharedToken = (String) userData.get("shareToken");
                userFcmToken = (String) userData.get("fcmToken");
                if (TextUtils.isEmpty(sharedToken) || !sharedToken.startsWith("LINKED_")) {
                    showNoPartnerMessage();
                } else {
                    determinePartnerId();
                    loadPartnerName();
                    loadChat();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MessageActivity.this, "Failed to load user data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void determinePartnerId() {
        String[] tokens = sharedToken.split("_");
        String userId1 = tokens[1];
        String userId2 = tokens[2];
        partnerId = userId1.equals(currentUserId) ? userId2 : userId1;
    }

    private void loadPartnerName() {
        userService.getUser(partnerId, new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> partnerData) {
                partnerName = (String) partnerData.get("name");
                partnerFcmToken = (String) partnerData.get("fcmToken");
                textViewPartnerName.setText(partnerName != null ? partnerName : "Partner");
                textViewPartnerName.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String errorMessage) {
                textViewPartnerName.setVisibility(View.GONE);
                Toast.makeText(MessageActivity.this, "Failed to load partner's name: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoPartnerMessage() {
        findViewById(R.id.layoutChat).setVisibility(View.GONE);
        findViewById(R.id.layoutNoPartner).setVisibility(View.VISIBLE);
    }

    private void loadChat() {
        findViewById(R.id.layoutNoPartner).setVisibility(View.GONE);
        findViewById(R.id.layoutChat).setVisibility(View.VISIBLE);

        messageService.listenForMessages(sharedToken, new MessageService.MessageListCallbackWithErrorHandling() {
            @Override
            public void onSuccess(List<Message> newMessages) {
                messages.clear();
                messages.addAll(newMessages);
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MessageActivity.this, "Error loading messages: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, errorMessage -> Toast.makeText(MessageActivity.this, "Failed to start listener: " + errorMessage, Toast.LENGTH_SHORT).show());
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Message message = new Message(text, sharedToken, partnerId, currentUserId, System.currentTimeMillis());
        messageService.sendMessage(sharedToken, message, () -> {
            editTextMessage.setText("");
            sendPushNotificationToPartner(text);
        }, errorMessage -> Toast.makeText(this, "Failed to send message: " + errorMessage, Toast.LENGTH_SHORT).show());
    }

    private void sendPushNotificationToPartner(String messageText) {
        if (!TextUtils.isEmpty(partnerFcmToken)) {
            String title = "New message from " + (TextUtils.isEmpty(partnerName) ? "Someone" : partnerName);
            notificationService.sendPushNotification(partnerFcmToken, title, messageText, new NotificationServiceHttp.NotificationCallback() {
                @Override
                public void onSuccess(String messageId) {
                }

                @Override
                public void onFailure(String errorMessage) {
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_message;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                float x = event.getRawX() + v.getLeft() - location[0];
                float y = event.getRawY() + v.getTop() - location[1];
                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    hideKeyboard();
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageService.stopListening();
    }
}
