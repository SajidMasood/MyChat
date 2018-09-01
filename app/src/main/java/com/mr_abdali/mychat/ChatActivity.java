package com.mr_abdali.mychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    // TODO: 8/8/2018 Variables Declaration Section...
    public String PassParentId , CurrentChildId;
    Firebase reference1, reference2;

    @BindView(R.id.toolBar) Toolbar toolbar;
    @BindView(R.id.sendButton) ImageView btnSendButton;
    @BindView(R.id.messageArea) EditText edMessageArea;
    @BindView(R.id.layout1) LinearLayout layout;
    @BindView(R.id.scrollView) ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        // TODO: 8/6/2018 toolbar setting...
        toolbar.setTitle("Client Name");
        setSupportActionBar(toolbar);
        //toolbar.setSubtitle("Now you can Chat with your Parent's");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: 8/8/2018 get Intent variable same variables...
        CurrentChildId = getIntent().getStringExtra("UserId");
        PassParentId=getIntent().getStringExtra("Pid");

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://childmonitor-849e8.firebaseio.com/Messages/"+ CurrentChildId + "_" + PassParentId);
        reference2 = new Firebase("https://childmonitor-849e8.firebaseio.com/Messages/"+ PassParentId + "_" +CurrentChildId);

        btnSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = edMessageArea.getText().toString();
                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("Message", messageText);
                    map.put("Users", CurrentChildId);

                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                }
                edMessageArea.setText("");
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("Message").toString();
                String userName = map.get("Users").toString();

                if(userName.equals(PassParentId)){
                    addMessageBox("BABA:\n" + message, 1);
                }
                else if (userName.equals(CurrentChildId)){
                    addMessageBox("YOU-\n"+ message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void addMessageBox(String message, int type){
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
        }
        else if (type==2){
            textView.setBackgroundResource(R.drawable.rounded_corner2);
        }

        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_UP);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
