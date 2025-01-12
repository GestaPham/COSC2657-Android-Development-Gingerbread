package com.gingerbread.asm3.Views.Chatbot;

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
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Services.MessageService;
import com.gingerbread.asm3.Services.OpenAIService;
import com.gingerbread.asm3.Services.UserService;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatbotActivity extends BaseActivity {

    private UserService userService;
    private MessageService messageService;
    private OpenAIService openAIService;

    private String currentUserId;
    private User currentUser;

    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private JSONArray conversationHistory;
    private TextView textViewPartnerName;

    private int userMessageCount = 0;

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

        loadCurrentUserAndChatbotDetails();

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void loadCurrentUserAndChatbotDetails() {
        if (currentUserId != null) {
            userService.getUser(currentUserId, new UserService.UserCallback() {
                @Override
                public void onSuccess(java.util.Map<String, Object> userData) {
                    currentUser = new User();
                    currentUser.setUserId(currentUserId);
                    currentUser.setName(userData.get("name") != null ? userData.get("name").toString() : "");
                    currentUser.setPremium(userData.get("premium") != null && (boolean) userData.get("premium"));
                    loadChatbotDetails();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ChatbotActivity.this,
                            "Failed to load user data: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                    loadChatbotDetails();
                }
            });
        } else {
            loadChatbotDetails();
        }
    }

    private void loadChatbotDetails() {
        textViewPartnerName.setText("AI Relationship Consultant");
        textViewPartnerName.setVisibility(View.VISIBLE);
        loadChat();
    }

    private void loadChat() {
        findViewById(R.id.layoutNoPartner).setVisibility(View.GONE);
        findViewById(R.id.layoutChat).setVisibility(View.VISIBLE);

        String todayDate = new SimpleDateFormat("dd-MM-yy").format(new Date());
        String conversationId = "AI_" + currentUserId + "_" + todayDate;

        messageService.getMessages(conversationId, messages -> {
            runOnUiThread(() -> {
                this.messages.clear();
                this.messages.addAll(messages);

                for (Message message : messages) {
                    if (message.getSenderId().equals(currentUserId)) {
                        userMessageCount++;
                    }
                    addToConversationHistory(message);
                }

                checkMessageLimit(userMessageCount);
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(this.messages.size() - 1);

                if (messages.isEmpty()) {
                    sendInitialAIMessage(conversationId);
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

        String todayDate = new SimpleDateFormat("dd-MM-yy").format(new Date());
        String conversationId = "AI_" + currentUserId + "_" + todayDate;

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Message userMessage = new Message(text, conversationId, "AI", currentUserId, System.currentTimeMillis());
        addToConversationHistory(userMessage);

        messageService.sendMessage(conversationId, userMessage, () -> {
            runOnUiThread(() -> {
                messages.add(userMessage);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                recyclerViewChat.scrollToPosition(messages.size() - 1);
                editTextMessage.setText("");

                userMessageCount++;
                checkMessageLimit(userMessageCount);

                getAIResponse();
            });
        }, errorMessage -> runOnUiThread(() ->
                Toast.makeText(ChatbotActivity.this, "Failed to send message: " + errorMessage, Toast.LENGTH_SHORT).show()
        ));
    }

    private void getAIResponse() {
        String todayDate = new SimpleDateFormat("dd-MM-yy").format(new Date());
        String conversationId = "AI_" + currentUserId + "_" + todayDate;

        openAIService.getAIResponse(conversationHistory.toString(), new OpenAIService.OpenAIResponseCallback() {
            @Override
            public void onSuccess(String aiResponse) {
                Message aiMessage = new Message(aiResponse, conversationId, currentUserId, "AI", System.currentTimeMillis());
                addToConversationHistory(aiMessage);

                messageService.sendMessage(conversationId, aiMessage, () -> {
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

    private void checkMessageLimit(int userMessageCount) {
        int maxMessages = 10;
        if (currentUser != null && currentUser.isPremium()) {
            maxMessages = 100;
        }

        if (userMessageCount >= maxMessages) {
            editTextMessage.setText("Message limit reached for today!");
            editTextMessage.setEnabled(false);
            buttonSend.setEnabled(false);
            buttonSend.setBackgroundTintList(null);
            Toast.makeText(this, "Message limit reached for today!", Toast.LENGTH_SHORT).show();
        } else {
            editTextMessage.setText("");
            editTextMessage.setEnabled(true);
            buttonSend.setEnabled(true);
            buttonSend.setBackgroundTintList(getResources().getColorStateList(R.color.light_pink));
        }
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
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_ai_chatbot;
    }
}
