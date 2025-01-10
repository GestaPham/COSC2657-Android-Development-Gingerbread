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

public class OpenAIService {

    private static final String TAG = "OpenAIService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    public interface OpenAIResponseCallback {
        void onSuccess(String response);

        void onFailure(String errorMessage);
    }

    public void getDateIdea(String userInput, OpenAIResponseCallback callback) {
        new Thread(() -> {
            int retries = 0;
            int maxRetries = 5;
            long backoffTime = 1000;

            while (retries < maxRetries) {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("model", "gpt-4o-mini");
                    JSONArray messages = new JSONArray();

                    messages.put(new JSONObject().put("role", "system")
                            .put("content", "I am a love advice expert. Only suggest romantic and creative date ideas. If the input is invalid, respond with: 'Sorry, I can only give you date ideas.'"));

                    messages.put(new JSONObject().put("role", "user").put("content", userInput));

                    payload.put("messages", messages);
                    payload.put("max_tokens", 100);
                    payload.put("temperature", 0.7);

                    URL url = new URL(API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(payload.toString());
                    writer.flush();
                    writer.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject responseObject = new JSONObject(response.toString());
                        JSONArray choices = responseObject.getJSONArray("choices");
                        if (choices.length() > 0) {
                            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                            postToMainThread(() -> callback.onSuccess(content.trim()));
                            return;
                        } else {
                            postToMainThread(() -> callback.onFailure("No response received from OpenAI."));
                            return;
                        }
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

    private void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
