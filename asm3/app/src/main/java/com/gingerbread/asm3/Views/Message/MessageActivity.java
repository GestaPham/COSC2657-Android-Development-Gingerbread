package com.gingerbread.asm3.Views.Message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Adapter.ChatAdapter;
import com.gingerbread.asm3.Models.Message;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageActivity extends BaseActivity {

    private FirebaseFirestore firestore;
    private UserService userService;
    private String currentUserId, partnerId, sharedToken, partnerName;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private TextView textViewPartnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_message, findViewById(R.id.activity_content));

        firestore = FirebaseFirestore.getInstance();
        userService = new UserService();

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
                if (TextUtils.isEmpty(sharedToken) || !sharedToken.startsWith("LINKED_")) {
                    showNoPartnerMessage();
                } else {
                    // Extract partner ID from shared token
                    String[] tokens = sharedToken.split("_");
                    String userId1 = tokens[1];
                    String userId2 = tokens[2];
                    partnerId = userId1.equals(currentUserId) ? userId2 : userId1;

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

    private void loadPartnerName() {
        userService.getUser(partnerId, new UserService.UserCallback() {
            @Override
            public void onSuccess(Map<String, Object> partnerData) {
                partnerName = (String) partnerData.get("name");
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
        findViewById(R.id.layoutNoPartner).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutChat).setVisibility(View.GONE);
        textViewPartnerName.setVisibility(View.GONE);
    }

    private void loadChat() {
        findViewById(R.id.layoutNoPartner).setVisibility(View.GONE);
        findViewById(R.id.layoutChat).setVisibility(View.VISIBLE);

        CollectionReference chatRef = firestore.collection("chats").document(sharedToken).collection("messages");
        chatRef.orderBy("timestamp").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                messages.clear();
                messages.addAll(value.toObjects(Message.class));
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(text, sharedToken, partnerId, currentUserId, System.currentTimeMillis());

        firestore.collection("chats").document(sharedToken).collection("messages").add(message).addOnSuccessListener(documentReference -> {
            editTextMessage.setText("");
            recyclerViewChat.scrollToPosition(messages.size() - 1);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_message;
    }
}
