package com.gingerbread.asm3.Views.Message;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gingerbread.asm3.Models.MessageRoom;
import com.gingerbread.asm3.Models.User;
import com.gingerbread.asm3.R;
import com.gingerbread.asm3.Views.BottomNavigation.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageActivity extends BaseActivity {
    private User user2;
    private String messageRoomId;
    EditText messageInput;
    ImageButton sendMessageButton;
    RecyclerView recyclerView;
    private MessageRoom messageRoom;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_message,findViewById(R.id.activity_content));
        messageInput = findViewById(R.id.inputField);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        //firestore =
        //loadMockData();
        sendMessageButton.setOnClickListener(v->{

        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.nav_message;
    }
    private void getMessageRoom(){
        firestore.get
    }
}
