package com.example.project.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.ChatAdapter;
import com.example.project.MainActivity;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "CHAT FRAGMENT";

    private String mParam1;
    private String mParam2;

    Activity mActivity;
    Context mContext;

    String groupId;
    String groupName;
    String userName;
    FirebaseFirestore db;
    String userId;

    TextView tvChat;
    RecyclerView rvChat;
    EditText etSendMessage;
    ImageButton ibSend;
    Boolean comingFromNotification;

    protected ChatAdapter chatAdapter;
    protected ArrayList<Map<String, Object>> messages;

    private FirebaseAnalytics mFirebaseAnalytics;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();

        groupId = getActivity().getIntent().getStringExtra("groupId");
        groupName = getActivity().getIntent().getStringExtra("groupName");
        userName = getActivity().getIntent().getStringExtra("userName");
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db = FirebaseFirestore.getInstance();
        tvChat = view.findViewById(R.id.tvChat);
        etSendMessage = view.findViewById(R.id.etSendMessage);
        ibSend = view.findViewById(R.id.ibSend);
        rvChat = view.findViewById(R.id.rvChat);
        chatAdapter = ((MainActivity)getActivity()).getChatAdapter();
        rvChat.setAdapter(chatAdapter);

        comingFromNotification = false;
        if (userName == null) {
            comingFromNotification = true;
            getUserName();
            getGroupName(groupId);
            pullChat();

            messages = new ArrayList<>();
            chatAdapter = new ChatAdapter(getContext(), messages, groupId);
            rvChat.setAdapter(chatAdapter);
        } else {
            tvChat.setText(groupName + " chat");
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvChat.setLayoutManager(linearLayoutManager);
        rvChat.scrollToPosition(chatAdapter.getItemCount()-1);

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = etSendMessage.getText().toString();
                if (TextUtils.isEmpty(body)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveMessage(body);
                etSendMessage.getText().clear();
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        db.enableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        db.collection("chats").document(groupId)
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.w(TAG, "Listen error", error);
                        return;
                    }

                    if (value.getData().containsKey("messages")) {
                        Object messagesObj = value.get("messages").getClass();
                        if (messagesObj == HashMap.class) {
                            return;
                        }
                        ArrayList<Map<String, Object>> newMessages = (ArrayList<Map<String, Object>>) value.getData().get("messages");
                        HashMap<String, Object> newUserTrade = ((MainActivity)mContext).getNewUserTrade();
                        if (newUserTrade != null && newUserTrade.size() != 0) {
                            newMessages.add(((MainActivity)mContext).getNewUserTrade());
                        }
                        Map<String, Object> newMessage = newMessages.get(newMessages.size()-1);
                        // work around to fix duplicate trade messages in chat
                        // if new message is a trade
                        if (newMessage.get("type").toString().equals("trade")) {
                            ArrayList<Map<String, Object>> messages = ((MainActivity)mContext).getMessages();
                            if (messages.size() != 0) {
                                String newLot = newMessage.get("lot").toString();
                                Map<String, Object> pastMessage = messages.get(messages.size()-1);
                                // if past message is a trade
                                if (pastMessage.get("type").toString().equals("trade")) {
                                    String pastLot = messages.get(messages.size()-1).get("lot").toString();
                                    if (!newLot.equals(pastLot)) {
                                        messages.add(newMessage);
                                        chatAdapter.notifyDataSetChanged();
                                        rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                                    }
                                } else {
                                    messages.add(newMessage);
                                    chatAdapter.notifyDataSetChanged();
                                    rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                                }
                            } else {
                                messages.add(newMessage);
                                chatAdapter.notifyDataSetChanged();
                                rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                            }

                        } else if (newMessage.get("type").toString().equals("message")){
                            ArrayList<Map<String, Object>> messages = ((MainActivity)mContext).getMessages();

                            if (messages.size() != 0) {
                                Timestamp newTime = (Timestamp)newMessage.get("time");
                                Map<String, Object> pastMessage = messages.get(messages.size()-1);
                                // if past message is a message
                                if (pastMessage.get("type").toString().equals("message")) {
                                    Timestamp pastTime = (Timestamp)messages.get(messages.size()-1).get("time");
                                    if (!newTime.equals(pastTime)) {
                                        messages.add(newMessage);
                                    }
                                    chatAdapter.notifyDataSetChanged();
                                    rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                                } else {
                                    messages.add(newMessage);
                                    chatAdapter.notifyDataSetChanged();
                                    rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                                }
                            } else {
                                messages.add(newMessage);
                                ((MainActivity)mContext).setMessages(messages);
                                rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                            }
                            if (comingFromNotification) {
                                chatAdapter.addAll(messages);
                                rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                            }
                        }
                    }
                }
            });
    }

    private void getUserName() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- USERS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        userName = data.get("name").toString();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void pullChat() {
        DocumentReference docRef = db.collection("chats").document(groupId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- CHATS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        ArrayList<Map<String, Object>> messagesMap = (ArrayList) data.get("messages");
                        ((MainActivity)getActivity()).setMessages(messagesMap);
                        if (comingFromNotification) {
                            chatAdapter.addAll(messagesMap);
                        }
                        chatAdapter.notifyDataSetChanged();
                        rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getGroupName(String groupId) {
        DocumentReference docRef = db.collection("groups").document(groupId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- GROUPS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        groupName = data.get("name").toString();
                        tvChat.setText(groupName + " chat");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity =(Activity) context;
        }
    }

    private void saveMessage(String body) {
        DocumentReference docRef = db.collection("chats").document(groupId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- CHATS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Map<String, Object>> messages = ((MainActivity)getActivity()).getMessages();
                        Map<String, Object> data = document.getData();
                        ArrayList<HashMap<String, Object>> pastMessages = (ArrayList<HashMap<String, Object>>) data.get("messages");

                        HashMap<String, Object> message = new HashMap<String, Object>();
                        message.put("user", userId);
                        message.put("name", userName);
                        message.put("body", body);
                        Timestamp time = Timestamp.now();
                        message.put("time", time);
                        message.put("type", "message");
                        message.put("groupId", groupId);

                        pastMessages.add(message);
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("messages", pastMessages);
                        db.collection("chats").document(groupId).set(updatedData, SetOptions.merge());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}