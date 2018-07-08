package com.example.suresh.mychattapplication.Controllers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.suresh.mychattapplication.Models.FirebaseDAO;
import com.example.suresh.mychattapplication.Models.Message;
import com.example.suresh.mychattapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class Conversation extends AppCompatActivity implements CommonActivity, View.OnClickListener{

    private ChatView chatView ;
    private ActionBar actionBar;
    private ImageButton btnBack;
    private FirebaseDAO firebaseDAO;



    private String targetUserID,imageURI,fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        targetUserID=getIntent().getStringExtra("targetUserID");
        fullname=getIntent().getStringExtra("fullname");
        imageURI=getIntent().getStringExtra("imageURI");

    }


    @Override
    protected void onStart() {
        super.onStart();

        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_for_chat_activity);
        TextView title=findViewById(R.id.txtUsername);
        title.setText(fullname);

        initializeControls();
        checkForExistingConversation();

        loadMessages();


    }

    @Override
    public void initializeControls() {

        firebaseDAO=FirebaseDAO.getFirebaseDAOObject();


        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        chatView=findViewById(R.id.chat_view);


        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){

                HashMap<String,String> message=new HashMap<>();

                message.put("formattedTime",chatMessage.getFormattedTime());
                message.put("message",chatMessage.getMessage());
                message.put("timestamp",chatMessage.getTimestamp()+"");
                message.put("sender",FirebaseDAO.UID);
                message.put("receiver",targetUserID);

                firebaseDAO.getDbReference()
                        .child("Messages")
                        .child(FirebaseDAO.CHAT_ID)
                        .push()
                        .setValue(message);
                return true;
            }
        });

        chatView.setTypingListener(new ChatView.TypingListener(){
            @Override
            public void userStartedTyping(){
                // will be called when the user starts typing
            }

            @Override
            public void userStoppedTyping(){
                // will be called when the user stops typing
            }
        });
    }

    @Override
    public boolean validateFields() {
        return false;
    }


    public void checkForExistingConversation(){

        //this function checks if the chatting parties are having chat for the first time or they have alrady
        //chatted and got their chat IDs.
        firebaseDAO.getDbReference().child("ConversationLogs")
                .child(FirebaseDAO.UID)
                .child(targetUserID)
                .child("chatID")
                .orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getValue()==null){

                            //getting new key for message node
                             FirebaseDAO.CHAT_ID=firebaseDAO.getDbReference().child("Messages").push().getKey();

                            //registering the message node for the sender
                            firebaseDAO.getDbReference().child("ConversationLogs")
                                                        .child(FirebaseDAO.UID)
                                                        .child(targetUserID)
                                                        .child("chatID")
                                                        .setValue(FirebaseDAO.CHAT_ID);

                            //registering same message node for the receiver
                            firebaseDAO.getDbReference().child("ConversationLogs")
                                                    .child(targetUserID)
                                                    .child(FirebaseDAO.UID)
                                                    .child("chatID")
                                                    .setValue(FirebaseDAO.CHAT_ID);

                            //finally registering the node key generated above inside Message node
                            firebaseDAO.getDbReference().child("Messages")
                                                        .child(FirebaseDAO.CHAT_ID)
                                                        .push()
                                                        .setValue("00");

                        }
                        else
                        {
                            FirebaseDAO.CHAT_ID=dataSnapshot.getValue(String.class);
                        }


                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }


    public void loadMessages(){


            firebaseDAO.getDbReference().child("ConversationLogs")
                    .child(FirebaseDAO.UID)
                    .child(targetUserID)
                    .child("chatID")
                    .orderByKey()
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String chatID = dataSnapshot.getValue(String.class);

                            firebaseDAO.getDbReference().child("Messages")
                                    .child(chatID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            chatView.clearMessages();

                                            for (DataSnapshot eachMessage : dataSnapshot.getChildren()) {

                                                String message = eachMessage.child("message").getValue(String.class);
                                                Long timeStamp = Long.valueOf(eachMessage.child("timestamp").getValue(String.class));
                                                String senderId = eachMessage.child("sender").getValue(String.class);
                                                ChatMessage chatMessage;

                                                if (senderId.equals(FirebaseDAO.UID)) {
                                                    chatMessage = new ChatMessage(message, timeStamp, ChatMessage.Type.SENT);
                                                } else {
                                                    chatMessage = new ChatMessage(message, timeStamp, ChatMessage.Type.RECEIVED);
                                                }
                                                chatView.addMessage(chatMessage);
                                            }


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                    }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnBack:
                Intent i=new Intent(Conversation.this,HomePage.class);
                startActivity(i);
                break;

                default:
                    break;

        }

    }
}
