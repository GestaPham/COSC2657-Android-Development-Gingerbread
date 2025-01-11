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

import org.json.JSONArray;
import org.json.JSONObject;

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
    private JSONArray conversationHistory;
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
        conversationHistory = new JSONArray();
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

                if (messages.isEmpty()) {
                    sendInitialAIMessage(conversationId);
                } else {
                    for (Message message : messages) {
                        addToConversationHistory(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerViewChat.scrollToPosition(this.messages.size() - 1);
                }
            });
        }, errorMessage -> {
        });
    }

    private void sendInitialAIMessage(String conversationId) {
        String initialMessageContent = "Hi there! I'm here to help you with any love or relationship advice you need. Feel free to ask me anything!";
        Message initialMessage = new Message(
                initialMessageContent,
                conversationId,
                currentUserId,
                "AI",
                System.currentTimeMillis()
        );

        messageService.sendMessage(conversationId, initialMessage, () -> {
            runOnUiThread(() -> {
                messages.add(initialMessage);
                addToConversationHistory(initialMessage);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                recyclerViewChat.scrollToPosition(messages.size() - 1);
            });
        }, errorMessage -> runOnUiThread(() ->
                Toast.makeText(ChatbotActivity.this, "Failed to send initial message: " + errorMessage, Toast.LENGTH_SHORT).show()
        ));
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message userMessage = new Message(text, "AI_" + currentUserId, "AI", currentUserId, System.currentTimeMillis());
        addToConversationHistory(userMessage);

        messageService.sendMessage("AI_" + currentUserId, userMessage, () -> {
            runOnUiThread(() -> {
                messages.add(userMessage);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                recyclerViewChat.scrollToPosition(messages.size() - 1);
                editTextMessage.setText("");
                getAIResponse();
            });
        }, errorMessage -> runOnUiThread(() ->
                Toast.makeText(ChatbotActivity.this, "Failed to send message: " + errorMessage, Toast.LENGTH_SHORT).show()
        ));
    }

    private void getAIResponse() {
        openAIService.getAIResponse(conversationHistory.toString(), new OpenAIService.OpenAIResponseCallback() {
            @Override
            public void onSuccess(String aiResponse) {
                Message aiMessage = new Message(aiResponse, "AI_" + currentUserId, currentUserId, "AI", System.currentTimeMillis());
                addToConversationHistory(aiMessage);

                messageService.sendMessage("AI_" + currentUserId, aiMessage, () -> {
                    runOnUiThread(() -> {
                        messages.add(aiMessage);
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    });
                }, errorMessage -> runOnUiThread(() ->
                        Toast.makeText(ChatbotActivity.this, "Failed to save AI response: " + errorMessage, Toast.LENGTH_SHORT).show()
                ));
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(ChatbotActivity.this, "AI failed to respond: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void addToConversationHistory(Message message) {
        try {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", message.getSenderId().equals(currentUserId) ? "user" : "assistant");
            messageJson.put("content", message.getMessage());
            conversationHistory.put(messageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
