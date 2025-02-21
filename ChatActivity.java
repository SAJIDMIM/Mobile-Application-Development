package com.example.flight_booking_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.Adapter.ChatAdapter;
import com.example.flight_booking_app.Model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText userInput;
    private ImageView sendButton, backButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private ImageView homeView,chatView,locationView,profileView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.backbtn); // Add this line

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Handle back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close ChatActivity and return to MainActivity
            }
        });

        homeView = findViewById(R.id.homeBtn);
        homeView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
        });

        chatView = findViewById(R.id.chatBtn);
        chatView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        locationView = findViewById(R.id.locationBtn);
        locationView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ActivityLocationTracking.class);
            startActivity(intent);
        });

        profileView = findViewById(R.id.editProfileBtn);
        profileView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, EditAccount.class);
            startActivity(intent);
        });
    }

    private void sendMessage() {
        String message = userInput.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            messageList.add(new ChatMessage(message, true));  // User message
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
            userInput.setText("");

            // Respond asynchronously
            respondToUser(message);
        }
    }

    private void respondToUser(final String message) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String botResponse = generateBotResponse(message);

                messageList.add(new ChatMessage(botResponse, false));  // Bot message
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }
        }, 1000); // 1-second delay to simulate thinking
    }

    private String generateBotResponse(String message) {
        message = message.toLowerCase(); // Convert to lowercase for better matching

        switch (message) {
            case "hello":
                return "Hello! How can I assist you today?";
            case "how are you?":
                return "I'm just a chatbot, but I'm here to help!";
            case "what airlines are available?":
                return "You can book flights with Delta, American Airlines, Southwest, and more!";
            case "what is the cheapest flight?":
                return "The lowest price is $159.1 with Southwest Airlines.";
            case "what is the fastest flight?":
                return "The shortest flight duration is 2h 30m with American Airlines.";
            case "how long is the flight from jfk to lax?":
                return "Flight duration varies from 2h 30m to 2h 45m.";
            case "is business class available?":
                return "Yes! Business class is available for all flights listed.";
            case "how can i book a flight?":
                return "You can book through our app or website.";
            case "what is the most expensive ticket?":
                return "The highest price listed is $170.6 for Delta Airlines.";
            case "can i cancel my flight?":
                return "Flight cancellation depends on the airline's policy.";
            default:
                return "I didn't understand that. Can you try rephrasing?";
        }    }
}
