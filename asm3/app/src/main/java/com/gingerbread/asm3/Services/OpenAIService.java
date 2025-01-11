package com.gingerbread.asm3.Services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenAIService {

    private static final String TAG = "OpenAIService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    private static final String SYSTEM_CONTEXT = "I am a love and relationship consultant expert. I can answer any question or provide advice about love, relationships, or your feelings about your relationship. Feel free to greet me or have a casual conversation to start. However, if the topic strays entirely away from love or relationships, I will respond with: 'Sorry, I can only give advice about love or relationships.'";
    private final List<JSONObject> conversationHistory = new ArrayList<>();

    public interface OpenAIResponseCallback {
        void onSuccess(String response);

        void onFailure(String errorMessage);
    }

    public void getAIResponse(String userInput, OpenAIResponseCallback callback) {
        new Thread(() -> {
            int retries = 0;
            int maxRetries = 5;
            long backoffTime = 1000;

            while (retries < maxRetries) {
                try {
                    JSONObject payload = createPayload(userInput);
                    HttpURLConnection connection = setupConnection();

                    sendPayload(connection, payload);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String response = getResponse(connection);
                        processResponse(response, callback);
                        return;
                    } else if (responseCode == 429) {
                        Log.w(TAG, "Rate limit reached. Retrying...");
                        retries++;
                        Thread.sleep(backoffTime);
                        backoffTime *= 2;
                    } else {
                        postToMainThread(() -> callback.onFailure("HTTP error code: " + responseCode));
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error communicating with OpenAI: " + e.getMessage());
                    postToMainThread(() -> callback.onFailure("Error: " + e.getMessage()));
                    return;
                }
            }
            postToMainThread(() -> callback.onFailure("Max retries reached. Please try again later."));
        }).start();
    }

    private JSONObject createPayload(String userInput) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o-mini");

        if (conversationHistory.isEmpty()) {
            conversationHistory.add(new JSONObject().put("role", "system")
                    .put("content", SYSTEM_CONTEXT));}

        conversationHistory.add(new JSONObject().put("role", "user").put("content", userInput));

        JSONArray messages = new JSONArray(conversationHistory);
        payload.put("messages", messages);
        payload.put("max_tokens", 1000);
        payload.put("temperature", 0.8);

        return payload;
    }

    private HttpURLConnection setupConnection() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    private void sendPayload(HttpURLConnection connection, JSONObject payload) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(payload.toString());
        writer.flush();
        writer.close();
    }

    private String getResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private void processResponse(String response, OpenAIResponseCallback callback) {
        try {
            JSONObject responseObject = new JSONObject(response);
            JSONArray choices = responseObject.getJSONArray("choices");
            if (choices.length() > 0) {
                String content = choices.getJSONObject(0).getJSONObject("message").getString("content");

                conversationHistory.add(new JSONObject().put("role", "assistant").put("content", content.trim()));
                postToMainThread(() -> callback.onSuccess(content.trim()));
            } else {
                postToMainThread(() -> callback.onFailure("No response received from OpenAI."));
            }
        } catch (Exception e) {
            postToMainThread(() -> callback.onFailure("Error parsing response: " + e.getMessage()));
        }
    }

    private void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
    }
}
