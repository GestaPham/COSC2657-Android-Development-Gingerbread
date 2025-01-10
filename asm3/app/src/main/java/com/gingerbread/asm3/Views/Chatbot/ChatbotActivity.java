package com.gingerbread.asm3.Views.Chatbot;

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
import com.gingerbread.asm3.Services.MessageService;
import com.gingerbread.asm3.Services.OpenAIService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends BaseActivity {

    private UserService userService;
    private MessageService messageService;
    private OpenAIService openAIService;

    private String currentUserId;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private TextView textViewPartnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_chatbot, findViewById(R.id.activity_content));

        userService = new UserService();
        messageService = new MessageService();
        openAIService = new OpenAIService();

        currentUserId = userService.getCurrentUserId();

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        textViewPartnerName = findViewById(R.id.textViewPartnerName);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, currentUserId);
        recyclerViewChat.setAdapter(chatAdapter);

        loadChatbotDetails();
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void loadChatbotDetails() {
        textViewPartnerName.setText("AI Date Advisor");
        textViewPartnerName.setVisibility(View.VISIBLE);
        loadChat();
    }

    private void loadChat() {
        findViewById(R.id.layoutNoPartner).setVisibility(View.GONE);
        findViewById(R.id.layoutChat).setVisibility(View.VISIBLE);
        String conversationId = "AI_" + currentUserId;

        messageService.getMessages(conversationId, messages -> {
            runOnUiThread(() -> {
                this.messages.clear();
                this.messages.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(this.messages.size() - 1);
            });
        }, errorMessage -> runOnUiThread(() ->
                Toast.makeText(ChatbotActivity.this, "Error loading chat: " + errorMessage, Toast.LENGTH_SHORT).show()
        ));
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message userMessage = new Message(text, "AI_" + currentUserId, "AI", currentUserId, System.currentTimeMillis());
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerViewChat.scrollToPosition(messages.size() - 1);

        editTextMessage.setText("");

        openAIService.getDateIdea(text, new OpenAIService.OpenAIResponseCallback() {
            @Override
            public void onSuccess(String aiResponse) {
                runOnUiThread(() -> {
                    Message aiMessage = new Message(aiResponse, "AI_" + currentUserId, currentUserId, "AI", System.currentTimeMillis());
                    messages.add(aiMessage);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerViewChat.scrollToPosition(messages.size() - 1);

                    messageService.sendMessage("AI_" + currentUserId, aiMessage, () -> {}, errorMessage -> {});
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(ChatbotActivity.this, "AI failed to respond: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_ai_chatbot;
    }
}
